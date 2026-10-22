'use client';

import React, { createContext, useContext, useState, useEffect, useMemo, useCallback } from 'react';
import {
  Wallet,
  WalletType,
  Category,
  Transaction,
  Budget,
  Debt,
  Goal,
  FinancialSummary,
  UserProfile,
  TransactionType,
} from '@/types/finance';
import {
  SYSTEM_CATEGORIES,
  INITIAL_WALLETS,
  INITIAL_BUDGETS,
  INITIAL_DEBTS,
  INITIAL_GOALS,
  generateSeedTransactions,
} from '@/lib/constants';
import {
  SYSTEM_CATEGORIES as SYS_CAT,
  isLivingExpense,
  collapseInternalTransferPairs,
} from '@/lib/semantics';
import { FINANCE_CONFIG, budgetUsage, financeDate, financeDayInstant, monthsInPeriod, netWalletAssets, reportPeriods, shiftDate, validFinanceTimeZone, walletBalanceAt, type PeriodKey, type ReportPeriod } from '@/lib/financeCalculations';
import { deleteTransactionWithRollback } from '@/lib/transactionRollback';
import { FIRESTORE_SCHEMA as schema } from '@/lib/firestoreSchema';
import { mergeAccountPhoto } from '@/lib/accountProfile';
import { auth, db } from '@/lib/firebase';
import { onAuthStateChanged, signOut, User } from 'firebase/auth';
import {
  collection,
  doc,
  onSnapshot,
  runTransaction,
  setDoc,
  deleteDoc,
  serverTimestamp,
  Timestamp,
  getDocs,
} from 'firebase/firestore';

export interface MonthlyTrendPoint {
  key: string;
  label: string;
  income: number;
  expense: number;
}

export interface CategoryBreakdownItem {
  categoryId: string;
  name: string;
  color: string;
  amount: number;
  ratio: number;
}

export interface BudgetProgressItem {
  id: string;
  start: string;
  end: string;
  categoryId: string;
  name: string;
  color: string;
  spent: number;
  limit: number;
  ratio: number;
  status: 'good' | 'normal' | 'warn';
}

interface FinanceContextType {
  // Auth state
  user: UserProfile | null;
  authLoading: boolean;
  isCloudSynced: boolean;
  isAuthModalOpen: boolean;
  setIsAuthModalOpen: (open: boolean) => void;
  openAccountDetails: () => void;
  logout: () => Promise<void>;

  // Collections
  wallets: (Wallet & { mark?: string; info?: string; opening?: number })[];
  categories: Category[];
  transactions: Transaction[];
  budgets: Budget[];
  debts: Debt[];
  goals: Goal[];
  activeTab: string;
  setActiveTab: (tab: string) => void;
  period: PeriodKey;
  periods: Record<PeriodKey, ReportPeriod>;
  financeTimeZone: string;
  setPeriod: (p: PeriodKey) => void;
  theme: 'light' | 'dark';
  setTheme: (t: 'light' | 'dark') => void;
  toggleTheme: () => void;
  search: string;
  setSearch: (q: string) => void;
  kind: 'all' | 'income' | 'expense' | 'transfer';
  setKind: (k: 'all' | 'income' | 'expense' | 'transfer') => void;
  selectedTxId: string | null;
  setSelectedTxId: (id: string | null) => void;
  toastMessage: string | null;
  showToast: (msg: string) => void;
  isSidebarOpen: boolean;
  setIsSidebarOpen: (open: boolean) => void;

  // Filtered in current period
  periodTransactions: Transaction[];
  periodIncome: number;
  periodExpense: number;
  periodCashFlow: number;
  currentNetWorth: number;
  previousNetWorth: number;
  netWorthDelta: number;
  availableLiquid: number;
  monthlyTrend: MonthlyTrendPoint[];
  categoryBreakdown: CategoryBreakdownItem[];
  budgetProgressList: BudgetProgressItem[];
  summary: FinancialSummary;

  // Mutators
  addTransaction: (data: Omit<Transaction, 'id' | 'createdAt'>) => Promise<boolean>;
  deleteTransaction: (id: string) => void;
  addWallet: (wallet: Omit<Wallet, 'id' | 'createdAt'>) => void;
  updateWallet: (id: string, updates: Partial<Wallet>) => void;
  deleteWallet: (id: string) => void;
  updateBudgetLimit: (categoryId: string, limitAmount: number) => void;
  addDebt: (debt: Omit<Debt, 'id' | 'createdAt' | 'isSettled'>) => void;
  makeDebtPayment: (debtId: string, walletId: string, amount: number) => void;
  addGoal: (goal: Omit<Goal, 'id' | 'createdAt'>) => void;
  depositToGoal: (goalId: string, walletId: string, amount: number) => void;
  addSpinReward: (amount: number, note: string) => void;
  resetDemoData: () => void;
}

const FinanceContext = createContext<FinanceContextType | undefined>(undefined);

const STORAGE_KEY = 'finlux_prism_state_v2';

