/** Names shared by web ledger services; matches the existing Firestore schema. */
export const FIRESTORE_SCHEMA = {
  users: 'users',
  wallets: 'wallets',
  transactions: 'transactions',
  categories: 'categories',
  budgets: 'budgets',
  debts: 'debts',
  goals: 'goals',
  financialPreferences: 'financialPreferences',
  salaryCycle: 'salaryCycle',
  balance: 'balance',
  lastTransactionId: 'lastTransactionId',
  photoUrl: 'photoUrl',
} as const;
