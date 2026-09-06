export type BudgetTransactionDocument = {
  type?: string;
  amount?: number;
  categoryId?: string | null;
  goalFlowType?: string | null;
  debtId?: string | null;
  debtPrincipalAmount?: number | null;
  debtInterestAmount?: number | null;
  dealFlowType?: string | null;
};

function nonNegativeMoney(value: unknown): number {
  const parsed = Number(value ?? 0);
  return Number.isFinite(parsed) ? Math.max(0, parsed) : 0;
}

/**
 * Amount that belongs to operating expense / budget consumption.
 *
 * Legacy transactions without semantic metadata retain their full expense amount.
 */
export function operatingExpenseAmount(
  transaction: BudgetTransactionDocument | undefined,
): number {
  if (transaction?.type?.toLowerCase() !== "expense") return 0;

  const amount = nonNegativeMoney(transaction.amount);
  const goalFlowType = transaction.goalFlowType?.toLowerCase();
  const dealFlowType = transaction.dealFlowType?.toLowerCase();

  if (goalFlowType === "allocation") return 0;
  if (dealFlowType === "outlay_capital" || dealFlowType === "capital_loss") return 0;

  const hasDebtSplit =
    typeof transaction.debtId === "string" &&
    transaction.debtId.length > 0 &&
    transaction.debtPrincipalAmount != null &&
    transaction.debtInterestAmount != null;

  if (hasDebtSplit) {
    return Math.min(amount, nonNegativeMoney(transaction.debtInterestAmount));
  }

  return amount;
}
