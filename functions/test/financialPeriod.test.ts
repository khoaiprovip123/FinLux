import assert from "node:assert/strict";
import {
  resolveFinancialPeriod,
  resolvePreviousFinancialPeriod,
  type SalaryCycleConfig,
} from "../src/financialPeriod.js";

const HCM = "Asia/Ho_Chi_Minh";

function hcmInstant(year: number, month: number, day: number, hour = 12): Date {
  // Asia/Ho_Chi_Minh is UTC+7 year-round.
  return new Date(Date.UTC(year, month - 1, day, hour - 7, 0, 0));
}

function localDate(date: Date): string {
  return new Intl.DateTimeFormat("en-CA", {
    timeZone: HCM,
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  }).format(date);
}

describe("Financial period resolver", () => {
  it("resolves payday 25 from 25 Aug through before 25 Sep", () => {
    const config: SalaryCycleConfig = {
      enabled: true,
      paydayRuleType: "DAY_OF_MONTH",
      paydayDay: 25,
      budgetPeriodBasis: "SALARY_CYCLE",
      financeTimeZone: HCM,
    };
    const period = resolveFinancialPeriod(hcmInstant(2026, 9, 10), config);
    assert.equal(period.key, "salary:2026-08-25");
    assert.equal(localDate(period.start), "2026-08-25");
    assert.equal(localDate(period.end), "2026-09-25");
  });

  it("clamps payday 31 to February final valid day", () => {
    const config: SalaryCycleConfig = {
      enabled: true,
      paydayRuleType: "DAY_OF_MONTH",
      paydayDay: 31,
      budgetPeriodBasis: "SALARY_CYCLE",
      financeTimeZone: HCM,
    };
    const period = resolveFinancialPeriod(hcmInstant(2027, 2, 28), config);
    assert.equal(localDate(period.start), "2027-02-28");
    assert.equal(localDate(period.end), "2027-03-31");
  });

  it("supports first day rule", () => {
    const period = resolveFinancialPeriod(hcmInstant(2026, 8, 14), {
      enabled: true,
      paydayRuleType: "FIRST_DAY_OF_MONTH",
      paydayDay: 25,
      budgetPeriodBasis: "SALARY_CYCLE",
      financeTimeZone: HCM,
    });
    assert.equal(localDate(period.start), "2026-08-01");
    assert.equal(localDate(period.end), "2026-09-01");
  });

  it("supports last day rule across unequal month lengths", () => {
    const period = resolveFinancialPeriod(hcmInstant(2026, 9, 15), {
      enabled: true,
      paydayRuleType: "LAST_DAY_OF_MONTH",
      budgetPeriodBasis: "SALARY_CYCLE",
      financeTimeZone: HCM,
    });
    assert.equal(localDate(period.start), "2026-08-31");
    assert.equal(localDate(period.end), "2026-09-30");
  });

  it("keeps budget on calendar month when budgetPeriodBasis is calendar", () => {
    const period = resolveFinancialPeriod(hcmInstant(2026, 9, 10), {
      enabled: true,
      paydayRuleType: "DAY_OF_MONTH",
      paydayDay: 25,
      budgetPeriodBasis: "CALENDAR_MONTH",
      financeTimeZone: HCM,
    });
    assert.equal(period.key, "month:2026-09");
    assert.equal(localDate(period.start), "2026-09-01");
    assert.equal(localDate(period.end), "2026-10-01");
  });

  it("resolves the immediately preceding salary period without a 15-day heuristic", () => {
    const config: SalaryCycleConfig = {
      enabled: true,
      paydayRuleType: "DAY_OF_MONTH",
      paydayDay: 25,
      budgetPeriodBasis: "SALARY_CYCLE",
      financeTimeZone: HCM,
    };
    const current = resolveFinancialPeriod(hcmInstant(2026, 9, 30), config);
    const previous = resolvePreviousFinancialPeriod(current, config);
    assert.equal(previous.key, "salary:2026-08-25");
    assert.equal(localDate(previous.start), "2026-08-25");
    assert.equal(localDate(previous.end), "2026-09-25");
  });
});
