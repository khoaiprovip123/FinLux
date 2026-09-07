export const DEFAULT_FINANCE_TIME_ZONE = "Asia/Ho_Chi_Minh";

export type PaydayRuleType = "DAY_OF_MONTH" | "FIRST_DAY_OF_MONTH" | "LAST_DAY_OF_MONTH";
export type BudgetPeriodBasis = "CALENDAR_MONTH" | "SALARY_CYCLE";

export type SalaryCycleConfig = {
  enabled: boolean;
  paydayRuleType: PaydayRuleType;
  paydayDay: number;
  salaryWalletId?: string | null;
  savingsWalletId?: string | null;
  expectedSalary?: number | null;
  rolloverRule?: "KEEP_IN_WALLET" | "MOVE_TO_SAVINGS" | "ASK_EACH_CYCLE";
  budgetPeriodBasis: BudgetPeriodBasis;
  financeTimeZone: string;
};

export type FinancialPeriod = {
  key: string;
  start: Date;
  endExclusive: Date;
  basis: BudgetPeriodBasis;
};

type LocalDate = {year: number; month: number; day: number};
type ZonedParts = LocalDate & {hour: number; minute: number; second: number};

const DEFAULT_CONFIG: SalaryCycleConfig = {
  enabled: false,
  paydayRuleType: "DAY_OF_MONTH",
  paydayDay: 25,
  budgetPeriodBasis: "CALENDAR_MONTH",
  financeTimeZone: DEFAULT_FINANCE_TIME_ZONE,
};

export function normalizeSalaryCycleConfig(data: Record<string, unknown> | undefined): SalaryCycleConfig {
  const legacyBaseDay = integerInRange(data?.baseDay, 1, 31);
  return {
    enabled: typeof data?.enabled === "boolean" ? data.enabled : DEFAULT_CONFIG.enabled,
    paydayRuleType: isPaydayRuleType(data?.paydayRuleType) ? data.paydayRuleType : DEFAULT_CONFIG.paydayRuleType,
    paydayDay: integerInRange(data?.paydayDay, 1, 31) ?? legacyBaseDay ?? DEFAULT_CONFIG.paydayDay,
    salaryWalletId: nullableString(data?.salaryWalletId),
    savingsWalletId: nullableString(data?.savingsWalletId),
    expectedSalary: typeof data?.expectedSalary === "number" ? data.expectedSalary : null,
    rolloverRule: isRolloverRule(data?.rolloverRule) ? data.rolloverRule : "KEEP_IN_WALLET",
    budgetPeriodBasis: isBudgetPeriodBasis(data?.budgetPeriodBasis) ? data.budgetPeriodBasis : DEFAULT_CONFIG.budgetPeriodBasis,
    financeTimeZone: validTimeZone(data?.financeTimeZone),
  };
}

export function resolveFinancialPeriod(instant: Date, config: SalaryCycleConfig): FinancialPeriod {
  const timeZone = validTimeZone(config.financeTimeZone);
  const local = zonedParts(instant, timeZone);

  if (!config.enabled || config.budgetPeriodBasis === "CALENDAR_MONTH") {
    const startDate = {year: local.year, month: local.month, day: 1};
    const endDate = shiftMonth(startDate, 1);
    return {
      key: `month:${formatYearMonth(startDate)}`,
      start: localMidnightToInstant(startDate, timeZone),
      endExclusive: localMidnightToInstant(endDate, timeZone),
      basis: "CALENDAR_MONTH",
    };
  }

  const currentBoundaryDate = boundaryDate(local.year, local.month, config);
  const currentBoundary = localMidnightToInstant(currentBoundaryDate, timeZone);
  const startMonth = instant.getTime() >= currentBoundary.getTime()
    ? currentBoundaryDate
    : shiftMonth(currentBoundaryDate, -1);
  const startDate = boundaryDate(startMonth.year, startMonth.month, config);
  const nextMonth = shiftMonth(startDate, 1);
  const endDate = boundaryDate(nextMonth.year, nextMonth.month, config);

  return {
    key: `salary:${formatLocalDate(startDate)}`,
    start: localMidnightToInstant(startDate, timeZone),
    endExclusive: localMidnightToInstant(endDate, timeZone),
    basis: "SALARY_CYCLE",
  };
}

