import { assertFails, assertSucceeds, initializeTestEnvironment } from "@firebase/rules-unit-testing";
import type { RulesTestEnvironment } from "@firebase/rules-unit-testing";
import * as fs from "fs";
import * as path from "path";

let testEnv: RulesTestEnvironment;

before(async () => {
    const rulesPath = fs.existsSync("firestore.rules") 
        ? "firestore.rules" 
        : fs.existsSync("../firestore.rules") 
            ? "../firestore.rules" 
            : path.resolve(process.cwd(), "firestore.rules");
    const host = process.env.FIRESTORE_EMULATOR_HOST ? process.env.FIRESTORE_EMULATOR_HOST.split(":")[0] : "127.0.0.1";
    const port = process.env.FIRESTORE_EMULATOR_HOST ? parseInt(process.env.FIRESTORE_EMULATOR_HOST.split(":")[1], 10) : 8080;

    testEnv = await initializeTestEnvironment({
        projectId: "finlux-test",
        firestore: {
            host,
            port,
            rules: fs.readFileSync(rulesPath, "utf8"),
        },
    });
});

after(async () => {
    if (testEnv) {
        await testEnv.cleanup();
    }
});

beforeEach(async () => {
    await testEnv.clearFirestore();
});

describe("Firestore Rules: Wallets", () => {
    const expense = {
        type: "expense",
        amount: 500,
        walletId: "w1",
        categoryId: "food",
        note: "Lunch",
        receiptImageUrl: null,
        date: new Date(),
        createdAt: new Date(),
        updatedAt: new Date(),
    };

    async function seedWallet(balance = 1000, lastTransactionId?: string) {
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().doc("users/alice/wallets/w1").set({
                name: "Cash",
                type: "cash",
                balance,
                color: "#000000",
                isDefault: true,
                createdAt: new Date(),
                ...(lastTransactionId ? {lastTransactionId} : {}),
            });
        });
    }

    it("rejects balance changes without a real ledger transition", async () => {
        const alice = testEnv.authenticatedContext("alice");
        await seedWallet();

        const ref = alice.firestore().doc("users/alice/wallets/w1");
        await assertFails(ref.update({balance: 500}));
        await assertFails(ref.update({balance: 500, lastTransactionId: "fake_txn"}));
    });

    it("rejects a stale transaction id and a no-op ledger write", async () => {
        const db = testEnv.authenticatedContext("alice").firestore();
        await seedWallet();
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().doc("users/alice/transactions/stale_txn").set(expense);
        });

        await assertFails(db.doc("users/alice/wallets/w1").update({
            balance: 500,
            lastTransactionId: "stale_txn",
        }));

        const batch = db.batch();
        batch.set(db.doc("users/alice/transactions/stale_txn"), expense);
        batch.update(db.doc("users/alice/wallets/w1"), {
            balance: 500,
            lastTransactionId: "stale_txn",
        });
        await assertFails(batch.commit());
    });

    it("rejects using a metadata-only ledger edit to move a wallet balance", async () => {
        const db = testEnv.authenticatedContext("alice").firestore();
        await seedWallet(500, "txn1");
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().doc("users/alice/transactions/txn1").set(expense);
        });
        const batch = db.batch();
        batch.update(db.doc("users/alice/transactions/txn1"), {note: "Changed note"});
        batch.update(db.doc("users/alice/wallets/w1"), {balance: 900, lastTransactionId: "txn1"});
        await assertFails(batch.commit());
    });

    it("allows an atomic expense create with the exact wallet delta", async () => {
        const db = testEnv.authenticatedContext("alice").firestore();
        await seedWallet();
        const batch = db.batch();
        batch.set(db.doc("users/alice/transactions/txn1"), expense);
        batch.update(db.doc("users/alice/wallets/w1"), {
            balance: 500,
            lastTransactionId: "txn1",
        });

        await assertSucceeds(batch.commit());
    });

    it("rejects an atomic transaction when the wallet delta is wrong", async () => {
        const db = testEnv.authenticatedContext("alice").firestore();
        await seedWallet();
        const batch = db.batch();
        batch.set(db.doc("users/alice/transactions/txn1"), expense);
        batch.update(db.doc("users/alice/wallets/w1"), {
            balance: 600,
            lastTransactionId: "txn1",
        });

        await assertFails(batch.commit());
    });

    it("allows an atomic expense delete with the reverse wallet delta", async () => {
        const db = testEnv.authenticatedContext("alice").firestore();
        await seedWallet(500, "txn1");
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().doc("users/alice/transactions/txn1").set(expense);
        });
        const batch = db.batch();
        batch.delete(db.doc("users/alice/transactions/txn1"));
        batch.update(db.doc("users/alice/wallets/w1"), {
            balance: 1000,
            lastTransactionId: "txn1",
        });

        await assertSucceeds(batch.commit());
    });

    it("allows paired transfer legs and rejects linking an unrelated wallet", async () => {
        const db = testEnv.authenticatedContext("alice").firestore();
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().doc("users/alice/wallets/w1").set({balance: 1000});
            await context.firestore().doc("users/alice/wallets/w2").set({balance: 100});
        });
        const occurredAt = new Date();
        const validBatch = db.batch();
        validBatch.set(db.doc("users/alice/transactions/transfer_out"), {
            ...expense,
            type: "transfer_out",
            amount: 400,
            categoryId: null,
            walletId: "w1",
            relatedWalletId: "w2",
            date: occurredAt,
        });
        validBatch.set(db.doc("users/alice/transactions/transfer_in"), {
            ...expense,
            type: "transfer_in",
            amount: 400,
            categoryId: null,
            walletId: "w2",
            relatedWalletId: "w1",
            date: occurredAt,
        });
        validBatch.update(db.doc("users/alice/wallets/w1"), {balance: 600, lastTransactionId: "transfer_out"});
        validBatch.update(db.doc("users/alice/wallets/w2"), {balance: 500, lastTransactionId: "transfer_in"});
        await assertSucceeds(validBatch.commit());

        const invalidBatch = db.batch();
        invalidBatch.set(db.doc("users/alice/transactions/txn_unrelated"), {...expense, amount: 100});
        invalidBatch.update(db.doc("users/alice/wallets/w1"), {balance: 500, lastTransactionId: "txn_unrelated"});
        invalidBatch.update(db.doc("users/alice/wallets/w2"), {balance: 900, lastTransactionId: "txn_unrelated"});
        await assertFails(invalidBatch.commit());
    });
});

