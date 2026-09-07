import {strict as assert} from "node:assert";
import {readFileSync} from "node:fs";
import {resolve} from "node:path";
import {normalizeSalaryCycleConfig, resolveFinancialPeriod} from "../src/financialPeriod.js";

type ContractVector = {
  name: string;
  enabled: boolean;
  budgetPeriodBasis: "CALENDAR_MONTH" | "SALARY_CYCLE";
  paydayRuleType: "DAY_OF_MONTH" | "FIRST_DAY_OF_MONTH" | "LAST_DAY_OF_MONTH";
  paydayDay: number;
  financeTimeZone: string;
  instant: string;
  expectedKey: string;
  expectedStart: string;
  expectedEndExclusive: string;
};

function loadVectors(): ContractVector[] {
  const fixture = resolve(process.cwd(), "..", "contracts", "financial-period-vectors.tsv");
  const [header, ...rows] = readFileSync(fixture, "utf8").trim().split(/\r?\n/);
  const keys = header.split("\t") as Array<keyof ContractVector>;
  return rows.map((row) => {
    const raw = Object.fromEntries(keys.map((key, index) => [key, row.split("\t")[index]]));
    return {
      ...raw,
      enabled: raw.enabled === "true",
      paydayDay: Number(raw.paydayDay),
    } as ContractVector;
  });
}

describe("financial period shared contract", () => {
  for (const vector of loadVectors()) {
    it(vector.name, () => {
      const config = normalizeSalaryCycleConfig(vector);
      const actual = resolveFinancialPeriod(new Date(vector.instant), config);
      assert.equal(actual.key, vector.expectedKey);
      assert.equal(actual.start.toISOString(), vector.expectedStart);
      assert.equal(actual.endExclusive.toISOString(), vector.expectedEndExclusive);
      assert.equal(actual.basis, vector.budgetPeriodBasis);
    });
  }

  it("maps legacy baseDay while current fields take precedence", () => {
    assert.equal(normalizeSalaryCycleConfig({baseDay: 5}).paydayDay, 5);
    assert.equal(normalizeSalaryCycleConfig({baseDay: 5, paydayDay: 25}).paydayDay, 25);
  });

  it("uses the documented payday 25 default", () => {
    assert.equal(normalizeSalaryCycleConfig(undefined).paydayDay, 25);
  });

  it("falls back to the finance default for invalid timezones", () => {
    assert.equal(normalizeSalaryCycleConfig({financeTimeZone: "Not/A_Zone"}).financeTimeZone, "Asia/Ho_Chi_Minh");
  });
});
