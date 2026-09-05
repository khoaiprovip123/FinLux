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
    it("should reject wallet update if balance is changed without lastTransactionId", async () => {
        const alice = testEnv.authenticatedContext("alice");
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().doc("users/alice/wallets/w1").set({
                balance: 1000
            });
        });

        // 1. Ghi balance không kèm lastTransactionId -> TỪ CHỐI
        const ref = alice.firestore().doc("users/alice/wallets/w1");
        await assertFails(ref.update({
            balance: 500
        }));

        // 2. Kèm lastTransactionId -> THÀNH CÔNG
        await assertSucceeds(ref.update({
            balance: 500,
            lastTransactionId: "txn1"
        }));
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
    it("should reject direct spentAmount modification", async () => {
        const alice = testEnv.authenticatedContext("alice");
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().doc("users/alice/budgets/b1").set({
                spentAmount: 0,
                limitAmount: 1000
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


describe("Firestore Rules: Period Budgets", () => {
    it("allows the canonical period schema and keeps spentAmount server-owned", async () => {
        const alice = testEnv.authenticatedContext("alice");
        const ref = alice.firestore().doc("users/alice/budgets/food_salary:2026-08-25");

        await assertSucceeds(ref.set({
            categoryId: "food",
            periodKey: "salary:2026-08-25",
            periodStart: 1787581200000,
            periodEndExclusive: 1790259600000,
            periodBasis: "SALARY_CYCLE",
            month: null,
            limitAmount: 5000000,
            spentAmount: 0,
            notified80: false,
            notified100: false,
        }));

        await assertSucceeds(ref.update({limitAmount: 6000000}));
        await assertFails(ref.update({spentAmount: 100000}));
    });
});

describe("Firestore Rules: Deals", () => {
    const validDeal = {
        title: "Cho vay ngắn hạn",
        description: "Theo dõi thu hồi vốn",
        category: "lending",
        targetAmount: 12000000,
        totalCapitalOutlay: 10000000,
        totalRecovered: 0,
        netProfitLoss: 0,
        status: "active",
        startDate: new Date(),
        endDate: null,
        createdAt: new Date(),
        updatedAt: new Date(),
    };

    it("allows only the owner to read and write deals", async () => {
        const alice = testEnv.authenticatedContext("alice");
        const bob = testEnv.authenticatedContext("bob");
        const path = "users/alice/deals/deal-1";

        await assertSucceeds(alice.firestore().doc(path).set(validDeal));
        await assertSucceeds(alice.firestore().doc(path).get());
        await assertFails(bob.firestore().doc(path).get());
        await assertFails(bob.firestore().doc(path).set(validDeal));
    });

    it("allows a deal ledger transaction only when the wallet delta is atomic", async () => {
        const alice = testEnv.authenticatedContext("alice");
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().doc("users/alice/wallets/w1").set({
                balance: 1000000,
                lastTransactionId: "seed",
            });
        });

        const batch = alice.firestore().batch();
        const walletRef = alice.firestore().doc("users/alice/wallets/w1");
        const txRef = alice.firestore().doc("users/alice/transactions/deal-outlay-1");

        batch.update(walletRef, {
            balance: 900000,
            lastTransactionId: "deal-outlay-1",
        });
        batch.set(txRef, {
            type: "expense",
            amount: 100000,
            categoryId: null,
            walletId: "w1",
            relatedWalletId: null,
            dealId: "deal-1",
            dealFlowType: "outlay_capital",
            note: "Xuất vốn",
            receiptImageUrl: null,
            date: new Date(),
            createdAt: new Date(),
            updatedAt: new Date(),
        });

        await assertSucceeds(batch.commit());
    });



    it("allows atomic cascade deletion of a deal with multiple ledger entries", async () => {
        const alice = testEnv.authenticatedContext("alice");
        const walletPath = "users/alice/wallets/w1";
        const dealPath = "users/alice/deals/deal-cascade";
        const outPath = "users/alice/transactions/deal-out";
        const inPath = "users/alice/transactions/deal-in";

        await testEnv.withSecurityRulesDisabled(async (context) => {
            const db = context.firestore();
            await db.doc(walletPath).set({
                name: "Ví chính",
                type: "bank",
                balance: 800000,
                color: "#000000",
                isDefault: true,
                createdAt: new Date(),
                lastTransactionId: "deal-in",
            });
            await db.doc(dealPath).set({
                title: "Deal cascade",
                description: "",
                category: "investment",
                targetAmount: 1000000,
                totalCapitalOutlay: 400000,
                totalRecovered: 200000,
                netProfitLoss: 0,
                status: "active",
                startDate: new Date(),
                endDate: null,
                createdAt: new Date(),
                updatedAt: new Date(),
            });
            await db.doc(outPath).set({
                type: "expense",
                amount: 400000,
                categoryId: null,
                walletId: "w1",
                relatedWalletId: null,
                dealId: "deal-cascade",
                dealFlowType: "outlay_capital",
                note: "Xuất vốn",
                receiptImageUrl: null,
                date: new Date(),
                createdAt: new Date(),
                updatedAt: new Date(),
            });
            await db.doc(inPath).set({
                type: "income",
                amount: 200000,
                categoryId: null,
                walletId: "w1",
                relatedWalletId: null,
                dealId: "deal-cascade",
                dealFlowType: "principal_recovery",
                note: "Thu hồi vốn",
                receiptImageUrl: null,
                date: new Date(),
                createdAt: new Date(),
                updatedAt: new Date(),
            });
        });

        const batch = alice.firestore().batch();
        batch.update(alice.firestore().doc(walletPath), {
            balance: 1000000,
            lastTransactionId: "deal-delete",
        });
        batch.delete(alice.firestore().doc(outPath));
        batch.delete(alice.firestore().doc(inPath));
        batch.delete(alice.firestore().doc(dealPath));

        await assertSucceeds(batch.commit());
    });
    it("allows capital-loss settlement entries without a fake wallet mutation", async () => {
        const alice = testEnv.authenticatedContext("alice");
        const ref = alice.firestore().doc("users/alice/transactions/deal-loss-1");

        await assertSucceeds(ref.set({
            type: "expense",
            amount: 250000,
            categoryId: null,
            walletId: "DEAL_SETTLEMENT",
            relatedWalletId: null,
            dealId: "deal-1",
            dealFlowType: "capital_loss",
            note: "Chốt lỗ",
            receiptImageUrl: null,
            date: new Date(),
            createdAt: new Date(),
            updatedAt: new Date(),
        }));
    });
});
