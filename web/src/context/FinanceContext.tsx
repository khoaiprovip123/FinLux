'use client';

import React, { createContext, useContext, useState, useEffect } from 'react';
import {
  Wallet,
  Category,
  Transaction,
  Budget,
  Debt,
  Goal,
  FinancialSummary,
} from '@/types/finance';
import {
  SYSTEM_CATEGORIES,
  INITIAL_WALLETS,
  INITIAL_TRANSACTIONS,
  INITIAL_BUDGETS,
  INITIAL_DEBTS,
  INITIAL_GOALS,
} from '@/lib/constants';

interface FinanceContextType {
  wallets: Wallet[];
  categories: Category[];
  transactions: Transaction[];
  budgets: Budget[];
  debts: Debt[];
  goals: GoalsState;
  summary: FinancialSummary;
  activeTab: string;
  setActiveTab: (tab: string) => void;
  // Transactions
  addTransaction: (data: Omit<Transaction, 'id' | 'createdAt'>) => void;
  deleteTransaction: (id: string) => void;
  // Wallets
  addWallet: (wallet: Omit<Wallet, 'id' | 'createdAt'>) => void;
  updateWallet: (id: string, updates: Partial<Wallet>) => void;
  deleteWallet: (id: string) => void;
  // Budgets
  updateBudgetLimit: (categoryId: string, limitAmount: number) => void;
  // Debts
  addDebt: (debt: Omit<Debt, 'id' | 'createdAt' | 'isSettled'>) => void;
  makeDebtPayment: (debtId: string, walletId: string, amount: number) => void;
  // Goals
  addGoal: (goal: Omit<Goal, 'id' | 'createdAt'>) => void;
  depositToGoal: (goalId: string, walletId: string, amount: number) => void;
  // Spin
  addSpinReward: (amount: number, note: string) => void;
}

type GoalsState = Goal[];

const FinanceContext = createContext<FinanceContextType | undefined>(undefined);

