import {initializeApp} from "firebase-admin/app";
import {FieldValue, Timestamp, getFirestore} from "firebase-admin/firestore";
import {getMessaging} from "firebase-admin/messaging";
import {logger} from "firebase-functions";
import {onDocumentWritten} from "firebase-functions/v2/firestore";
import {HttpsError, onCall} from "firebase-functions/v2/https";
import {onSchedule} from "firebase-functions/v2/scheduler";
import {
  BudgetDocument,
  budgetClientContractSignature,
  resolveBudgetContract,
} from "./budgetContract.js";
import {
  DEFAULT_FINANCE_TIME_ZONE,
  FinancialPeriod,
  SalaryCycleConfig,
  normalizeSalaryCycleConfig,
  resolveFinancialPeriod,
  sameLocalDate,
} from "./financialPeriod.js";

initializeApp();

const db = getFirestore();
const FINANCE_TIME_ZONE = DEFAULT_FINANCE_TIME_ZONE;

function requireDocumentId(value: unknown, field: string): string {
  if (typeof value !== "string" || !/^[A-Za-z0-9_-]{1,128}$/.test(value)) {
    throw new HttpsError("invalid-argument", `${field} không hợp lệ`);
  }
  return value;
}

export const deleteDebtCascade = onCall(
  {region: "asia-southeast1"},
  async (request) => {
    const uid = request.auth?.uid;
    if (!uid) throw new HttpsError("unauthenticated", "Phiên đăng nhập không hợp lệ");
    const debtId = requireDocumentId(request.data?.debtId, "debtId");
    const debtRef = db.doc(`users/${uid}/debts/${debtId}`);
    const deleted = await db.runTransaction(async (atomic) => {
      const debt = await atomic.get(debtRef);
      if (!debt.exists) return false;
      const payments = await atomic.get(debtRef.collection("payments"));
      if (payments.size > 450) {
        throw new HttpsError("resource-exhausted", "Khoản nợ có quá nhiều lịch sử thanh toán để xóa an toàn trong một lần");
      }
      payments.docs.forEach((payment) => atomic.delete(payment.ref));
      atomic.delete(debtRef);
      return true;
    });
    return {deleted};
  },
);

export const deleteDealCascade = onCall(
  {region: "asia-southeast1", timeoutSeconds: 120},
  async (request) => {
    const uid = request.auth?.uid;
    if (!uid) throw new HttpsError("unauthenticated", "Phiên đăng nhập không hợp lệ");
    const dealId = requireDocumentId(request.data?.dealId, "dealId");
    const userRef = db.doc(`users/${uid}`);
    const dealRef = userRef.collection("deals").doc(dealId);
    const ledgerQuery = userRef.collection("transactions").where("dealId", "==", dealId);

    await db.runTransaction(async (atomic) => {
      const deal = await atomic.get(dealRef);
      if (!deal.exists) return;
      const ledger = await atomic.get(ledgerQuery);
      if (ledger.size > 400) {
        throw new HttpsError("resource-exhausted", "Deal có quá nhiều bút toán để xóa an toàn trong một lần");
      }

      const deltas = new Map<string, number>();
      for (const entry of ledger.docs) {
        const walletId = entry.get("walletId");
        const amount = Number(entry.get("amount") ?? 0);
        const flowType = String(entry.get("dealFlowType") ?? "").toUpperCase();
        if (typeof walletId !== "string" || walletId.length === 0 || !Number.isSafeInteger(amount) || amount <= 0) continue;
        const reverseDelta = flowType === "OUTLAY_CAPITAL" ? amount :
          flowType === "PRINCIPAL_RECOVERY" || flowType === "CAPITAL_GAIN" ? -amount : 0;
        deltas.set(walletId, (deltas.get(walletId) ?? 0) + reverseDelta);
      }

      const walletRefs = [...deltas.keys()].map((walletId) => userRef.collection("wallets").doc(walletId));
      if (ledger.size + walletRefs.length + 1 > 500) {
        throw new HttpsError("resource-exhausted", "Deal vượt giới hạn ghi nguyên tử; cần quy trình migration có kiểm soát");
      }
      const wallets = await Promise.all(walletRefs.map((walletRef) => atomic.get(walletRef)));
      wallets.forEach((wallet, index) => {
        if (!wallet.exists) throw new HttpsError("failed-precondition", "Ví liên kết với Deal không còn tồn tại");
        const balance = Number(wallet.get("balance") ?? 0);
        const next = balance + (deltas.get(wallet.id) ?? 0);
        if (!Number.isSafeInteger(next)) throw new HttpsError("failed-precondition", "Số dư ví sau hoàn tác không hợp lệ");
        atomic.update(wallet.ref, {balance: next, lastTransactionId: FieldValue.delete()});
      });
      ledger.docs.forEach((entry) => atomic.delete(entry.ref));
      atomic.delete(dealRef);
    });
    return {deleted: true};
  },
);

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

