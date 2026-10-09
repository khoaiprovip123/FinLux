export type WalletType = 'cash' | 'bank' | 'ewallet' | 'card' | 'investment' | 'other';

export interface Wallet {
  id: string;
  name: string;
  type: WalletType;
  balance: number;
  color: string;
  isDefault?: boolean;
  accountNumber?: string;
  bankName?: string;
  createdAt: string;
}

export type CategoryType = 'income' | 'expense';

export interface Category {
  id: string;
  name: string;
  type: CategoryType;
  icon: string;
  color: string;
  isDefault?: boolean;
}

export type TransactionType = 'income' | 'expense' | 'transfer_out' | 'transfer_in';

export interface Transaction {
  id: string;
  type: TransactionType;
  amount: number;
  categoryId?: string | null;
  walletId: string;
  relatedWalletId?: string | null;
  note: string;
  date: string; // ISO date string
  createdAt: string;
}

export interface Budget {
  id: string;
  categoryId: string;
  month: string; // YYYY-MM
  limitAmount: number;
  spentAmount: number;
  notified80?: boolean;
  notified100?: boolean;
}

export type DebtType = 'CREDIT_CARD' | 'BANK_LOAN' | 'INSTALLMENT' | 'PERSONAL_LOAN';

export interface Debt {
  id: string;
  name: string;
  type: DebtType;
  initialAmount: number;
  remainingBalance: number;
  interestRateYearly: number; // % APR
  minPaymentMonthly: number;
  dueDate?: number | null; // 1-31
  isSettled: boolean;
  createdAt: string;
}

export interface Goal {
  id: string;
  name: string;
  targetAmount: number;
  currentAmount: number;
  deadline?: string | null;
  color: string;
  createdAt: string;
}

export interface FinancialSummary {
  totalBalance: number;
  monthlyIncome: number;
  monthlyExpense: number;
  netCashFlow: number;
  freeCashFlow: number;
  totalDebt: number;
  totalGoalSavings: number;
}

export interface UserProfile {
  uid: string;
  email: string | null;
  displayName: string | null;
  photoURL: string | null;
}

