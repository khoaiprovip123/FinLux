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