describe("Firestore Rules: Salary Rollovers", () => {
    it("should reject double writing for the same cycleKey", async () => {
        const alice = testEnv.authenticatedContext("alice");
        const docId = "cycle_2025-08-01";
        const ref = alice.firestore().doc(`users/alice/salaryRollovers/${docId}`);

        // Write first time -> SUCCEED
        await assertSucceeds(ref.set({
            cycleKey: "salary:2025-08-01",
            processedAt: new Date()
        }));

        // 3. Ghi salaryRollovers 2 lần cùng một cycleKey -> TỪ CHỐI
        await assertFails(ref.set({
            cycleKey: "salary:2025-08-01",
            processedAt: new Date()
        }));
    });
});

describe("Firestore Rules: Budgets", () => {
    const modernBudget = {
        categoryId: "food",
        periodKey: "salary:2026-08-25",
        periodStart: new Date("2026-08-24T17:00:00.000Z"),
        periodEndExclusive: new Date("2026-09-24T17:00:00.000Z"),
        periodBasis: "SALARY_CYCLE",
        month: null,
        limitAmount: 1000000,
        spentAmount: 0,
        notified80: false,
        notified100: false,
    };

    it("allows owner to create the modern salary-period schema and denies cross-user access", async () => {
        const alice = testEnv.authenticatedContext("alice");
        const bob = testEnv.authenticatedContext("bob");
        const ref = alice.firestore().doc("users/alice/budgets/food_salary:2026-08-25");

        await assertSucceeds(ref.set(modernBudget));
        await assertSucceeds(ref.get());
        await assertFails(bob.firestore().doc(ref.path).get());
    });

    it("keeps legacy calendar-month documents compatible", async () => {
        const alice = testEnv.authenticatedContext("alice");
        await assertSucceeds(alice.firestore().doc("users/alice/budgets/food_2026-08").set({
            categoryId: "food",
            month: "2026-08",
            limitAmount: 1000000,
            spentAmount: 0,
            notified80: false,
            notified100: false,
        }));
    });

    it("rejects malformed period contracts and unknown fields", async () => {
        const alice = testEnv.authenticatedContext("alice");
        const ref = alice.firestore().doc("users/alice/budgets/food_salary:2026-08-25");

        await assertFails(ref.set({...modernBudget, periodKey: "month:2026-08"}));
        await assertFails(ref.set({...modernBudget, periodEndExclusive: modernBudget.periodStart}));
        await assertFails(ref.set({...modernBudget, injected: true}));
        await assertFails(ref.set({...modernBudget, spentAmount: 1}));
        await assertFails(ref.set({...modernBudget, notified80: true}));
        await assertFails(ref.set({...modernBudget, notified100: true}));
    });

    it("should reject direct spentAmount modification", async () => {
        const alice = testEnv.authenticatedContext("alice");
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().doc("users/alice/budgets/b1").set({
                ...modernBudget,
                periodKey: "month:2026-08",
                periodStart: new Date("2026-07-31T17:00:00.000Z"),
                periodEndExclusive: new Date("2026-08-31T17:00:00.000Z"),
                periodBasis: "CALENDAR_MONTH",
            });
        });

        const ref = alice.firestore().doc("users/alice/budgets/b1");
        
        // Sửa budget.spentAmount tự do -> TỪ CHỐI
        await assertFails(ref.update({
            spentAmount: 500
        }));

        // Sửa fields khác thì OK (ví dụ limitAmount)
        await assertSucceeds(ref.update({
            limitAmount: 2000
        }));

        await assertFails(ref.update({
            notified80: true
        }));

        await assertFails(ref.update({
            periodKey: "month:2026-09"
        }));
    });
});