function parseDateValue(val: unknown, timeZone: string = FINANCE_CONFIG.defaultTimeZone): string {
  if (val && typeof val === 'object' && 'toDate' in val && typeof (val as { toDate: () => Date }).toDate === 'function') {
    return financeDate((val as { toDate: () => Date }).toDate(), timeZone);
  }
  if (typeof val === 'string') {
    return val.length === 10 ? val : financeDate(new Date(val), timeZone);
  }
  return financeDate(new Date(), timeZone);
}

export function FinanceProvider({ children }: { children: React.ReactNode }) {
  // Auth state
  const [user, setUser] = useState<UserProfile | null>(null);
  const userId = user?.uid;
  const [authLoading, setAuthLoading] = useState(true);
  const [isCloudSynced, setIsCloudSynced] = useState(false);
  const [isAuthModalOpen, setIsAuthModalOpen] = useState(false);

  // Navigation & Preferences
  const [activeTab, setActiveTab] = useState<string>('overview');
  const [period, setPeriod] = useState<PeriodKey>('six');
  const [financeTimeZone, setFinanceTimeZone] = useState<string>(FINANCE_CONFIG.defaultTimeZone);
  const [clock, setClock] = useState<Date | null>(null);
  const [theme, setTheme] = useState<'light' | 'dark'>('dark');

  // Load theme from localStorage on client mount to prevent SSR hydration mismatch
  useEffect(() => {
    const savedTheme = localStorage.getItem('finlux_prism_theme') as 'light' | 'dark' | null;
    if (savedTheme === 'light' || savedTheme === 'dark') {
      // Read external preferences through a subscription callback, after hydration.
      queueMicrotask(() => setTheme(savedTheme));
    }
  }, []);
  useEffect(() => {
    queueMicrotask(() => setClock(new Date()));
    const timer = setInterval(() => setClock(new Date()), FINANCE_CONFIG.clockRefreshMs);
    return () => clearInterval(timer);
  }, []);
  const [search, setSearch] = useState<string>('');
  const [kind, setKind] = useState<'all' | 'income' | 'expense' | 'transfer'>('all');
  const [selectedTxId, setSelectedTxId] = useState<string | null>(null);
  const [toastMessage, setToastMessage] = useState<string | null>(null);
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);

  const openAccountDetails = useCallback(() => {
    setIsSidebarOpen(false);
    if (!user) {
      setIsAuthModalOpen(true);
      return;
    }
    setActiveTab('settings');
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }, [user]);

  // Core Collections (Chỉ nạp dữ liệu thực khi người dùng đã đăng nhập từ Cloud Firestore, không lưu dữ liệu ảo)
  const [wallets, setWallets] = useState<(Wallet & { mark?: string; info?: string; opening?: number })[]>([]);
  const [categories, setCategories] = useState<Category[]>(SYSTEM_CATEGORIES);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [budgets, setBudgets] = useState<Budget[]>([]);
  const [debts, setDebts] = useState<Debt[]>([]);
  const [goals, setGoals] = useState<Goal[]>([]);

  // Sync data-theme attribute on documentElement
  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
  }, [theme]);

  const toggleTheme = useCallback(() => {
    setTheme((prev) => {
      const next = prev === 'light' ? 'dark' : 'light';
      if (typeof window !== 'undefined') {
        localStorage.setItem('finlux_prism_theme', next);
        document.documentElement.setAttribute('data-theme', next);
      }
      return next;
    });
  }, []);

  const handleSetTheme = useCallback((newTheme: 'light' | 'dark') => {
    setTheme(newTheme);
    if (typeof window !== 'undefined') {
      localStorage.setItem('finlux_prism_theme', newTheme);
      document.documentElement.setAttribute('data-theme', newTheme);
    }
  }, []);

  // Show Toast
  const showToast = useCallback((msg: string) => {
    setToastMessage(msg);
  }, []);

  // Save State to Local Storage (guest mode)
  const saveStateToLocalStorage = useCallback(
    (
      newWallets = wallets,
      newTxs = transactions,
      newBudgets = budgets,
      newDebts = debts,
      newGoals = goals
    ) => {
      if (typeof window === 'undefined') return;
      try {
        localStorage.setItem(
          STORAGE_KEY,
          JSON.stringify({
            wallets: newWallets,
            transactions: newTxs,
            budgets: newBudgets,
            debts: newDebts,
            goals: newGoals,
          })
        );
      } catch (err) {
        console.error('Save state failed', err);
      }
    },
    [wallets, transactions, budgets, debts, goals]
  );

  // Realtime Firebase Auth & Firestore Listener
  useEffect(() => {
    // Safety timeout: Never let auth check block the app for more than 1.2s
    const timer = setTimeout(() => {
      setAuthLoading(false);
    }, 1200);

    const unsubscribeAuth = onAuthStateChanged(
      auth,
      async (firebaseUser: User | null) => {
        clearTimeout(timer);
        if (firebaseUser) {
          const profile: UserProfile = {
            uid: firebaseUser.uid,
            email: firebaseUser.email,
            displayName: firebaseUser.displayName || firebaseUser.email?.split('@')[0] || 'Người dùng',
            photoURL: firebaseUser.photoURL,
          };
          setUser(profile);
          setIsCloudSynced(true);
          setAuthLoading(false);

          // Ensure user document exists in Firestore (non-blocking)
          try {
            const userDocRef = doc(db, schema.users, firebaseUser.uid);
            await setDoc(
              userDocRef,
              {
                displayName: profile.displayName,
                email: profile.email,
                lastLoginAt: serverTimestamp(),
              },
              { merge: true }
            );

            // If brand new user with 0 wallets, seed initial wallet
            const walletsSnap = await getDocs(collection(db, schema.users, firebaseUser.uid, schema.wallets));
            if (walletsSnap.empty) {
              await setDoc(doc(db, schema.users, firebaseUser.uid, schema.wallets, 'cash'), {
                name: 'Tiền mặt',
                type: 'cash',
                balance: 0,
                color: '#1F6FBF',
                isDefault: true,
                createdAt: serverTimestamp(),
              });
            }
          } catch (e) {
            console.error('Error ensuring Firestore user docs:', e);
          }
        } else {
          setUser(null);
          setCategories(SYSTEM_CATEGORIES);
          setFinanceTimeZone(FINANCE_CONFIG.defaultTimeZone);
          setIsCloudSynced(false);
          setAuthLoading(false);
          setWallets([]);
          setTransactions([]);
          setBudgets([]);
          setDebts([]);
          setGoals([]);
          if (typeof window !== 'undefined') {
            localStorage.removeItem(STORAGE_KEY);
          }
        }
      },
      (error) => {
        console.error('onAuthStateChanged error:', error);
        clearTimeout(timer);
        setAuthLoading(false);
      }
    );

    return () => {
      clearTimeout(timer);
      unsubscribeAuth();
    };
  }, []);

  // Realtime Firestore Subscriptions for Logged In User
  useEffect(() => {
    if (!userId) return;
    const authPhoto = auth.currentUser?.uid === userId ? auth.currentUser.photoURL : null;
    return onSnapshot(doc(db, schema.users, userId), (snapshot) => {
      setUser((current) => mergeAccountPhoto(current, userId, snapshot.data()?.[schema.photoUrl], authPhoto));
    }, () => showToast('Không thể tải ảnh hồ sơ. Đang dùng ảnh đăng nhập hoặc icon thay thế.'));
  }, [userId, showToast]);

  useEffect(() => {
    if (!userId) return;
    return onSnapshot(doc(db, schema.users, userId, schema.financialPreferences, schema.salaryCycle), (snapshot) => {
      setFinanceTimeZone(validFinanceTimeZone(snapshot.data()?.financeTimeZone));
    }, () => showToast('Không thể tải múi giờ tài chính. Đang dùng múi giờ mặc định.'));
  }, [userId, showToast]);

  useEffect(() => {
    if (!userId) return;

    const uid = userId;

    // 1. Wallets subscription
    const unsubWallets = onSnapshot(collection(db, schema.users, uid, schema.wallets), (snap) => {
      const list = snap.docs.map((d) => {
        const data = d.data();
        const name = data.name || 'Ví';
        const rawType = String(data.type || 'cash').toLowerCase();
        const walletType: WalletType =
          rawType === 'bank' ? 'bank' :
          rawType === 'ewallet' ? 'ewallet' :
          rawType === 'card' ? 'card' :
          rawType === 'investment' ? 'investment' :
          rawType === 'other' ? 'other' : 'cash';

        return {
          id: d.id,
          name,
          type: walletType,
          balance: Number(data.balance) || 0,
          color: data.color || '#655bdc',
          mark: name.slice(0, 2).toUpperCase(),
          info: data.bankName || name,
          bankName: data.bankName,
          accountNumber: data.accountNumber,
          isDefault: Boolean(data.isDefault),
          createdAt: parseDateValue(data.createdAt, financeTimeZone),
        };
      });
      setWallets(list);
    });

    // 2. Categories subscription
    const unsubCategories = onSnapshot(collection(db, schema.users, uid, schema.categories), (snap) => {
      const list = snap.docs.map((d) => {
        const data = d.data();
        return {
          id: d.id,
          name: data.name || 'Danh mục',
          type: data.type || 'expense',
          icon: data.icon || 'tag',
          color: data.color || '#655bdc',
          isDefault: Boolean(data.isDefault),
        };
      });
      setCategories(list.length ? list : SYSTEM_CATEGORIES);
    });

    // 3. Transactions subscription
    const unsubTxs = onSnapshot(collection(db, schema.users, uid, schema.transactions), (snap) => {
      const list = snap.docs.map((d) => {
        const data = d.data();
        const rawType = String(data.type || 'expense').toLowerCase();
        const txType: TransactionType =
          rawType === 'income' ? 'income' :
          rawType === 'transfer_out' ? 'transfer_out' :
          rawType === 'transfer_in' ? 'transfer_in' : 'expense';

        return {
          id: d.id,
          type: txType,
          amount: Number(data.amount) || 0,
          categoryId: data.categoryId || null,
          walletId: data.walletId || '',
          relatedWalletId: data.relatedWalletId || null,
          note: data.note || '',
          date: parseDateValue(data.date, financeTimeZone),
          createdAt: parseDateValue(data.createdAt, financeTimeZone),
        };
      });
      setTransactions(list.sort((a, b) => b.date.localeCompare(a.date)));
    });

    // 4. Budgets subscription
    const unsubBudgets = onSnapshot(collection(db, schema.users, uid, schema.budgets), (snap) => {
      const list = snap.docs.map((d) => {
        const data = d.data();
        const monthStr = data.month || (data.periodKey?.startsWith('month:') ? data.periodKey.replace('month:', '') : '');
        return {
          id: d.id,
          categoryId: data.categoryId || d.id,
          month: monthStr,
          periodKey: data.periodKey,
          periodStart: data.periodStart,
          periodEndExclusive: data.periodEndExclusive,
          periodBasis: data.periodBasis,
          limitAmount: Number(data.limitAmount) || 0,
          spentAmount: Number(data.spentAmount) || 0,
        };
      });
      setBudgets(list);
    });

    // 5. Goals subscription (Đồng bộ hai chiều với Android: savedAmount / currentAmount)
    const unsubGoals = onSnapshot(collection(db, schema.users, uid, schema.goals), (snap) => {
      const list = snap.docs.map((d) => {
        const data = d.data();
        return {
          id: d.id,
          name: data.name || 'Mục tiêu',
          targetAmount: Number(data.targetAmount) || 0,
          currentAmount: Number(data.savedAmount ?? data.currentAmount) || 0,
          deadline: parseDateValue(data.deadline),
          color: data.color || '#655bdc',
          createdAt: parseDateValue(data.createdAt),
        };
      });
      setGoals(list);
    });

    // 6. Debts subscription
    const unsubDebts = onSnapshot(collection(db, schema.users, uid, schema.debts), (snap) => {
      const list = snap.docs.map((d) => {
        const data = d.data();
        return {
          id: d.id,
          name: data.name || 'Khoản nợ',
          type: data.type || 'CREDIT_CARD',
          initialAmount: Number(data.initialAmount) || 0,
          remainingBalance: Number(data.remainingBalance) || 0,
          interestRateYearly: Number(data.interestRateYearly) || 0,
          minPaymentMonthly: Number(data.minPaymentMonthly) || 0,
          dueDate: data.dueDate,
          isSettled: Boolean(data.isSettled),
          createdAt: parseDateValue(data.createdAt),
        };
      });
      setDebts(list);
    });

    return () => {
      unsubWallets();
      unsubCategories();
      unsubTxs();
      unsubBudgets();
      unsubGoals();
      unsubDebts();
    };
  }, [userId, financeTimeZone]);

  // Logout function
  const logout = useCallback(async () => {
    try {
      await signOut(auth);
      setUser(null);
      setIsCloudSynced(false);
      setWallets([]);
      setTransactions([]);
      setBudgets([]);
      setDebts([]);
      setGoals([]);
      if (typeof window !== 'undefined') {
        localStorage.removeItem(STORAGE_KEY);
      }
      showToast('Đã đăng xuất tài khoản.');
    } catch (e) {
      console.error('Logout error:', e);
      showToast('Đăng xuất thất bại.');
    }
  }, [showToast]);

  // Active Period Boundaries
  const today = clock ? financeDate(clock, financeTimeZone) : '';
  const periods = useMemo(() => reportPeriods(today, [...transactions.map((tx) => tx.date), ...wallets.map((wallet) => wallet.createdAt.slice(0, 10))]), [today, transactions, wallets]);
  const currentPeriodDef = periods[period];

  // Transactions within Period (collapse internal transfer pairs for clean presentation)
  const periodTransactions = useMemo(() => {
    const list = transactions
      .filter((t) => {
        const d = t.date.slice(0, 10);
        return d >= currentPeriodDef.start && d <= currentPeriodDef.end;
      })
      .sort((a, b) => b.date.localeCompare(a.date));
    return collapseInternalTransferPairs(list);
  }, [transactions, currentPeriodDef]);

  // Income, Expense (Living Expenses only - BR-01, BR-02, BR-14), Cash Flow in Period
  const { periodIncome, periodExpense, periodCashFlow } = useMemo(() => {
    let inc = 0;
    let exp = 0;
    for (const t of periodTransactions) {
      if (t.type === 'income') inc += t.amount;
      else if (isLivingExpense(t)) exp += t.amount;
    }
    return {
      periodIncome: inc,
      periodExpense: exp,
      periodCashFlow: inc - exp,
    };
  }, [periodTransactions]);

  const computeWalletBalance = useCallback(
    (walletId: string, asOfDate: string) => {
      const wallet = wallets.find((item) => item.id === walletId);
      return wallet ? walletBalanceAt(wallet, transactions, asOfDate) : 0;
    }, [wallets, transactions]
  );

  const currentNetWorth = useMemo(() => netWalletAssets(wallets.map((wallet) => ({
    ...wallet, balance: computeWalletBalance(wallet.id, currentPeriodDef.end),
  }))), [wallets, computeWalletBalance, currentPeriodDef.end]);

  const previousNetWorth = useMemo(() => netWalletAssets(wallets.map((wallet) => ({
    ...wallet, balance: computeWalletBalance(wallet.id, shiftDate(currentPeriodDef.start, -1)),
  }))), [wallets, computeWalletBalance, currentPeriodDef.start]);

  const netWorthDelta = currentNetWorth - previousNetWorth;

  const availableLiquid = useMemo(() => wallets
    .filter((wallet) => wallet.type === 'cash' || wallet.type === 'bank' || wallet.type === 'ewallet')
    .reduce((sum, wallet) => sum + computeWalletBalance(wallet.id, currentPeriodDef.end), 0),
    [wallets, computeWalletBalance, currentPeriodDef.end]);

  const monthlyTrend = useMemo(() => monthsInPeriod(currentPeriodDef).map((key): MonthlyTrendPoint => {
    const entries = periodTransactions.filter((tx) => tx.date.startsWith(key));
    return {
      key, label: key.slice(5) + '/' + key.slice(0, 4),
      income: entries.filter((tx) => tx.type === 'income').reduce((sum, tx) => sum + tx.amount, 0),
      expense: entries.filter(isLivingExpense).reduce((sum, tx) => sum + tx.amount, 0),
    };
  }), [currentPeriodDef, periodTransactions]);

  // Category Breakdown for Donut Chart (Chỉ tính chi tiêu sinh hoạt Living Expense)
  const categoryBreakdown = useMemo(() => {
    const map: Record<string, number> = {};
    let total = 0;
    for (const t of periodTransactions) {
      if (isLivingExpense(t) && t.categoryId) {
        map[t.categoryId] = (map[t.categoryId] || 0) + t.amount;
        total += t.amount;
      }
    }
    return Object.entries(map)
      .map(([catId, amount]) => {
        const cat = categories.find((c) => c.id === catId);
        return {
          categoryId: catId,
          name: cat?.name || catId,
          color: cat?.color || '#7566e8',
          amount,
          ratio: total > 0 ? amount / total : 0,
        };
      })
      .sort((a, b) => b.amount - a.amount);
  }, [periodTransactions, categories]);

  const budgetProgressList = useMemo(() => budgetUsage(budgets, transactions, currentPeriodDef, financeTimeZone).map((usage): BudgetProgressItem => {
    const category = categories.find((item) => item.id === usage.categoryId);
    const ratio = usage.limit > 0 ? usage.spent / usage.limit : 0;
    return {
      ...usage, name: category?.name || usage.categoryId, color: category?.color || 'var(--purple)', ratio,
      status: ratio >= FINANCE_CONFIG.budgetLimitRatio ? 'warn' : ratio >= FINANCE_CONFIG.budgetWarningRatio ? 'normal' : 'good',
    };
  }), [budgets, categories, transactions, currentPeriodDef, financeTimeZone]);

  // Summary object for backwards compatibility
  const summary = useMemo(() => {
    return {
      totalBalance: currentNetWorth,
      monthlyIncome: periodIncome,
      monthlyExpense: periodExpense,
      netCashFlow: periodCashFlow,
      freeCashFlow: Math.max(0, periodCashFlow),
      totalDebt: debts.filter((d) => !d.isSettled).reduce((s, d) => s + d.remainingBalance, 0),
      totalGoalSavings: goals.reduce((s, g) => s + g.currentAmount, 0),
    };
  }, [currentNetWorth, periodIncome, periodExpense, periodCashFlow, debts, goals]);

  // Add Transaction with Firestore atomic transaction (Đồng bộ 100% với Mobile App)
  const addTransaction = useCallback(
    async (data: Omit<Transaction, 'id' | 'createdAt'>) => {
      if (!Number.isSafeInteger(data.amount) || data.amount <= 0 || data.amount > FINANCE_CONFIG.maxVndAmount ||
        !wallets.some((wallet) => wallet.id === data.walletId) ||
        (data.type === 'transfer_out' && (!data.relatedWalletId || data.relatedWalletId === data.walletId || !wallets.some((wallet) => wallet.id === data.relatedWalletId)))) {
        showToast('Kiểm tra số tiền và tài khoản giao dịch.');
        return false;
      }
      if (user) {
        try {
          if (data.type === 'transfer_out' && data.relatedWalletId) {
            // Double-entry transfer: pair of _out and _in transactions
            const pairId = `tx_${Date.now()}`;
            const outTxRef = doc(db, schema.users, user.uid, schema.transactions, `${pairId}_out`);
            const inTxRef = doc(db, schema.users, user.uid, schema.transactions, `${pairId}_in`);
            const walletRef = doc(db, schema.users, user.uid, schema.wallets, data.walletId);
            const relatedWalletRef = doc(db, schema.users, user.uid, schema.wallets, data.relatedWalletId);

            await runTransaction(db, async (t) => {
              const wDoc = await t.get(walletRef);
              if (!wDoc.exists()) throw new Error('Không tìm thấy ví nguồn');
              const relDoc = await t.get(relatedWalletRef);
              if (!relDoc.exists()) throw new Error('Không tìm thấy ví nhận');

              const currentBal = Number(wDoc.data().balance) || 0;
              const currentRelBal = Number(relDoc.data().balance) || 0;

              t.update(walletRef, {
                balance: currentBal - data.amount,
                lastTransactionId: outTxRef.id,
              });
              t.update(relatedWalletRef, {
                balance: currentRelBal + data.amount,
                lastTransactionId: inTxRef.id,
              });

              const txDate = Timestamp.fromDate(financeDayInstant(data.date, financeTimeZone));
              const now = serverTimestamp();

              t.set(outTxRef, {
                type: 'transfer_out',
                amount: data.amount,
                categoryId: null,
                walletId: data.walletId,
                relatedWalletId: data.relatedWalletId,
                note: data.note || 'Chuyển khoản đi',
                date: txDate,
                createdAt: now,
                updatedAt: now,
              });

              t.set(inTxRef, {
                type: 'transfer_in',
                amount: data.amount,
                categoryId: null,
                walletId: data.relatedWalletId,
                relatedWalletId: data.walletId,
                note: data.note || 'Chuyển khoản đến',
                date: txDate,
                createdAt: now,
                updatedAt: now,
              });
            });
          } else {
            // Single transaction (income or expense)
            const newTxRef = doc(collection(db, schema.users, user.uid, schema.transactions));
            const walletRef = doc(db, schema.users, user.uid, schema.wallets, data.walletId);

            await runTransaction(db, async (t) => {
              const wDoc = await t.get(walletRef);
              if (!wDoc.exists()) throw new Error('Không tìm thấy ví nguồn');
              const currentBal = Number(wDoc.data().balance) || 0;

              const newBal = data.type === 'expense' ? currentBal - data.amount : currentBal + data.amount;

              t.update(walletRef, {
                balance: newBal,
                lastTransactionId: newTxRef.id,
              });

              t.set(newTxRef, {
                type: data.type,
                amount: data.amount,
                categoryId: data.categoryId || null,
                walletId: data.walletId,
                relatedWalletId: null,
                note: data.note,
                date: Timestamp.fromDate(financeDayInstant(data.date, financeTimeZone)),
                createdAt: serverTimestamp(),
                updatedAt: serverTimestamp(),
              });
            });
          }

          showToast('Đã lưu giao dịch lên Cloud & đồng bộ App!');
          return true;
        } catch (err) {
          console.error('Firestore transaction error:', err);
          showToast('Không thể ghi giao dịch lên Cloud.');
          return false;
        }
      } else {
        // Guest mode: Local state & localStorage
        const newTx: Transaction = {
          ...data,
          id: `tx_${Date.now()}`,
          createdAt: new Date().toISOString(),
        };

        const updatedTxs = [newTx, ...transactions];
        setTransactions(updatedTxs);

        const updatedWallets = wallets.map((w) => {
          if (data.type === 'expense' && w.id === data.walletId) {
            return { ...w, balance: w.balance - data.amount };
          }
          if (data.type === 'income' && w.id === data.walletId) {
            return { ...w, balance: w.balance + data.amount };
          }
          if (data.type === 'transfer_out') {
            if (w.id === data.walletId) return { ...w, balance: w.balance - data.amount };
            if (data.relatedWalletId && w.id === data.relatedWalletId) {
              return { ...w, balance: w.balance + data.amount };
            }
          }
          return w;
        });
        setWallets(updatedWallets);

        saveStateToLocalStorage(updatedWallets, updatedTxs, budgets, debts, goals);
        showToast('Đã thêm giao dịch (Lưu cục bộ)!');
        return true;
      }
    },
    [user, wallets, transactions, budgets, debts, goals, saveStateToLocalStorage, showToast, financeTimeZone]
  );

  // Delete Transaction with atomic Rollback & companion clean-up
  const deleteTransaction = useCallback(
    async (id: string) => {
      if (user) {
        try {
          await deleteTransactionWithRollback(db, user.uid, id);
          showToast('Đã xóa giao dịch và hoàn trả số dư trên Cloud!');
        } catch (err) {
          console.error('Delete transaction error:', err);
          showToast('Xóa giao dịch thất bại.');
        }
      } else {
        const tx = transactions.find((item) => item.id === id);
        if (!tx) return;

        const updatedTxs = transactions.filter((item) => item.id !== id);
        setTransactions(updatedTxs);

        const updatedWallets = wallets.map((w) => {
          if (tx.type === 'expense' && w.id === tx.walletId) {
            return { ...w, balance: w.balance + tx.amount };
          }
          if (tx.type === 'income' && w.id === tx.walletId) {
            return { ...w, balance: w.balance - tx.amount };
          }
          if (tx.type === 'transfer_out') {
            if (w.id === tx.walletId) return { ...w, balance: w.balance + tx.amount };
            if (tx.relatedWalletId && w.id === tx.relatedWalletId) {
              return { ...w, balance: w.balance - tx.amount };
            }
          }
          return w;
        });
        setWallets(updatedWallets);

        saveStateToLocalStorage(updatedWallets, updatedTxs, budgets, debts, goals);
        showToast('Đã xóa giao dịch thành công!');
      }
    },
    [user, transactions, wallets, budgets, debts, goals, saveStateToLocalStorage, showToast]
  );

  // Wallets CRUD
  const addWallet = useCallback(
    async (walletData: Omit<Wallet, 'id' | 'createdAt'>) => {
      const newWallet: Wallet & { mark?: string; info?: string; opening?: number } = {
        ...walletData,
        id: `w_${Date.now()}`,
        mark: walletData.name.slice(0, 2).toUpperCase(),
        info: walletData.name,
        opening: walletData.balance,
        createdAt: new Date().toISOString(),
      };

      if (user) {
        try {
          await setDoc(doc(db, schema.users, user.uid, schema.wallets, newWallet.id), {
            name: newWallet.name,
            type: newWallet.type,
            balance: newWallet.balance,
            color: newWallet.color,
            isDefault: Boolean(newWallet.isDefault),
            status: 'active',
            createdAt: serverTimestamp(),
          });
          showToast('Đã tạo tài khoản trên Cloud!');
        } catch (e) {
          console.error(e);
          showToast('Lỗi khi tạo tài khoản trên Cloud.');
        }
      } else {
        const updated = [...wallets, newWallet];
        setWallets(updated);
        saveStateToLocalStorage(updated, transactions, budgets, debts, goals);
        showToast('Đã tạo tài khoản (Cục bộ)!');
      }
    },
    [user, wallets, transactions, budgets, debts, goals, saveStateToLocalStorage, showToast]
  );

  const updateWallet = useCallback(
    async (id: string, updates: Partial<Wallet>) => {
      if (user) {
        try {
          await setDoc(doc(db, schema.users, user.uid, schema.wallets, id), updates, { merge: true });
          showToast('Đã cập nhật tài khoản trên Cloud!');
        } catch (e) {
          console.error(e);
        }
      } else {
        const updated = wallets.map((w) => (w.id === id ? { ...w, ...updates } : w));
        setWallets(updated);
        saveStateToLocalStorage(updated, transactions, budgets, debts, goals);
        showToast('Đã cập nhật tài khoản!');
      }
    },
    [user, wallets, transactions, budgets, debts, goals, saveStateToLocalStorage, showToast]
  );

  const deleteWallet = useCallback(
    async (id: string) => {
      if (user) {
        try {
          await deleteDoc(doc(db, schema.users, user.uid, schema.wallets, id));
          showToast('Đã xóa tài khoản trên Cloud!');
        } catch (e) {
          console.error(e);
        }
      } else {
        const updated = wallets.filter((w) => w.id !== id);
        setWallets(updated);
        saveStateToLocalStorage(updated, transactions, budgets, debts, goals);
        showToast('Đã xóa tài khoản!');
      }
    },
    [user, wallets, transactions, budgets, debts, goals, saveStateToLocalStorage, showToast]
  );

  // Budgets
  const updateBudgetLimit = useCallback(
    async (categoryId: string, limitAmount: number) => {
      const month = today.slice(0, 7);
      const periodKey = `month:${month}`;
      const budgetId = `${categoryId}_${periodKey}`;
      if (user) {
        try {
          await setDoc(
            doc(db, schema.users, user.uid, schema.budgets, budgetId),
            {
              categoryId,
              month,
              periodKey,
              limitAmount,
            },
            { merge: true }
          );
          showToast('Đã cập nhật hạn mức ngân sách trên Cloud!');
        } catch (e) {
          console.error(e);
        }
      } else {
        const existing = budgets.find((b) => b.categoryId === categoryId);
        let updated: Budget[];
        if (existing) {
          updated = budgets.map((b) => (b.id === existing.id ? { ...b, limitAmount } : b));
        } else {
          updated = [
            ...budgets,
            {
              id: budgetId,
              categoryId,
              month,
              limitAmount,
              spentAmount: 0,
            },
          ];
        }
        setBudgets(updated);
        saveStateToLocalStorage(wallets, transactions, updated, debts, goals);
        showToast('Đã cập nhật hạn mức ngân sách!');
      }
    },
    [user, budgets, wallets, transactions, debts, goals, saveStateToLocalStorage, showToast, today]
  );

  // Debts
  const addDebt = useCallback(
    async (debtData: Omit<Debt, 'id' | 'createdAt' | 'isSettled'>) => {
      const newDebt: Debt = {
        ...debtData,
        id: `debt_${Date.now()}`,
        isSettled: false,
        createdAt: new Date().toISOString(),
      };
      if (user) {
        try {
          await setDoc(doc(db, schema.users, user.uid, schema.debts, newDebt.id), {
            ...debtData,
            isSettled: false,
            createdAt: serverTimestamp(),
          });
          showToast('Đã lưu khoản nợ lên Cloud!');
        } catch (e) {
          console.error(e);
        }
      } else {
        const updated = [...debts, newDebt];
        setDebts(updated);
        saveStateToLocalStorage(wallets, transactions, budgets, updated, goals);
        showToast('Đã thêm khoản nợ!');
      }
    },
    [user, debts, wallets, transactions, budgets, goals, saveStateToLocalStorage, showToast]
  );

  const makeDebtPayment = useCallback(
    async (debtId: string, walletId: string, amount: number) => {
      const debt = debts.find((d) => d.id === debtId);
      if (!debt) return;

      addTransaction({
        type: 'expense',
        amount,
        categoryId: SYS_CAT.DEBT_PAYMENT,
        walletId,
        note: `Trả nợ: ${debt.name}`,
        date: new Date().toISOString().slice(0, 10),
      });

      const updatedRemaining = Math.max(0, debt.remainingBalance - amount);
      if (user) {
        try {
          await setDoc(
            doc(db, schema.users, user.uid, schema.debts, debtId),
            {
              remainingBalance: updatedRemaining,
              isSettled: updatedRemaining === 0,
            },
            { merge: true }
          );
        } catch (e) {
          console.error(e);
        }
      } else {
        const updatedDebts = debts.map((d) =>
          d.id === debtId
            ? {
                ...d,
                remainingBalance: updatedRemaining,
                isSettled: updatedRemaining === 0,
              }
            : d
        );
        setDebts(updatedDebts);
        saveStateToLocalStorage(wallets, transactions, budgets, updatedDebts, goals);
      }
    },
    [user, debts, addTransaction, wallets, transactions, budgets, goals, saveStateToLocalStorage]
  );

  // Goals
  const addGoal = useCallback(
    async (goalData: Omit<Goal, 'id' | 'createdAt'>) => {
      const newGoal: Goal = {
        ...goalData,
        id: `goal_${Date.now()}`,
        createdAt: new Date().toISOString(),
      };
      if (user) {
        try {
          await setDoc(doc(db, schema.users, user.uid, schema.goals, newGoal.id), {
            ...goalData,
            savedAmount: goalData.currentAmount,
            createdAt: serverTimestamp(),
          });
          showToast('Đã tạo mục tiêu lên Cloud!');
        } catch (e) {
          console.error(e);
        }
      } else {
        const updated = [...goals, newGoal];
        setGoals(updated);
        saveStateToLocalStorage(wallets, transactions, budgets, debts, updated);
        showToast('Đã tạo mục tiêu tài chính mới!');
      }
    },
    [user, goals, wallets, transactions, budgets, debts, saveStateToLocalStorage, showToast]
  );

  const depositToGoal = useCallback(
    async (goalId: string, walletId: string, amount: number) => {
      const goal = goals.find((g) => g.id === goalId);
      if (!goal) return;

      addTransaction({
        type: 'expense',
        amount,
        categoryId: SYS_CAT.SAVINGS,
        walletId,
        note: `Tích lũy mục tiêu: ${goal.name}`,
        date: new Date().toISOString().slice(0, 10),
      });

      const updatedCurrent = goal.currentAmount + amount;
      if (user) {
        try {
          await setDoc(
            doc(db, schema.users, user.uid, schema.goals, goalId),
            {
              savedAmount: updatedCurrent,
              currentAmount: updatedCurrent,
            },
            { merge: true }
          );
        } catch (e) {
          console.error(e);
        }
      } else {
        const updatedGoals = goals.map((g) =>
          g.id === goalId ? { ...g, currentAmount: updatedCurrent } : g
        );
        setGoals(updatedGoals);
        saveStateToLocalStorage(wallets, transactions, budgets, debts, updatedGoals);
      }
    },
    [user, goals, addTransaction, wallets, transactions, budgets, debts, saveStateToLocalStorage]
  );

  // Spin Reward
  const addSpinReward = useCallback(
    (amount: number, note: string) => {
      if (amount <= 0) return;
      const defaultWallet = wallets.find((w) => w.isDefault) || wallets[0];
      if (!defaultWallet) return;

      addTransaction({
        type: 'income',
        amount,
        categoryId: null,
        walletId: defaultWallet.id,
        note: `Saving Spin: ${note}`,
        date: new Date().toISOString().slice(0, 10),
      });
    },
    [addTransaction, wallets]
  );

  // Reset Demo Data
  const resetDemoData = useCallback(() => {
    const seedTxs = generateSeedTransactions();
    setWallets(INITIAL_WALLETS);
    setCategories(SYSTEM_CATEGORIES);
    setTransactions(seedTxs);
    setBudgets(INITIAL_BUDGETS);
    setDebts(INITIAL_DEBTS);
    setGoals(INITIAL_GOALS);
    setPeriod('six');
    if (typeof window !== 'undefined') {
      localStorage.removeItem(STORAGE_KEY);
    }
    showToast('Đã khôi phục dữ liệu mẫu thành công!');
  }, [showToast]);

  return (
    <FinanceContext.Provider
      value={{
        user,
        authLoading,
        isCloudSynced,
        isAuthModalOpen,
        setIsAuthModalOpen,
        openAccountDetails,
        logout,
        wallets,
        categories,
        transactions,
        budgets,
        debts,
        goals,
        summary,
        activeTab,
        setActiveTab,
        period,
        periods,
        financeTimeZone,
        setPeriod,
        theme,
        setTheme: handleSetTheme,
        toggleTheme,
        search,
        setSearch,
        kind,
        setKind,
        selectedTxId,
        setSelectedTxId,
        toastMessage,
        showToast,
        isSidebarOpen,
        setIsSidebarOpen,
        periodTransactions,
        periodIncome,
        periodExpense,
        periodCashFlow,
        currentNetWorth,
        previousNetWorth,
        netWorthDelta,
        availableLiquid,
        monthlyTrend,
        categoryBreakdown,
        budgetProgressList,
        addTransaction,
        deleteTransaction,
        addWallet,
        updateWallet,
        deleteWallet,
        updateBudgetLimit,
        addDebt,
        makeDebtPayment,
        addGoal,
        depositToGoal,
        addSpinReward,
        resetDemoData,
      }}
    >
      {children}
    </FinanceContext.Provider>
  );
}

export function useFinance() {
  const context = useContext(FinanceContext);
  if (!context) {
    throw new Error('useFinance must be used within a FinanceProvider');
  }
  return context;
}
