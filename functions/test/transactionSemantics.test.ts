import assert from "node:assert/strict";
import {operatingExpenseAmount} from "../src/transactionSemantics.js";

describe("Transaction operating expense semantics", () => {
  it("keeps ordinary expense as budget consumption", () => {
    assert.equal(
      operatingExpenseAmount({
        type: "expense",
        amount: 500_000,
        categoryId: "food",
      }),
      500_000,
    );
  });

  it("excludes tagged goal allocation from budget consumption", () => {
    assert.equal(
      operatingExpenseAmount({
        type: "expense",
        amount: 1_000_000,
        categoryId: "savings",
        goalFlowType: "allocation",
      }),
      0,
    );
  });

  it("counts only interest for tagged debt payment", () => {
    assert.equal(
      operatingExpenseAmount({
        type: "expense",
        amount: 1_200_000,
        categoryId: "debt_payment",
        debtId: "debt-1",
        debtPrincipalAmount: 1_000_000,
        debtInterestAmount: 200_000,
      }),
      200_000,
    );
  });

  it("excludes investment capital outlay and accounting loss settlement", () => {
    assert.equal(
      operatingExpenseAmount({
        type: "expense",
        amount: 3_000_000,
        categoryId: "investment",
        dealFlowType: "outlay_capital",
      }),
      0,
    );
    assert.equal(
      operatingExpenseAmount({
        type: "expense",
        amount: 900_000,
        categoryId: "investment",
        dealFlowType: "capital_loss",
      }),
      0,
    );
  });

  it("preserves legacy untagged expense behavior", () => {
    assert.equal(
      operatingExpenseAmount({
        type: "EXPENSE",
        amount: 750_000,
        categoryId: "savings",
      }),
      750_000,
    );
  });
});
