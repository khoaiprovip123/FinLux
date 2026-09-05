export type PaydayRuleType = "DAY_OF_MONTH" | "FIRST_DAY_OF_MONTH" | "LAST_DAY_OF_MONTH";
export type BudgetPeriodBasis = "CALENDAR_MONTH" | "SALARY_CYCLE";

export type SalaryCycleConfig = {
  enabled?: boolean;
  paydayRuleType?: PaydayRuleType;
  paydayDay?: number;
  budgetPeriodBasis?: BudgetPeriodBasis;
  financeTimeZone?: string;
};

export type ResolvedFinancialPeriod = {
  key: string;
  start: Date;
  end: Date;
  basis: BudgetPeriodBasis;
};

type ZonedParts = {
  year: number;
  month: number;
  day: number;
  hour: number;
  minute: number;
  second: number;
};

const DEFAULT_TIME_ZONE = "Asia/Ho_Chi_Minh";

function safeTimeZone(raw?: string): string {
  const candidate = raw?.trim() || DEFAULT_TIME_ZONE;
  try {
    new Intl.DateTimeFormat("en-US", {timeZone: candidate}).format(new Date());
    return candidate;
  } catch {
    return DEFAULT_TIME_ZONE;
  }
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
  const parts = Object.fromEntries(
    formatter.formatToParts(date)
      .filter((part) => part.type !== "literal")
      .map((part) => [part.type, part.value]),
  );
  return {
    year: Number(parts.year),
    month: Number(parts.month),
    day: Number(parts.day),
    hour: Number(parts.hour),
    minute: Number(parts.minute),
    second: Number(parts.second),
  };
}

function timeZoneOffsetMillis(date: Date, timeZone: string): number {
  const parts = zonedParts(date, timeZone);
  const representedAsUtc = Date.UTC(
    parts.year,
    parts.month - 1,
    parts.day,
    parts.hour,
    parts.minute,
    parts.second,
  );
  return representedAsUtc - Math.floor(date.getTime() / 1000) * 1000;
}

function zonedMidnightUtc(year: number, month: number, day: number, timeZone: string): Date {
  const utcWallClock = Date.UTC(year, month - 1, day, 0, 0, 0);
  let candidate = new Date(utcWallClock);
  let offset = timeZoneOffsetMillis(candidate, timeZone);
  candidate = new Date(utcWallClock - offset);

  // Re-evaluate once to handle zones whose offset changes near the requested date.
  const adjustedOffset = timeZoneOffsetMillis(candidate, timeZone);
  if (adjustedOffset !== offset) {
    offset = adjustedOffset;
    candidate = new Date(utcWallClock - offset);
  }
  return candidate;
}

function shiftMonth(year: number, month: number, delta: number): {year: number; month: number} {
  const shifted = new Date(Date.UTC(year, month - 1 + delta, 1));
  return {year: shifted.getUTCFullYear(), month: shifted.getUTCMonth() + 1};
}

function daysInMonth(year: number, month: number): number {
  return new Date(Date.UTC(year, month, 0)).getUTCDate();
}

function salaryBoundary(
  year: number,
  month: number,
  config: SalaryCycleConfig,
  timeZone: string,
): Date {
  const rule = config.paydayRuleType ?? "DAY_OF_MONTH";
  const maxDay = daysInMonth(year, month);
  const configuredDay = Math.min(Math.max(config.paydayDay ?? 1, 1), maxDay);
  const day = rule === "FIRST_DAY_OF_MONTH"
    ? 1
    : rule === "LAST_DAY_OF_MONTH"
      ? maxDay
      : configuredDay;
  return zonedMidnightUtc(year, month, day, timeZone);
}

function formatKeyDate(date: Date, timeZone: string): string {
  const parts = zonedParts(date, timeZone);
  return `${parts.year}-${String(parts.month).padStart(2, "0")}-${String(parts.day).padStart(2, "0")}`;
}

export function resolveFinancialPeriod(
  date: Date,
  config: SalaryCycleConfig,
): ResolvedFinancialPeriod {
  const timeZone = safeTimeZone(config.financeTimeZone);
  const local = zonedParts(date, timeZone);
  const basis = config.enabled && config.budgetPeriodBasis === "SALARY_CYCLE"
    ? "SALARY_CYCLE"
    : "CALENDAR_MONTH";

  if (basis === "CALENDAR_MONTH") {
    const next = shiftMonth(local.year, local.month, 1);
    const start = zonedMidnightUtc(local.year, local.month, 1, timeZone);
    const end = zonedMidnightUtc(next.year, next.month, 1, timeZone);
    return {
      key: `month:${local.year}-${String(local.month).padStart(2, "0")}`,
      start,
      end,
      basis,
    };
  }

  const currentMonthBoundary = salaryBoundary(local.year, local.month, config, timeZone);
  const startMonth = date.getTime() >= currentMonthBoundary.getTime()
    ? {year: local.year, month: local.month}
    : shiftMonth(local.year, local.month, -1);
  const nextMonth = shiftMonth(startMonth.year, startMonth.month, 1);
  const start = salaryBoundary(startMonth.year, startMonth.month, config, timeZone);
  const end = salaryBoundary(nextMonth.year, nextMonth.month, config, timeZone);

  return {
    key: `salary:${formatKeyDate(start, timeZone)}`,
    start,
    end,
    basis,
  };
}

export function resolvePreviousFinancialPeriod(
  period: ResolvedFinancialPeriod,
  config: SalaryCycleConfig,
): ResolvedFinancialPeriod {
  return resolveFinancialPeriod(new Date(period.start.getTime() - 1), config);
}

export function isPeriodBoundaryDate(date: Date, period: ResolvedFinancialPeriod, config: SalaryCycleConfig): boolean {
  const timeZone = safeTimeZone(config.financeTimeZone);
  const a = zonedParts(date, timeZone);
  const b = zonedParts(period.start, timeZone);
  return a.year === b.year && a.month === b.month && a.day === b.day;
}
