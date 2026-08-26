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
