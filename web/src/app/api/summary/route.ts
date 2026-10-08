import { NextResponse } from 'next/server';
import { INITIAL_WALLETS, INITIAL_TRANSACTIONS, INITIAL_DEBTS, INITIAL_GOALS } from '@/lib/constants';

export async function GET() {
  const totalBalance = INITIAL_WALLETS.reduce((sum, w) => sum + w.balance, 0);
  const monthlyIncome = INITIAL_TRANSACTIONS
    .filter((t) => t.type === 'income')
    .reduce((sum, t) => sum + t.amount, 0);
  const monthlyExpense = INITIAL_TRANSACTIONS
    .filter((t) => t.type === 'expense')
    .reduce((sum, t) => sum + t.amount, 0);
  const netCashFlow = monthlyIncome - monthlyExpense;
  const freeCashFlow = netCashFlow > 0 ? netCashFlow : 0;
  const totalDebt = INITIAL_DEBTS
    .filter((d) => !d.isSettled)
    .reduce((sum, d) => sum + d.remainingBalance, 0);
  const totalGoalSavings = INITIAL_GOALS.reduce((sum, g) => sum + g.currentAmount, 0);

  return NextResponse.json({
    summary: {
      totalBalance,
      monthlyIncome,
      monthlyExpense,
      netCashFlow,
      freeCashFlow,
      totalDebt,
      totalGoalSavings,
    },
    counts: {
      wallets: INITIAL_WALLETS.length,
      transactions: INITIAL_TRANSACTIONS.length,
      debts: INITIAL_DEBTS.length,
      goals: INITIAL_GOALS.length,
    },
    timestamp: new Date().toISOString(),
  });
}
