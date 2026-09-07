import {
  FinancialPeriod,
  SalaryCycleConfig,
  resolveFinancialPeriod,
} from "./financialPeriod.js";

export type BudgetDocument = {
  categoryId?: unknown;
  periodKey?: unknown;
  periodStart?: unknown;
  periodEndExclusive?: unknown;
  periodBasis?: unknown;
  month?: unknown;
  limitAmount?: unknown;
  spentAmount?: unknown;
  notified80?: unknown;
  notified100?: unknown;
};

export type ResolvedBudgetContract = {
  categoryId: string;
  periodKey: string;
  start: Date;
  endExclusive: Date;
  basis: FinancialPeriod["basis"];
};

export function resolveBudgetContract(
  data: BudgetDocument | undefined,
  salaryConfig: SalaryCycleConfig,
): ResolvedBudgetContract | null {
  if (!data || typeof data.categoryId !== "string" || data.categoryId.length === 0) return null;

  const periodKey = typeof data.periodKey === "string" ? data.periodKey : "";
  const periodBasis = data.periodBasis;
  const start = asDate(data.periodStart);
  const endExclusive = asDate(data.periodEndExclusive);
  const isCalendar = /^month:\d{4}-\d{2}$/.test(periodKey) && periodBasis === "CALENDAR_MONTH";
  const isSalary = /^salary:\d{4}-\d{2}-\d{2}$/.test(periodKey) && periodBasis === "SALARY_CYCLE";

  if ((isCalendar || isSalary) && start && endExclusive && endExclusive > start) {
    return {
      categoryId: data.categoryId,
      periodKey,
      start,
      endExclusive,
      basis: periodBasis,
    };
  }

  if (typeof data.month !== "string" || !/^\d{4}-\d{2}$/.test(data.month)) return null;
  const legacyMonth = data.month;
  const period = resolveFinancialPeriod(
    new Date(`${legacyMonth}-15T12:00:00.000Z`),
    {...salaryConfig, enabled: false, budgetPeriodBasis: "CALENDAR_MONTH"},
  );
  return {
    categoryId: data.categoryId,
    periodKey: period.key,
    start: period.start,
    endExclusive: period.endExclusive,
    basis: period.basis,
  };
}

export function budgetClientContractSignature(data: BudgetDocument | undefined): string {
  return JSON.stringify([
    data?.categoryId ?? null,
    data?.periodKey ?? null,
    millis(data?.periodStart),
    millis(data?.periodEndExclusive),
    data?.periodBasis ?? null,
    data?.month ?? null,
    data?.limitAmount ?? null,
  ]);
}

function asDate(value: unknown): Date | null {
  if (value instanceof Date && Number.isFinite(value.getTime())) return value;
  if (typeof value === "number" && Number.isFinite(value)) return new Date(value);
  if (value && typeof value === "object" && "toDate" in value) {
    const toDate = (value as {toDate?: unknown}).toDate;
    if (typeof toDate === "function") {
      const date = toDate.call(value) as unknown;
      return date instanceof Date && Number.isFinite(date.getTime()) ? date : null;
    }
  }
  return null;
}

function millis(value: unknown): number | null {
  return asDate(value)?.getTime() ?? null;
}
