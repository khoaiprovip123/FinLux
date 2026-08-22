import {initializeApp} from "firebase-admin/app";
import {FieldValue, Timestamp, getFirestore} from "firebase-admin/firestore";
import {getMessaging} from "firebase-admin/messaging";
import {logger} from "firebase-functions";
import {onDocumentWritten} from "firebase-functions/v2/firestore";
import {onSchedule} from "firebase-functions/v2/scheduler";

initializeApp();

const db = getFirestore();
const FINANCE_TIME_ZONE = "Asia/Ho_Chi_Minh";

type TransactionDocument = {
  type?: string;
  amount?: number;
  categoryId?: string | null;
  date?: Timestamp;
};

type ExpenseTransactionDocument = TransactionDocument & {
  categoryId: string;
  date: Timestamp;
};

type ReminderDocument = {
  title?: string;
  amount?: number;
  recurrence?: "daily" | "weekly" | "monthly";
  enabled?: boolean;
  nextTriggerDate?: Timestamp;
  categoryId?: string;
  walletId?: string;
};

function monthKey(date: Date): string {
  const parts = new Intl.DateTimeFormat("en-US", {
    timeZone: FINANCE_TIME_ZONE,
    year: "numeric",
    month: "2-digit",
  }).formatToParts(date);
  const year = parts.find((part) => part.type === "year")?.value;
  const month = parts.find((part) => part.type === "month")?.value;
  if (!year || !month) throw new Error("Không thể xác định tháng tài chính");
  return `${year}-${month}`;
}

function monthBounds(month: string): {start: Timestamp; end: Timestamp} {
  const [year, monthNumber] = month.split("-").map(Number);
  const next = new Date(Date.UTC(year, monthNumber, 1));
  const nextMonth = `${next.getUTCFullYear()}-${String(next.getUTCMonth() + 1).padStart(2, "0")}`;
  return {
    start: Timestamp.fromDate(new Date(`${month}-01T00:00:00+07:00`)),
    end: Timestamp.fromDate(new Date(`${nextMonth}-01T00:00:00+07:00`)),
  };
}

function shiftedMonth(month: string, delta: number): string {
  const [year, monthNumber] = month.split("-").map(Number);
  const value = new Date(Date.UTC(year, monthNumber - 1 + delta, 1));
  return `${value.getUTCFullYear()}-${String(value.getUTCMonth() + 1).padStart(2, "0")}`;
}

function isExpense(data: TransactionDocument | undefined): data is ExpenseTransactionDocument {
  return data?.type?.toLowerCase() === "expense" &&
    typeof data.categoryId === "string" &&
    data.categoryId.length > 0 &&
    data.date instanceof Timestamp;
}

async function sendUserPush(
  uid: string,
  title: string,
  body: string,
  data: Record<string, string>,
): Promise<void> {
  const user = await db.doc(`users/${uid}`).get();
  const tokens = (user.get("fcmTokens") as unknown[] | undefined)
    ?.filter((token): token is string => typeof token === "string" && token.length > 0)
    .slice(0, 500) ?? [];
  if (tokens.length === 0) return;

  const response = await getMessaging().sendEachForMulticast({
    tokens,
    notification: {title, body},
    data,
    android: {priority: "high"},
  });
  if (response.failureCount > 0) {
    logger.warn("Some FinLux pushes failed", {uid, failures: response.failureCount});
  }
}

async function reconcileBudget(uid: string, categoryId: string, month: string): Promise<void> {
  const budgetRef = db.doc(`users/${uid}/budgets/${categoryId}_${month}`);
  const budgetSnapshot = await budgetRef.get();
  if (!budgetSnapshot.exists) return;

  const {start, end} = monthBounds(month);
  const transactionSnapshot = await db.collection(`users/${uid}/transactions`)
    .where("date", ">=", start)
    .where("date", "<", end)
    .get();
  const spentAmount = transactionSnapshot.docs.reduce((total, document) => {
    const transaction = document.data() as TransactionDocument;
    if (transaction.type?.toLowerCase() !== "expense" || transaction.categoryId !== categoryId) return total;
    return total + Math.max(0, Number(transaction.amount ?? 0));
  }, 0);

  const push = await db.runTransaction(async (transaction) => {
    const current = await transaction.get(budgetRef);
    if (!current.exists) return null;

    const limitAmount = Number(current.get("limitAmount") ?? 0);
    const notified80 = current.get("notified80") === true;
    const notified100 = current.get("notified100") === true;
    const reached100 = limitAmount > 0 && spentAmount >= limitAmount;
    const reached80 = limitAmount > 0 && spentAmount >= limitAmount * 0.8;
    const updates: Record<string, unknown> = {spentAmount};
    let pendingPush: {title: string; body: string; threshold: string} | null = null;

    if (reached100 && !notified100) {
      updates.notified80 = true;
      updates.notified100 = true;
      pendingPush = {
        title: "Đã vượt ngân sách",
        body: `Danh mục đã chi ${spentAmount.toLocaleString("vi-VN")}đ trên hạn mức ${limitAmount.toLocaleString("vi-VN")}đ.`,
        threshold: "100",
      };
    } else if (reached80 && !notified80) {
      updates.notified80 = true;
      pendingPush = {
        title: "Sắp chạm hạn mức ngân sách",
        body: `Danh mục đã sử dụng ${Math.floor((spentAmount * 100) / limitAmount)}% ngân sách tháng này.`,
        threshold: "80",
      };
    }

    transaction.update(budgetRef, updates);
    if (pendingPush) {
      const notificationRef = db.doc(`users/${uid}/notifications/budget_${categoryId}_${month}_${pendingPush.threshold}`);
      transaction.set(notificationRef, {
        title: pendingPush.title,
        body: pendingPush.body,
        type: pendingPush.threshold === "100" ? "budget_exceeded" : "budget_warning",
        amount: spentAmount,
        categoryId,
        targetRoute: "budget",
        timestamp: FieldValue.serverTimestamp(),
        createdAt: FieldValue.serverTimestamp(),
        isRead: false,
        isPaid: false,
      }, {merge: true});
    }
    return pendingPush;
  });

  if (push) {
    await sendUserPush(uid, push.title, push.body, {
      destination: "budget",
      categoryId,
      threshold: push.threshold,
    });
  }
}