async function getSalaryConfig(uid: string): Promise<SalaryCycleConfig> {
  const current = await db.doc(`users/${uid}/financialPreferences/salaryCycle`).get();
  if (current.exists) return normalizeSalaryCycleConfig(current.data());

  // Transitional read-only fallback. New writes remain on financialPreferences/salaryCycle.
  const legacy = await db.doc(`users/${uid}/preferences/salaryCycle`).get();
  return normalizeSalaryCycleConfig(legacy.exists ? legacy.data() : undefined);
}

function resolvePeriod(date: Date, config: SalaryCycleConfig): {
  key: string;
  start: Timestamp;
  end: Timestamp;
  basis: FinancialPeriod["basis"];
} {
  const period = resolveFinancialPeriod(date, config);
  return {
    key: period.key,
    start: Timestamp.fromDate(period.start),
    end: Timestamp.fromDate(period.endExclusive),
    basis: period.basis,
  };
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

async function reconcileBudget(
  uid: string,
  categoryId: string,
  periodKey: string,
  start: Timestamp,
  end: Timestamp,
  budgetId = `${categoryId}_${periodKey}`,
): Promise<void> {
  const budgetRef = db.doc(`users/${uid}/budgets/${budgetId}`);
  const budgetSnapshot = await budgetRef.get();
  if (!budgetSnapshot.exists) return;

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
      const notificationRef = db.doc(`users/${uid}/notifications/budget_${categoryId}_${periodKey}_${pendingPush.threshold}`);
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

async function reconcileBudgetsForPeriod(
  uid: string,
  categoryId: string,
  periodKey: string,
  start: Timestamp,
  end: Timestamp,
): Promise<void> {
  const budgets = db.collection(`users/${uid}/budgets`);
  const budgetIds = new Set<string>();
  const deterministicId = `${categoryId}_${periodKey}`;
  const deterministic = await budgets.doc(deterministicId).get();
  if (deterministic.exists) budgetIds.add(deterministicId);

  const modern = await budgets.where("periodKey", "==", periodKey).get();
  for (const document of modern.docs) {
    if (document.get("categoryId") === categoryId) budgetIds.add(document.id);
  }

  if (periodKey.startsWith("month:")) {
    const legacy = await budgets.where("month", "==", periodKey.slice("month:".length)).get();
    for (const document of legacy.docs) {
      if (document.get("categoryId") === categoryId) budgetIds.add(document.id);
    }
  }

  await Promise.all([...budgetIds].map((budgetId) =>
    reconcileBudget(uid, categoryId, periodKey, start, end, budgetId),
  ));
}

export const onTransactionWrite = onDocumentWritten(
  {document: "users/{uid}/transactions/{transactionId}", region: "asia-southeast1"},
  async (event) => {
    const uid = event.params.uid;
    const before = event.data?.before.data() as TransactionDocument | undefined;
    const after = event.data?.after.data() as TransactionDocument | undefined;
    const affected = new Map<string, {categoryId: string; periodKey: string; start: Timestamp; end: Timestamp}>();
    
    let config: SalaryCycleConfig | undefined;

    for (const candidate of [before, after]) {
      if (!isExpense(candidate)) continue;
      if (!config) config = await getSalaryConfig(uid);
      const period = resolvePeriod(candidate.date.toDate(), config);
      affected.set(`${candidate.categoryId}_${period.key}`, {
        categoryId: candidate.categoryId, 
        periodKey: period.key,
        start: period.start,
        end: period.end
      });
    }

    await Promise.all([...affected.values()].map(({categoryId, periodKey, start, end}) =>
      reconcileBudgetsForPeriod(uid, categoryId, periodKey, start, end),
    ));
  },
);

export const onBudgetWrite = onDocumentWritten(
  {document: "users/{uid}/budgets/{budgetId}", region: "asia-southeast1"},
  async (event) => {
    const before = event.data?.before.data() as BudgetDocument | undefined;
    const after = event.data?.after.data() as BudgetDocument | undefined;
    if (!after || budgetClientContractSignature(before) === budgetClientContractSignature(after)) return;

    const config = await getSalaryConfig(event.params.uid);
    const budget = resolveBudgetContract(after, config);
    if (!budget) {
      logger.warn("Skipping budget reconciliation because the period contract is invalid", {
        uid: event.params.uid,
        budgetId: event.params.budgetId,
      });
      return;
    }

    await reconcileBudget(
      event.params.uid,
      budget.categoryId,
      budget.periodKey,
      Timestamp.fromDate(budget.start),
      Timestamp.fromDate(budget.endExclusive),
      event.params.budgetId,
    );
  },
);

export const monthlyBudgetReset = onSchedule(
  {schedule: "0 0 * * *", timeZone: FINANCE_TIME_ZONE, region: "asia-southeast1"},
  async () => {
    // Note: for salary cycle, the budget resets on the salary day, not necessarily the 1st of the month.
    // So we should run this function daily and check if today is the boundary of the next period.
    const now = new Date();
    const users = await db.collection("users").get();

    for (const user of users.docs) {
      const config = await getSalaryConfig(user.id);
      
      // Determine what period we are currently in
      const currentPeriod = resolvePeriod(now, config);
      
      // Determine what the PREVIOUS period was by subtracting 15 days from the start of the current period
      // This is a robust way to land in the previous period regardless of whether it's month or salary based.
      const previousDate = new Date(currentPeriod.start.toDate().getTime() - 15 * 24 * 60 * 60 * 1000);
      const previousPeriod = resolvePeriod(previousDate, config);
      
      // We only want to duplicate budgets if today is EXACTLY the start of the current period.
      // For example, a fixed payday or the first/last-day rule starts a new period today.
      if (!sameLocalDate(now, currentPeriod.start.toDate(), config.financeTimeZone)) {
          continue; // Not the boundary day for this user
      }

      const previousBudgets = await user.ref.collection("budgets")
        .where("periodKey", "==", previousPeriod.key)
        .get();
        
      // Fallback: If no previousBudgets with periodKey, try the old 'month' field for legacy transition
      const legacyBudgets = previousBudgets.empty ? await user.ref.collection("budgets")
        .where("month", "==", previousPeriod.key.replace("month:", ""))
        .get() : previousBudgets;

      if (legacyBudgets.empty) continue;

      const batch = db.batch();
      for (const previous of legacyBudgets.docs) {
        const categoryId = String(previous.get("categoryId") ?? "");
        if (!categoryId) continue;
        batch.set(user.ref.collection("budgets").doc(`${categoryId}_${currentPeriod.key}`), {
          categoryId,
          periodKey: currentPeriod.key,
          periodStart: currentPeriod.start,
          periodEndExclusive: currentPeriod.end,
          periodBasis: currentPeriod.basis,
          month: currentPeriod.basis === "CALENDAR_MONTH"
            ? currentPeriod.key.replace("month:", "")
            : null,
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
