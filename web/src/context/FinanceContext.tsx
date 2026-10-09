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
  INITIAL_TRANSACTIONS,
  INITIAL_BUDGETS,
  INITIAL_DEBTS,
  INITIAL_GOALS,
  PERIODS,
  PeriodKey,
  generateSeedTransactions,
} from '@/lib/constants';
import {
  SYSTEM_CATEGORIES as SYS_CAT,
  isLivingExpense,
  collapseInternalTransferPairs,
} from '@/lib/semantics';
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
  addTransaction: (data: Omit<Transaction, 'id' | 'createdAt'>) => void;
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

function parseDateValue(val: unknown): string {
  if (val && typeof val === 'object' && 'toDate' in val && typeof (val as { toDate: () => Date }).toDate === 'function') {
    return (val as { toDate: () => Date }).toDate().toISOString().slice(0, 10);
  }
  if (typeof val === 'string') {
    return val.slice(0, 10);
  }
  return new Date().toISOString().slice(0, 10);
}

export function FinanceProvider({ children }: { children: React.ReactNode }) {
  // Auth state
  const [user, setUser] = useState<UserProfile | null>(null);
  const [authLoading, setAuthLoading] = useState(true);
  const [isCloudSynced, setIsCloudSynced] = useState(false);
  const [isAuthModalOpen, setIsAuthModalOpen] = useState(false);

  // Navigation & Preferences
  const [activeTab, setActiveTab] = useState<string>('overview');
  const [period, setPeriod] = useState<PeriodKey>('six');
  const [theme, setTheme] = useState<'light' | 'dark'>(() => {
    if (typeof window !== 'undefined') {
      const savedTheme = localStorage.getItem('finlux_prism_theme') as 'light' | 'dark' | null;
      if (savedTheme) return savedTheme;
    }
    return 'light';
  });
  const [search, setSearch] = useState<string>('');
  const [kind, setKind] = useState<'all' | 'income' | 'expense' | 'transfer'>('all');
  const [selectedTxId, setSelectedTxId] = useState<string | null>(null);
  const [toastMessage, setToastMessage] = useState<string | null>(null);
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);

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
    const unsubscribeAuth = onAuthStateChanged(auth, async (firebaseUser: User | null) => {
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

        // Ensure user document exists in Firestore
        try {
          const userDocRef = doc(db, 'users', firebaseUser.uid);
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
          const walletsSnap = await getDocs(collection(db, 'users', firebaseUser.uid, 'wallets'));
          if (walletsSnap.empty) {
            await setDoc(doc(db, 'users', firebaseUser.uid, 'wallets', 'cash'), {
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
    });

    return () => unsubscribeAuth();
  }, []);

  // Realtime Firestore Subscriptions for Logged In User
  useEffect(() => {
    if (!user) return;

    const uid = user.uid;

    // 1. Wallets subscription
    const unsubWallets = onSnapshot(collection(db, 'users', uid, 'wallets'), (snap) => {
      if (!snap.empty) {
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
            opening: Number(data.balance) || 0,
            color: data.color || '#655bdc',
            mark: name.slice(0, 2).toUpperCase(),
            info: data.bankName || name,
            bankName: data.bankName,
            accountNumber: data.accountNumber,
            isDefault: Boolean(data.isDefault),
            createdAt: parseDateValue(data.createdAt),
          };
        });
        setWallets(list);
      }
    });

    // 2. Categories subscription
    const unsubCategories = onSnapshot(collection(db, 'users', uid, 'categories'), (snap) => {
      if (!snap.empty) {
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
        setCategories(list);
      }
    });

    // 3. Transactions subscription
    const unsubTxs = onSnapshot(collection(db, 'users', uid, 'transactions'), (snap) => {
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
          date: parseDateValue(data.date),
          createdAt: parseDateValue(data.createdAt),
        };
      });
      setTransactions(list.sort((a, b) => b.date.localeCompare(a.date)));
    });

    // 4. Budgets subscription
    const unsubBudgets = onSnapshot(collection(db, 'users', uid, 'budgets'), (snap) => {
      if (!snap.empty) {
        const list = snap.docs.map((d) => {
          const data = d.data();
          const monthStr = data.month || (data.periodKey?.startsWith('month:') ? data.periodKey.replace('month:', '') : '2026-10');
          return {
            id: d.id,
            categoryId: data.categoryId || d.id,
            month: monthStr,
            limitAmount: Number(data.limitAmount) || 0,
            spentAmount: Number(data.spentAmount) || 0,
          };
        });
        setBudgets(list);
      }
    });

    // 5. Goals subscription (Đồng bộ hai chiều với Android: savedAmount / currentAmount)
    const unsubGoals = onSnapshot(collection(db, 'users', uid, 'goals'), (snap) => {
      if (!snap.empty) {
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
      }
    });

    // 6. Debts subscription
    const unsubDebts = onSnapshot(collection(db, 'users', uid, 'debts'), (snap) => {
      if (!snap.empty) {
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
      }
    });

    return () => {
      unsubWallets();
      unsubCategories();
      unsubTxs();
      unsubBudgets();
      unsubGoals();
      unsubDebts();
    };
  }, [user]);

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
  const currentPeriodDef = PERIODS[period];

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

  // Helper: compute balance of a wallet up to a specific date
  const computeWalletBalance = useCallback(
    (walletId: string, asOfDate: string) => {
      const w = wallets.find((item) => item.id === walletId);
      if (!w) return 0;
      let val = typeof w.opening === 'number' ? w.opening : w.balance;

      for (const t of transactions) {
        const d = t.date.slice(0, 10);
        if (d > asOfDate) continue;

        if (t.type === 'income' && t.walletId === walletId) {
          val += t.amount;
        } else if (t.type === 'expense' && t.walletId === walletId) {
          val -= t.amount;
        } else if (t.type === 'transfer_out') {
          if (t.walletId === walletId) val -= t.amount;
          if (t.relatedWalletId === walletId) val += t.amount;
        }
      }
      return val;
    },
    [wallets, transactions]
  );

  // Net Worth as of Period End (Đồng bộ chuẩn xác với WalletsViewModel.calculateNetWorth của Android)
  const currentNetWorth = useMemo(() => {
    const regularAssets = wallets
      .filter((w) => w.type !== 'card')
      .reduce((sum, w) => sum + w.balance, 0);
    const creditCardDebt = wallets
      .filter((w) => w.type === 'card')
      .reduce((sum, w) => sum + (w.balance < 0 ? -w.balance : 0), 0);
    return regularAssets - creditCardDebt;
  }, [wallets]);

  // Net Worth before Period Start
  const previousNetWorth = useMemo(() => {
    const d = new Date(currentPeriodDef.start + 'T12:00:00');
    d.setDate(d.getDate() - 1);
    const beforeDate = d.toISOString().slice(0, 10);
    const regularAssets = wallets
      .filter((w) => w.type !== 'card')
      .reduce((sum, w) => sum + computeWalletBalance(w.id, beforeDate), 0);
    const creditCardDebt = wallets
      .filter((w) => w.type === 'card')
      .reduce((sum, w) => {
        const b = computeWalletBalance(w.id, beforeDate);
        return sum + (b < 0 ? -b : 0);
      }, 0);
    return regularAssets - creditCardDebt;
  }, [wallets, currentPeriodDef.start, computeWalletBalance]);

  const netWorthDelta = currentNetWorth - previousNetWorth;

  // Available Liquid cash & bank
  const availableLiquid = useMemo(() => {
    const bankWallet = wallets.find((w) => w.type === 'bank') || wallets[0];
    const cashWallet = wallets.find((w) => w.type === 'cash');
    const bankVal = bankWallet ? computeWalletBalance(bankWallet.id, currentPeriodDef.end) : 0;
    const cashVal = cashWallet ? computeWalletBalance(cashWallet.id, currentPeriodDef.end) : 0;
    return bankVal + cashVal;
  }, [wallets, computeWalletBalance, currentPeriodDef.end]);

  // Monthly Trend Data for Bar Plot
  const monthlyTrend = useMemo(() => {
    const startM = Number(currentPeriodDef.start.slice(5, 7));
    const endM = Number(currentPeriodDef.end.slice(5, 7));
    const list: MonthlyTrendPoint[] = [];

    for (let m = startM; m <= endM; m++) {
      const key = `2026-${String(m).padStart(2, '0')}`;
      const label = `T${m}`;
      let inc = 0;
      let exp = 0;
      for (const t of periodTransactions) {
        if (t.date.startsWith(key)) {
          if (t.type === 'income') inc += t.amount;
          if (isLivingExpense(t)) exp += t.amount;
        }
      }
      list.push({ key, label, income: inc, expense: exp });
    }
    return list;
  }, [currentPeriodDef, periodTransactions]);

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

  // Budget Progress list for active period (Chỉ tính chi tiêu sinh hoạt isLivingExpense)
  const budgetProgressList = useMemo(() => {
    const monthsCount = Math.max(1, monthlyTrend.length);
    return budgets.map((b) => {
      const cat = categories.find((c) => c.id === b.categoryId);
      const spent = periodTransactions
        .filter((t) => isLivingExpense(t) && t.categoryId === b.categoryId)
        .reduce((sum, t) => sum + t.amount, 0);
      const totalLimit = b.limitAmount * monthsCount;
      const ratio = totalLimit > 0 ? spent / totalLimit : 0;
      const status: 'good' | 'normal' | 'warn' = ratio > 1 ? 'warn' : ratio < 0.6 ? 'good' : 'normal';

      return {
        categoryId: b.categoryId,
        name: cat?.name || b.categoryId,
        color: cat?.color || '#655bdc',
        spent,
        limit: totalLimit,
        ratio,
        status,
      };
    });
  }, [budgets, categories, periodTransactions, monthlyTrend.length]);

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
      if (user) {
        try {
          if (data.type === 'transfer_out' && data.relatedWalletId) {
            // Double-entry transfer: pair of _out and _in transactions
            const pairId = `tx_${Date.now()}`;
            const outTxRef = doc(db, 'users', user.uid, 'transactions', `${pairId}_out`);
            const inTxRef = doc(db, 'users', user.uid, 'transactions', `${pairId}_in`);
            const walletRef = doc(db, 'users', user.uid, 'wallets', data.walletId);
            const relatedWalletRef = doc(db, 'users', user.uid, 'wallets', data.relatedWalletId);

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

              const txDate = Timestamp.fromDate(new Date(data.date + 'T12:00:00'));
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
            const newTxRef = doc(collection(db, 'users', user.uid, 'transactions'));
            const walletRef = doc(db, 'users', user.uid, 'wallets', data.walletId);

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
                date: Timestamp.fromDate(new Date(data.date + 'T12:00:00')),
                createdAt: serverTimestamp(),
                updatedAt: serverTimestamp(),
              });
            });
          }

          showToast('Đã lưu giao dịch lên Cloud & đồng bộ App!');
        } catch (err) {
          console.error('Firestore transaction error:', err);
          showToast('Không thể ghi giao dịch lên Cloud.');
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
      }
    },
    [user, wallets, transactions, budgets, debts, goals, saveStateToLocalStorage, showToast]
  );

  // Delete Transaction with atomic Rollback & companion clean-up
  const deleteTransaction = useCallback(
    async (id: string) => {
      if (user) {
        try {
          const txRef = doc(db, 'users', user.uid, 'transactions', id);
          await runTransaction(db, async (t) => {
            const txDoc = await t.get(txRef);
            if (!txDoc.exists()) return;
            const txData = txDoc.data();
            const walletRef = doc(db, 'users', user.uid, 'wallets', txData.walletId);
            const wDoc = await t.get(walletRef);
            if (wDoc.exists()) {
              const curBal = Number(wDoc.data().balance) || 0;
              let revertedBal = curBal;
              if (txData.type === 'expense' || txData.type === 'transfer_out') {
                revertedBal = curBal + Number(txData.amount);
              } else if (txData.type === 'income' || txData.type === 'transfer_in') {
                revertedBal = curBal - Number(txData.amount);
              }
              t.update(walletRef, {
                balance: revertedBal,
                lastTransactionId: `rollback_${id}`,
              });
            }

            // If this is a transfer_out, also delete the companion transfer_in
            if (id.endsWith('_out')) {
              const companionId = id.replace(/_out$/, '_in');
              const companionRef = doc(db, 'users', user.uid, 'transactions', companionId);
              const compDoc = await t.get(companionRef);
              if (compDoc.exists()) {
                const compData = compDoc.data();
                const compWalletRef = doc(db, 'users', user.uid, 'wallets', compData.walletId);
                const cwDoc = await t.get(compWalletRef);
                if (cwDoc.exists()) {
                  const cwBal = Number(cwDoc.data().balance) || 0;
                  t.update(compWalletRef, {
                    balance: cwBal - Number(compData.amount),
                    lastTransactionId: `rollback_${companionId}`,
                  });
                }
                t.delete(companionRef);
              }
            } else if (id.endsWith('_in')) {
              const companionId = id.replace(/_in$/, '_out');
              const companionRef = doc(db, 'users', user.uid, 'transactions', companionId);
              const compDoc = await t.get(companionRef);
              if (compDoc.exists()) {
                const compData = compDoc.data();
                const compWalletRef = doc(db, 'users', user.uid, 'wallets', compData.walletId);
                const cwDoc = await t.get(compWalletRef);
                if (cwDoc.exists()) {
                  const cwBal = Number(cwDoc.data().balance) || 0;
                  t.update(compWalletRef, {
                    balance: cwBal + Number(compData.amount),
                    lastTransactionId: `rollback_${companionId}`,
                  });
                }
                t.delete(companionRef);
              }
            } else if (txData.relatedWalletId) {
              const relRef = doc(db, 'users', user.uid, 'wallets', txData.relatedWalletId);
              const relDoc = await t.get(relRef);
              if (relDoc.exists()) {
                const curRelBal = Number(relDoc.data().balance) || 0;
                t.update(relRef, {
                  balance: curRelBal - Number(txData.amount),
                  lastTransactionId: `rollback_${id}_rel`,
                });
              }
            }

            t.delete(txRef);
          });
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
          await setDoc(doc(db, 'users', user.uid, 'wallets', newWallet.id), {
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
          await setDoc(doc(db, 'users', user.uid, 'wallets', id), updates, { merge: true });
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
          await deleteDoc(doc(db, 'users', user.uid, 'wallets', id));
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
      const budgetId = `${categoryId}_2026-10`;
      if (user) {
        try {
          await setDoc(
            doc(db, 'users', user.uid, 'budgets', budgetId),
            {
              categoryId,
              month: '2026-10',
              periodKey: 'month:2026-10',
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
              month: '2026-10',
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
    [user, budgets, wallets, transactions, debts, goals, saveStateToLocalStorage, showToast]
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
          await setDoc(doc(db, 'users', user.uid, 'debts', newDebt.id), {
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
            doc(db, 'users', user.uid, 'debts', debtId),
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
          await setDoc(doc(db, 'users', user.uid, 'goals', newGoal.id), {
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
            doc(db, 'users', user.uid, 'goals', goalId),
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
