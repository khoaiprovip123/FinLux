import assert from "node:assert/strict";
import {describe, it} from "mocha";
import {
  budgetClientContractSignature,
  resolveBudgetContract,
} from "../src/budgetContract.js";
import {SalaryCycleConfig} from "../src/financialPeriod.js";

const salaryConfig: SalaryCycleConfig = {
  enabled: true,
  paydayRuleType: "DAY_OF_MONTH",
  paydayDay: 25,
  budgetPeriodBasis: "SALARY_CYCLE",
  financeTimeZone: "Asia/Ho_Chi_Minh",
};

describe("Budget period contract", () => {
  it("reads modern Timestamp-like salary boundaries", () => {
    const start = new Date("2026-08-24T17:00:00.000Z");
    const end = new Date("2026-09-24T17:00:00.000Z");
    const result = resolveBudgetContract({
      categoryId: "food",
      periodKey: "salary:2026-08-25",
      periodStart: {toDate: () => start},
      periodEndExclusive: {toDate: () => end},
      periodBasis: "SALARY_CYCLE",
    }, salaryConfig);

    assert.equal(result?.periodKey, "salary:2026-08-25");
    assert.equal(result?.start.toISOString(), start.toISOString());
    assert.equal(result?.endExclusive.toISOString(), end.toISOString());
  });

  it("keeps epoch-millis modern boundaries readable during migration", () => {
    const result = resolveBudgetContract({
      categoryId: "food",
      periodKey: "month:2026-09",
      periodStart: Date.parse("2026-08-31T17:00:00.000Z"),
      periodEndExclusive: Date.parse("2026-09-30T17:00:00.000Z"),
      periodBasis: "CALENDAR_MONTH",
    }, salaryConfig);

    assert.equal(result?.basis, "CALENDAR_MONTH");
    assert.equal(result?.start.toISOString(), "2026-08-31T17:00:00.000Z");
  });

  it("resolves legacy month documents as calendar periods", () => {
    const result = resolveBudgetContract({categoryId: "food", month: "2026-09"}, salaryConfig);

    assert.equal(result?.periodKey, "month:2026-09");
    assert.equal(result?.start.toISOString(), "2026-08-31T17:00:00.000Z");
    assert.equal(result?.endExclusive.toISOString(), "2026-09-30T17:00:00.000Z");
  });

  it("rejects mismatched basis and invalid boundaries", () => {
    assert.equal(resolveBudgetContract({
      categoryId: "food",
      periodKey: "salary:2026-08-25",
      periodStart: 2,
      periodEndExclusive: 1,
      periodBasis: "CALENDAR_MONTH",
    }, salaryConfig), null);
  });

  it("ignores server-owned aggregate changes in the client contract signature", () => {
    const before = {categoryId: "food", periodKey: "month:2026-09", limitAmount: 1_000_000, spentAmount: 1};
    const after = {...before, spentAmount: 500_000, notified80: true};

    assert.equal(budgetClientContractSignature(before), budgetClientContractSignature(after));
  });
});