export const onTransactionWrite = onDocumentWritten(
  {document: "users/{uid}/transactions/{transactionId}", region: "asia-southeast1"},
  async (event) => {
    const uid = event.params.uid;
    const before = event.data?.before.data() as TransactionDocument | undefined;
    const after = event.data?.after.data() as TransactionDocument | undefined;
    const affected = new Map<string, {categoryId: string; month: string}>();

    for (const candidate of [before, after]) {
      if (!isExpense(candidate)) continue;
      const month = monthKey(candidate.date.toDate());
      affected.set(`${candidate.categoryId}_${month}`, {categoryId: candidate.categoryId, month});
    }

    await Promise.all([...affected.values()].map(({categoryId, month}) =>
      reconcileBudget(uid, categoryId, month),
    ));
  },
);

export const monthlyBudgetReset = onSchedule(
  {schedule: "0 0 1 * *", timeZone: FINANCE_TIME_ZONE, region: "asia-southeast1"},
  async () => {
    const currentMonth = monthKey(new Date());
    const previousMonth = shiftedMonth(currentMonth, -1);
    const users = await db.collection("users").get();

    for (const user of users.docs) {
      const previousBudgets = await user.ref.collection("budgets")
        .where("month", "==", previousMonth)
        .get();
      if (previousBudgets.empty) continue;

      const batch = db.batch();
      for (const previous of previousBudgets.docs) {
        const categoryId = String(previous.get("categoryId") ?? "");
        if (!categoryId) continue;
        batch.set(user.ref.collection("budgets").doc(`${categoryId}_${currentMonth}`), {
          categoryId,
          month: currentMonth,
          limitAmount: Number(previous.get("limitAmount") ?? 0),
          spentAmount: 0,
          notified80: false,
          notified100: false,
        }, {merge: false});
      }
      await batch.commit();
    }
  },
);

function nextReminderDate(current: Date, recurrence: ReminderDocument["recurrence"]): Date {
  const next = new Date(current);
  if (recurrence === "daily") next.setUTCDate(next.getUTCDate() + 1);
  else if (recurrence === "weekly") next.setUTCDate(next.getUTCDate() + 7);
  else next.setUTCMonth(next.getUTCMonth() + 1);
  return next;
}

export const sendReminderPush = onSchedule(
  {schedule: "every 60 minutes", timeZone: FINANCE_TIME_ZONE, region: "asia-southeast1"},
  async () => {
    const now = Timestamp.now();
    const reminders = await db.collectionGroup("reminders")
      .where("enabled", "==", true)
      .where("nextTriggerDate", "<=", now)
      .limit(500)
      .get();

    for (const reminderSnapshot of reminders.docs) {
      const reminder = reminderSnapshot.data() as ReminderDocument;
      const userRef = reminderSnapshot.ref.parent.parent;
      if (!userRef || !reminder.nextTriggerDate || !reminder.recurrence) continue;
      const uid = userRef.id;
      const title = reminder.title?.trim() || "Nhắc nhở giao dịch";
      const amount = Math.max(0, Number(reminder.amount ?? 0));
      const body = amount > 0
        ? `Khoản ${title} trị giá ${amount.toLocaleString("vi-VN")}đ đã đến hạn.`
        : `Khoản ${title} đã đến hạn.`;
      const notificationId = `reminder_${reminderSnapshot.id}_${reminder.nextTriggerDate.seconds}`;
      const notificationRef = userRef.collection("notifications").doc(notificationId);
      const nextDate = nextReminderDate(reminder.nextTriggerDate.toDate(), reminder.recurrence);

      const batch = db.batch();
      batch.set(notificationRef, {
        title,
        body,
        type: "reminder",
        amount,
        reminderId: reminderSnapshot.id,
        categoryId: reminder.categoryId ?? null,
        walletId: reminder.walletId ?? null,
        targetRoute: "reminders",
        timestamp: FieldValue.serverTimestamp(),
        createdAt: FieldValue.serverTimestamp(),
        isRead: false,
        isPaid: false,
      }, {merge: true});
      batch.update(reminderSnapshot.ref, {nextTriggerDate: Timestamp.fromDate(nextDate)});
      await batch.commit();

      await sendUserPush(uid, title, body, {
        destination: "reminders",
        reminderId: reminderSnapshot.id,
      });
    }
  },
);