export function sameLocalDate(first: Date, second: Date, timeZone: string): boolean {
  const firstParts = zonedParts(first, validTimeZone(timeZone));
  const secondParts = zonedParts(second, validTimeZone(timeZone));
  return firstParts.year === secondParts.year && firstParts.month === secondParts.month && firstParts.day === secondParts.day;
}

function boundaryDate(year: number, month: number, config: SalaryCycleConfig): LocalDate {
  const maxDay = daysInMonth(year, month);
  const day = config.paydayRuleType === "FIRST_DAY_OF_MONTH"
    ? 1
    : config.paydayRuleType === "LAST_DAY_OF_MONTH"
      ? maxDay
      : Math.min(Math.max(config.paydayDay, 1), maxDay);
  return {year, month, day};
}

function shiftMonth(date: LocalDate, delta: number): LocalDate {
  const shifted = new Date(Date.UTC(date.year, date.month - 1 + delta, 1));
  return {year: shifted.getUTCFullYear(), month: shifted.getUTCMonth() + 1, day: 1};
}

function daysInMonth(year: number, month: number): number {
  return new Date(Date.UTC(year, month, 0)).getUTCDate();
}

function localMidnightToInstant(date: LocalDate, timeZone: string): Date {
  const targetUtcMillis = Date.UTC(date.year, date.month - 1, date.day, 0, 0, 0, 0);
  let candidateMillis = targetUtcMillis;
  for (let attempt = 0; attempt < 4; attempt += 1) {
    const parts = zonedParts(new Date(candidateMillis), timeZone);
    const representedAsUtc = Date.UTC(parts.year, parts.month - 1, parts.day, parts.hour, parts.minute, parts.second, 0);
    const nextCandidate = targetUtcMillis - (representedAsUtc - candidateMillis);
    if (nextCandidate === candidateMillis) break;
    candidateMillis = nextCandidate;
  }
  return new Date(candidateMillis);
}

function zonedParts(date: Date, timeZone: string): ZonedParts {
  const formatter = new Intl.DateTimeFormat("en-CA", {
    timeZone,
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
    hourCycle: "h23",
  });
  const values = Object.fromEntries(
    formatter.formatToParts(date)
      .filter((part) => part.type !== "literal")
      .map((part) => [part.type, Number(part.value)]),
  );
  return {
    year: values.year,
    month: values.month,
    day: values.day,
    hour: values.hour,
    minute: values.minute,
    second: values.second,
  };
}

function validTimeZone(value: unknown): string {
  if (typeof value !== "string" || value.length === 0 || value.length > 64) return DEFAULT_FINANCE_TIME_ZONE;
  try {
    new Intl.DateTimeFormat("en-US", {timeZone: value}).format();
    return value;
  } catch {
    return DEFAULT_FINANCE_TIME_ZONE;
  }
}

function integerInRange(value: unknown, min: number, max: number): number | undefined {
  return typeof value === "number" && Number.isInteger(value) && value >= min && value <= max ? value : undefined;
}

function nullableString(value: unknown): string | null {
  return typeof value === "string" && value.length > 0 ? value : null;
}

function isPaydayRuleType(value: unknown): value is PaydayRuleType {
  return value === "DAY_OF_MONTH" || value === "FIRST_DAY_OF_MONTH" || value === "LAST_DAY_OF_MONTH";
}

function isBudgetPeriodBasis(value: unknown): value is BudgetPeriodBasis {
  return value === "CALENDAR_MONTH" || value === "SALARY_CYCLE";
}

function isRolloverRule(value: unknown): value is NonNullable<SalaryCycleConfig["rolloverRule"]> {
  return value === "KEEP_IN_WALLET" || value === "MOVE_TO_SAVINGS" || value === "ASK_EACH_CYCLE";
}

function formatYearMonth(date: LocalDate): string {
  return `${String(date.year).padStart(4, "0")}-${String(date.month).padStart(2, "0")}`;
}

function formatLocalDate(date: LocalDate): string {
  return `${formatYearMonth(date)}-${String(date.day).padStart(2, "0")}`;
}