describe("Firestore Rules: Saving Spin", () => {
    const validConfig = {
        enabled: true,
        showOnHome: true,
        minAmount: 10000,
        maxAmount: 100000,
        stepAmount: 5000,
        slotCount: 8,
        frequency: "DAILY",
        selectedWeekdays: [],
        weeklyDay: 1,
        reminderEnabled: true,
        reminderHour: 9,
        reminderMinute: 0,
        snoozeEnabled: true,
        allowSkip: true,
        defaultDestinationId: null,
        createdAt: new Date(),
        updatedAt: new Date(),
    };

    it("allows owner config and denies another user", async () => {
        const alice = testEnv.authenticatedContext("alice");
        const bob = testEnv.authenticatedContext("bob");
        const path = "users/alice/savingSpinConfigs/default";

        await assertSucceeds(alice.firestore().doc(path).set(validConfig));
        await assertSucceeds(alice.firestore().doc(path).get());
        await assertFails(bob.firestore().doc(path).get());
        await assertFails(bob.firestore().doc(path).set(validConfig));
    });

    it("prevents changing a result after it has been locked", async () => {
        const alice = testEnv.authenticatedContext("alice");
        const path = "users/alice/savingSpinSessions/day_2026-08-31";
        const locked = {
            scheduleKey: "day:2026-08-31",
            wheelValues: [10000, 15000, 20000, 25000, 30000, 35000],
            selectedIndex: 2,
            selectedAmount: 20000,
            status: "SPUN_PENDING",
            destinationId: null,
            method: null,
            spunAt: new Date(),
            completedAt: null,
            skippedAt: null,
            snoozedUntil: null,
            createdAt: new Date(),
            updatedAt: new Date(),
        };
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().doc(path).set(locked);
        });

        await assertFails(alice.firestore().doc(path).update({
            selectedIndex: 3,
            selectedAmount: 25000,
            updatedAt: new Date(),
        }));
        await assertSucceeds(alice.firestore().doc(path).update({
            status: "COMPLETED",
            destinationId: "piggy_cash",
            method: "CASH",
            completedAt: new Date(),
            updatedAt: new Date(),
        }));
    });
});

