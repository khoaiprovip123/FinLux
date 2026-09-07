import {assertFails, assertSucceeds, initializeTestEnvironment} from "@firebase/rules-unit-testing";
import type {RulesTestContext, RulesTestEnvironment} from "@firebase/rules-unit-testing";
import * as fs from "fs";
import * as path from "path";

let testEnv: RulesTestEnvironment;

function rulesFile(): string {
  const candidates = ["storage.rules", "../storage.rules", path.resolve(process.cwd(), "storage.rules")];
  const found = candidates.find((candidate) => fs.existsSync(candidate));
  if (!found) throw new Error("storage.rules not found");
  return found;
}

function upload(context: RulesTestContext, objectPath: string, size: number, contentType: string) {
  return context.storage("gs://finlux-test.appspot.com")
    .ref(objectPath)
    .put(new Uint8Array(size), {contentType});
}

before(async () => {
  const [host, rawPort] = (process.env.FIREBASE_STORAGE_EMULATOR_HOST ?? "127.0.0.1:9199").split(":");
  testEnv = await initializeTestEnvironment({
    projectId: "finlux-test",
    storage: {host, port: Number(rawPort), rules: fs.readFileSync(rulesFile(), "utf8")},
  });
});

after(async () => testEnv?.cleanup());
beforeEach(async () => testEnv.clearStorage());

describe("Storage Rules: avatars and receipts", () => {
  it("allows owners to upload, read and delete valid images", async () => {
    const alice = testEnv.authenticatedContext("alice");
    const avatar = alice.storage("gs://finlux-test.appspot.com").ref("avatars/alice.jpg");
    const receipt = alice.storage("gs://finlux-test.appspot.com").ref("receipts/alice/tx-1.jpg");
    await assertSucceeds(upload(alice, avatar.fullPath, 32, "image/jpeg"));
    await assertSucceeds(upload(alice, receipt.fullPath, 32, "image/jpeg"));
    await assertSucceeds(avatar.getMetadata());
    await assertSucceeds(receipt.getMetadata());
    await assertSucceeds(avatar.delete());
    await assertSucceeds(receipt.delete());
  });

  it("denies anonymous and cross-user access", async () => {
    const alice = testEnv.authenticatedContext("alice");
    await assertSucceeds(upload(alice, "avatars/alice.jpg", 32, "image/jpeg"));
    const bobRef = testEnv.authenticatedContext("bob").storage("gs://finlux-test.appspot.com").ref("avatars/alice.jpg");
    const guestRef = testEnv.unauthenticatedContext().storage("gs://finlux-test.appspot.com").ref("avatars/alice.jpg");
    await assertFails(bobRef.getMetadata());
    await assertFails(bobRef.delete());
    await assertFails(guestRef.getMetadata());
  });

  it("denies invalid MIME types and files larger than 5 MiB", async () => {
    const alice = testEnv.authenticatedContext("alice");
    await assertFails(upload(alice, "avatars/alice.jpg", 32, "text/plain"));
    await assertFails(upload(alice, "receipts/alice/oversize.jpg", 5 * 1024 * 1024 + 1, "image/jpeg"));
  });

  it("denies unsupported paths and avatar extensions", async () => {
    const alice = testEnv.authenticatedContext("alice");
    await assertFails(upload(alice, "avatars/alice.gif", 32, "image/gif"));
    await assertFails(upload(alice, "private/alice.jpg", 32, "image/jpeg"));
  });
});