export function FinanceProvider({ children }: { children: React.ReactNode }) {
  const [activeTab, setActiveTab] = useState<string>('dashboard');
  const [wallets, setWallets] = useState<Wallet[]>(INITIAL_WALLETS);
  const [categories] = useState<Category[]>(SYSTEM_CATEGORIES);
  const [transactions, setTransactions] = useState<Transaction[]>(INITIAL_TRANSACTIONS);
  const [budgets, setBudgets] = useState<Budget[]>(INITIAL_BUDGETS);
  const [debts, setDebts] = useState<Debt[]>(INITIAL_DEBTS);
  const [goals, setGoals] = useState<Goal[]>(INITIAL_GOALS);
  const [currentMonth, setCurrentMonth] = useState<string>('2026-10');

  // Sync state to local storage on browser for persistent demo experience
  useEffect(() => {
    setCurrentMonth(new Date().toISOString().slice(0, 7));
    const saved = localStorage.getItem('finlux_state_v1');
    if (saved) {
      try {
        const parsed = JSON.parse(saved);
        if (parsed.wallets) setWallets(parsed.wallets);
        if (parsed.transactions) setTransactions(parsed.transactions);
        if (parsed.budgets) setBudgets(parsed.budgets);
        if (parsed.debts) setDebts(parsed.debts);
        if (parsed.goals) setGoals(parsed.goals);
      } catch (e) {
        console.error('Error loading stored finlux state', e);
      }
    }
  }, []);

  const saveToStorage = (
    newWallets = wallets,
    newTxs = transactions,
    newBudgets = budgets,
    newDebts = debts,
    newGoals = goals
  ) => {
    localStorage.setItem(
      'finlux_state_v1',
      JSON.stringify({
        wallets: newWallets,
        transactions: newTxs,
        budgets: newBudgets,
        debts: newDebts,
        goals: newGoals,
      })
    );
  };

  // Add Transaction with Atomic Balance & Budget update
  const addTransaction = (data: Omit<Transaction, 'id' | 'createdAt'>) => {
    const newTx: Transaction = {
      ...data,
      id: `tx_${Date.now()}_${Math.random().toString(36).substring(2, 6)}`,
      createdAt: new Date().toISOString(),
    };

    let updatedWallets = [...wallets];
    let updatedBudgets = [...budgets];

    if (data.type === 'expense') {
      // Deduct from wallet
      updatedWallets = updatedWallets.map((w) =>
        w.id === data.walletId ? { ...w, balance: w.balance - data.amount } : w
      );

      // Update budget spentAmount if categoryId matches
      if (data.categoryId) {
        const currentMonth = data.date.slice(0, 7);
        updatedBudgets = updatedBudgets.map((b) => {
          if (b.categoryId === data.categoryId && b.month === currentMonth) {
            return { ...b, spentAmount: b.spentAmount + data.amount };
          }
          return b;
        });
      }
    } else if (data.type === 'income') {
      // Add to wallet
      updatedWallets = updatedWallets.map((w) =>
        w.id === data.walletId ? { ...w, balance: w.balance + data.amount } : w
      );
    } else if (data.type === 'transfer_out') {
      // Deduct from source wallet, and add to destination wallet
      updatedWallets = updatedWallets.map((w) => {
        if (w.id === data.walletId) return { ...w, balance: w.balance - data.amount };
        if (data.relatedWalletId && w.id === data.relatedWalletId) {
          return { ...w, balance: w.balance + data.amount };
        }
        return w;
      });
    }

    const updatedTxs = [newTx, ...transactions];
    setWallets(updatedWallets);
    setBudgets(updatedBudgets);
    setTransactions(updatedTxs);
    saveToStorage(updatedWallets, updatedTxs, updatedBudgets, debts, goals);
  };

  // Delete Transaction with atomic Rollback
  const deleteTransaction = (id: string) => {
    const tx = transactions.find((t) => t.id === id);
    if (!tx) return;

    let updatedWallets = [...wallets];
    let updatedBudgets = [...budgets];

    if (tx.type === 'expense') {
      // Refund back to wallet
      updatedWallets = updatedWallets.map((w) =>
        w.id === tx.walletId ? { ...w, balance: w.balance + tx.amount } : w
      );
      // Rollback budget
      if (tx.categoryId) {
        const month = tx.date.slice(0, 7);
        updatedBudgets = updatedBudgets.map((b) => {
          if (b.categoryId === tx.categoryId && b.month === month) {
            return { ...b, spentAmount: Math.max(0, b.spentAmount - tx.amount) };
          }
          return b;
        });
      }
    } else if (tx.type === 'income') {
      // Deduct back from wallet
      updatedWallets = updatedWallets.map((w) =>
        w.id === tx.walletId ? { ...w, balance: w.balance - tx.amount } : w
      );
    } else if (tx.type === 'transfer_out') {
      // Revert transfer
      updatedWallets = updatedWallets.map((w) => {
        if (w.id === tx.walletId) return { ...w, balance: w.balance + tx.amount };
        if (tx.relatedWalletId && w.id === tx.relatedWalletId) {
          return { ...w, balance: w.balance - tx.amount };
        }
        return w;
      });
    }

    const updatedTxs = transactions.filter((t) => t.id !== id);
    setWallets(updatedWallets);
    setBudgets(updatedBudgets);
    setTransactions(updatedTxs);
    saveToStorage(updatedWallets, updatedTxs, updatedBudgets, debts, goals);
  };

  // Wallets
  const addWallet = (walletData: Omit<Wallet, 'id' | 'createdAt'>) => {
    const newWallet: Wallet = {
      ...walletData,
      id: `w_${Date.now()}`,
      createdAt: new Date().toISOString(),
    };
    const updated = [...wallets, newWallet];
    setWallets(updated);
    saveToStorage(updated, transactions, budgets, debts, goals);
  };

  const updateWallet = (id: string, updates: Partial<Wallet>) => {
    const updated = wallets.map((w) => (w.id === id ? { ...w, ...updates } : w));
    setWallets(updated);
    saveToStorage(updated, transactions, budgets, debts, goals);
  };

  const deleteWallet = (id: string) => {
    const updated = wallets.filter((w) => w.id !== id);
    setWallets(updated);
    saveToStorage(updated, transactions, budgets, debts, goals);
  };

  // Budgets
  const updateBudgetLimit = (categoryId: string, limitAmount: number) => {
    const currentMonth = new Date().toISOString().slice(0, 7);
    const existing = budgets.find((b) => b.categoryId === categoryId && b.month === currentMonth);
    let updated: Budget[];
    if (existing) {
      updated = budgets.map((b) =>
        b.id === existing.id ? { ...b, limitAmount } : b
      );
    } else {
      updated = [
        ...budgets,
        {
          id: `${categoryId}_${currentMonth}`,
          categoryId,
          month: currentMonth,
          limitAmount,
          spentAmount: 0,
        },
      ];
    }
    setBudgets(updated);
    saveToStorage(wallets, transactions, updated, debts, goals);
  };

  // Debt Payment
  const makeDebtPayment = (debtId: string, walletId: string, amount: number) => {
    const debt = debts.find((d) => d.id === debtId);
    if (!debt) return;

    // 1. Create repayment transaction (Hoán đổi tài sản / Trả nợ gốc theo BR-06/BR-DEBT)
    addTransaction({
      type: 'expense',
      amount,
      categoryId: null, // Hoán đổi tài sản, không tính living expense
      walletId,
      note: `Trả nợ: ${debt.name}`,
      date: new Date().toISOString(),
    });

    // 2. Reduce remaining balance
    const updatedRemaining = Math.max(0, debt.remainingBalance - amount);
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
    saveToStorage(wallets, transactions, budgets, updatedDebts, goals);
  };

  const addDebt = (debtData: Omit<Debt, 'id' | 'createdAt' | 'isSettled'>) => {
    const newDebt: Debt = {
      ...debtData,
      id: `debt_${Date.now()}`,
      isSettled: false,
      createdAt: new Date().toISOString(),
    };
    const updated = [...debts, newDebt];
    setDebts(updated);
    saveToStorage(wallets, transactions, budgets, updated, goals);
  };

  // Goals
  const addGoal = (goalData: Omit<Goal, 'id' | 'createdAt'>) => {
    const newGoal: Goal = {
      ...goalData,
      id: `goal_${Date.now()}`,
      createdAt: new Date().toISOString(),
    };
    const updated = [...goals, newGoal];
    setGoals(updated);
    saveToStorage(wallets, transactions, budgets, debts, updated);
  };

  const depositToGoal = (goalId: string, walletId: string, amount: number) => {
    const goal = goals.find((g) => g.id === goalId);
    if (!goal) return;

    // Create transaction
    addTransaction({
      type: 'expense',
      amount,
      categoryId: null,
      walletId,
      note: `Tích lũy mục tiêu: ${goal.name}`,
      date: new Date().toISOString(),
    });

    const updatedGoals = goals.map((g) =>
      g.id === goalId ? { ...g, currentAmount: g.currentAmount + amount } : g
    );
    setGoals(updatedGoals);
    saveToStorage(wallets, transactions, budgets, debts, updatedGoals);
  };

  // Saving Spin Reward
  const addSpinReward = (amount: number, note: string) => {
    if (amount <= 0) return;
    const defaultWallet = wallets.find((w) => w.isDefault) || wallets[0];
    if (!defaultWallet) return;

    // Add to default wallet as income
    addTransaction({
      type: 'income',
      amount,
      categoryId: 'cat_gift',
      walletId: defaultWallet.id,
      note: `Saving Spin: ${note}`,
      date: new Date().toISOString(),
    });
  };

  // Compute live financial summary
  const totalBalance = wallets.reduce((sum, w) => sum + w.balance, 0);
  const monthlyTxs = transactions.filter((t) => t.date.startsWith(currentMonth));
  const monthlyIncome = monthlyTxs
    .filter((t) => t.type === 'income')
    .reduce((sum, t) => sum + t.amount, 0);
  const monthlyExpense = monthlyTxs
    .filter((t) => t.type === 'expense')
    .reduce((sum, t) => sum + t.amount, 0);
  const netCashFlow = monthlyIncome - monthlyExpense;
  const freeCashFlow = netCashFlow > 0 ? netCashFlow : 0;
  const totalDebt = debts
    .filter((d) => !d.isSettled)
    .reduce((sum, d) => sum + d.remainingBalance, 0);
  const totalGoalSavings = goals.reduce((sum, g) => sum + g.currentAmount, 0);

  const summary: FinancialSummary = {
    totalBalance,
    monthlyIncome,
    monthlyExpense,
    netCashFlow,
    freeCashFlow,
    totalDebt,
    totalGoalSavings,
  };

  return (
    <FinanceContext.Provider
      value={{
        wallets,
        categories,
        transactions,
        budgets,
        debts,
        goals,
        summary,
        activeTab,
        setActiveTab,
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