describe("Firestore Rules: Financial Goals", () => {
    it("allows creating a goal with non-negative amounts", async () => {
        const alice = testEnv.authenticatedContext("alice");
        await assertSucceeds(alice.firestore().doc("users/alice/goals/g1").set({
            name: "Emergency Fund",
            targetAmount: 10000000,
            savedAmount: 0,
        }));
    });

    it("rejects creating or updating a goal with negative savedAmount or targetAmount", async () => {
        const alice = testEnv.authenticatedContext("alice");
        await assertFails(alice.firestore().doc("users/alice/goals/g_neg").set({
            name: "Bad Goal",
            targetAmount: -500,
            savedAmount: 0,
        }));
        await assertFails(alice.firestore().doc("users/alice/goals/g_neg2").set({
            name: "Bad Goal 2",
            targetAmount: 1000,
            savedAmount: -100,
        }));
    });

    it("allows deleting a goal when savedAmount is 0", async () => {
        const alice = testEnv.authenticatedContext("alice");
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().doc("users/alice/goals/g_empty").set({
                name: "Empty Goal",
                savedAmount: 0,
                targetAmount: 5000000,
            });
        });
        await assertSucceeds(alice.firestore().doc("users/alice/goals/g_empty").delete());
    });

    it("rejects deleting a goal when savedAmount is greater than 0", async () => {
        const alice = testEnv.authenticatedContext("alice");
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().doc("users/alice/goals/g_has_money").set({
                name: "Money Goal",
                savedAmount: 1000000,
                targetAmount: 5000000,
            });
        });
        await assertFails(alice.firestore().doc("users/alice/goals/g_has_money").delete());
    });

    it("rejects direct savedAmount mutation and allows an atomic goal deposit", async () => {
        const db = testEnv.authenticatedContext("alice").firestore();
        const now = new Date();
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().doc("users/alice/goals/g_atomic").set({name: "Goal", targetAmount: 5000, savedAmount: 0});
            await context.firestore().doc("users/alice/wallets/w_goal").set({balance: 2000});
        });
        await assertFails(db.doc("users/alice/goals/g_atomic").update({savedAmount: 500}));

        const batch = db.batch();
        batch.set(db.doc("users/alice/transactions/goal_tx"), {
            type: "expense", amount: 500, walletId: "w_goal", categoryId: "savings", goalId: "g_atomic", date: now,
        });
        batch.update(db.doc("users/alice/wallets/w_goal"), {balance: 1500, lastTransactionId: "goal_tx"});
        batch.update(db.doc("users/alice/goals/g_atomic"), {savedAmount: 500, lastTransactionId: "goal_tx"});
        await assertSucceeds(batch.commit());
    });
});

describe("Firestore Rules: Debts & Payments", () => {
    it("allows creating a debt and rejects negative balance", async () => {
        const alice = testEnv.authenticatedContext("alice");
        await assertSucceeds(alice.firestore().doc("users/alice/debts/d1").set({
            name: "Credit Card Debt",
            totalAmount: 5000000,
            remainingBalance: 5000000,
            paymentCount: 0,
        }));
        await assertFails(alice.firestore().doc("users/alice/debts/d_bad").set({
            name: "Bad Debt",
            totalAmount: 5000000,
            remainingBalance: -1000,
            paymentCount: 0,
        }));
    });

    it("requires payment, debt, wallet and ledger to change atomically", async () => {
        const alice = testEnv.authenticatedContext("alice");
        const db = alice.firestore();
        const now = new Date();
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().doc("users/alice/debts/d1").set({
                name: "Debt 1",
                totalAmount: 5000000,
                remainingBalance: 5000000,
                paymentCount: 0,
                isSettled: false,
            });
            await context.firestore().doc("users/alice/wallets/w1").set({balance: 2000000});
        });

        await assertFails(db.doc("users/alice/debts/d1").update({remainingBalance: 4100000}));
        await assertFails(db.doc("users/alice/debts/d1/payments/p1").set({
            debtId: "d1",
            walletId: "w1",
            amount: 1000000,
            principalPaid: 900000,
            interestPaid: 100000,
            transactionId: "debt_tx",
        }));

        const batch = db.batch();
        batch.set(db.doc("users/alice/transactions/debt_tx"), {
            type: "expense", amount: 1000000, walletId: "w1", categoryId: "debt_payment",
            debtId: "d1", debtPaymentId: "p1", date: now,
        });
        batch.update(db.doc("users/alice/wallets/w1"), {balance: 1000000, lastTransactionId: "debt_tx"});
        batch.set(db.doc("users/alice/debts/d1/payments/p1"), {
            debtId: "d1", walletId: "w1", transactionId: "debt_tx",
            amount: 1000000,
            principalPaid: 900000, interestPaid: 100000,
        });
        batch.update(db.doc("users/alice/debts/d1"), {
            remainingBalance: 4100000, isSettled: false, paymentCount: 1, lastPaymentId: "p1",
        });
        await assertSucceeds(batch.commit());

        await assertFails(db.doc("users/alice/debts/d1").delete());
    });
});

describe("Firestore Rules: Deals & Lending", () => {
    it("allows owner to create and update valid deal, denies cross-user", async () => {
        const alice = testEnv.authenticatedContext("alice");
        const bob = testEnv.authenticatedContext("bob");
        const now = new Date();

        // Alice creates deal
        await assertSucceeds(alice.firestore().doc("users/alice/deals/deal1").set({
            title: "Lướt sóng iPhone",
            description: "Mua đi bán lại",
            category: "investment",
            targetAmount: 50000000,
            totalCapitalOutlay: 0,
            totalRecovered: 0,
            netProfitLoss: 0,
            status: "active",
            startDate: now,
            endDate: null,
            createdAt: now,
            updatedAt: now,
        }));

        // Bob cannot read or write Alice's deal
        await assertFails(bob.firestore().doc("users/alice/deals/deal1").get());
        await assertFails(bob.firestore().doc("users/alice/deals/deal1").set({
            title: "Hacked Deal",
            category: "investment",
            targetAmount: 1000,
            totalCapitalOutlay: 0,
            totalRecovered: 0,
            netProfitLoss: 0,
            status: "active",
            startDate: now,
            createdAt: now,
            updatedAt: now,
        }));
    });

    it("rejects deal with negative capital or invalid category", async () => {
        const alice = testEnv.authenticatedContext("alice");
        const now = new Date();

        await assertFails(alice.firestore().doc("users/alice/deals/deal_bad1").set({
            title: "Bad Deal",
            category: "invalid_category",
            targetAmount: 50000000,
            totalCapitalOutlay: 0,
            totalRecovered: 0,
            netProfitLoss: 0,
            status: "active",
            startDate: now,
            createdAt: now,
            updatedAt: now,
        }));

        await assertFails(alice.firestore().doc("users/alice/deals/deal_bad2").set({
            title: "Bad Deal 2",
            category: "investment",
            targetAmount: 50000000,
            totalCapitalOutlay: -10000,
            totalRecovered: 0,
            netProfitLoss: 0,
            status: "active",
            startDate: now,
            createdAt: now,
            updatedAt: now,
        }));
    });

    it("allows atomic deal outlay with categoryId null and wallet delta", async () => {
        const alice = testEnv.authenticatedContext("alice");
        const now = new Date();

        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().doc("users/alice/wallets/w_deal").set({
                name: "Main Wallet",
                type: "bank",
                balance: 100000000,
                color: "#123456",
                isDefault: true,
                createdAt: now,
            });
            await context.firestore().doc("users/alice/deals/deal_outlay").set({
                title: "Deal Outlay Test",
                category: "investment",
                targetAmount: 50000000,
                totalCapitalOutlay: 0,
                totalRecovered: 0,
                netProfitLoss: 0,
                status: "active",
                startDate: now,
                createdAt: now,
                updatedAt: now,
            });
        });

        const batch = alice.firestore().batch();
        batch.set(alice.firestore().doc("users/alice/transactions/tx_outlay"), {
            type: "expense",
            amount: 20000000,
            categoryId: null,
            walletId: "w_deal",
            dealId: "deal_outlay",
            dealFlowType: "outlay_capital",
            date: now,
        });
        batch.update(alice.firestore().doc("users/alice/wallets/w_deal"), {
            balance: 80000000,
            lastTransactionId: "tx_outlay",
        });
        batch.update(alice.firestore().doc("users/alice/deals/deal_outlay"), {
            totalCapitalOutlay: 20000000,
            updatedAt: now,
            lastTransactionId: "tx_outlay",
        });

        await assertSucceeds(batch.commit());
    });

    it("allows atomic split deal inflow with counterpartTransactionId and combined wallet delta", async () => {
        const alice = testEnv.authenticatedContext("alice");
        const now = new Date();

        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().doc("users/alice/wallets/w_split").set({
                name: "Main Wallet",
                type: "bank",
                balance: 10000000,
                color: "#123456",
                isDefault: true,
                createdAt: now,
            });
            await context.firestore().doc("users/alice/deals/deal_split").set({
                title: "Deal Split Test",
                category: "investment",
                targetAmount: 50000000,
                totalCapitalOutlay: 20000000,
                totalRecovered: 0,
                netProfitLoss: 0,
                status: "active",
                startDate: now,
                createdAt: now,
                updatedAt: now,
            });
        });

        // Split inflow: 20M principal recovery + 5M capital gain = +25M wallet delta
        const batch = alice.firestore().batch();
        batch.set(alice.firestore().doc("users/alice/transactions/tx_p_rec"), {
            type: "income",
            amount: 20000000,
            categoryId: null,
            walletId: "w_split",
            dealId: "deal_split",
            dealFlowType: "principal_recovery",
            counterpartTransactionId: "tx_c_gain",
            date: now,
        });
        batch.set(alice.firestore().doc("users/alice/transactions/tx_c_gain"), {
            type: "income",
            amount: 5000000,
            categoryId: null,
            walletId: "w_split",
            dealId: "deal_split",
            dealFlowType: "capital_gain",
            counterpartTransactionId: "tx_p_rec",
            date: now,
        });
        batch.update(alice.firestore().doc("users/alice/wallets/w_split"), {
            balance: 35000000,
            lastTransactionId: "tx_c_gain",
        });
        batch.update(alice.firestore().doc("users/alice/deals/deal_split"), {
            totalRecovered: 20000000,
            netProfitLoss: 5000000,
            status: "completed",
            updatedAt: now,
            lastTransactionId: "tx_c_gain",
        });

        await assertSucceeds(batch.commit());
    });

    it("allows atomic stop-loss CAPITAL_LOSS transaction with no wallet delta and walletId null", async () => {
        const alice = testEnv.authenticatedContext("alice");
        const now = new Date();

        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().doc("users/alice/deals/deal_loss").set({
                title: "Deal Loss Test",
                category: "investment",
                targetAmount: 50000000,
                totalCapitalOutlay: 20000000,
                totalRecovered: 15000000,
                netProfitLoss: 0,
                status: "active",
                startDate: now,
                createdAt: now,
                updatedAt: now,
            });
        });

        // Close deal with loss: 5M loss recognized, no wallet delta, walletId null
        const batch = alice.firestore().batch();
        batch.set(alice.firestore().doc("users/alice/transactions/tx_stop_loss"), {
            type: "expense",
            amount: 5000000,
            categoryId: null,
            walletId: null,
            dealId: "deal_loss",
            dealFlowType: "capital_loss",
            date: now,
        });
        batch.update(alice.firestore().doc("users/alice/deals/deal_loss"), {
            netProfitLoss: -5000000,
            status: "completed",
            endDate: now,
            updatedAt: now,
            lastTransactionId: "tx_stop_loss",
        });

        await assertSucceeds(batch.commit());
    });

    it("rejects direct aggregate mutation and direct deal deletion", async () => {
        const db = testEnv.authenticatedContext("alice").firestore();
        const now = new Date();
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().doc("users/alice/deals/deal_locked").set({
                title: "Locked", category: "investment", targetAmount: 1000,
                totalCapitalOutlay: 500, totalRecovered: 0, netProfitLoss: 0,
                status: "active", startDate: now, createdAt: now, updatedAt: now,
            });
        });
        await assertFails(db.doc("users/alice/deals/deal_locked").update({totalRecovered: 500}));
        await assertFails(db.doc("users/alice/deals/deal_locked").delete());
    });
});
