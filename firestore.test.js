const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read or write user profile", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).set({
    userId: ALICE_UID,
    email: "alice@example.com"
  }));
});

test("Authenticated user: can read and write own profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set({
    userId: ALICE_UID,
    email: "alice@example.com",
    displayName: "Alice",
    currency: "USD",
    country: "USA",
    city: "New York",
    notificationsEnabled: true,
    savedCards: ["Chase Sapphire", "Amex Gold"]
  }));
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
});

test("Cross-user access: Alice cannot read or write Bob's profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
  await assertFails(aliceDb.collection("users").doc(BOB_UID).set({
    userId: BOB_UID,
    email: "bob@example.com"
  }));
});

test("Authenticated user: can save and read own deals", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("saved_deals").doc("deal_1").set({
    userId: ALICE_UID,
    dealId: "deal_1",
    merchant: "FlyNas",
    title: "Flight deal 25% off",
    discountHighlight: "25% OFF",
    category: "Travel",
    city: "Riyadh",
    country: "Saudi Arabia"
  }));
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("saved_deals").doc("deal_1").get());
});

test("Cross-user access: Alice cannot read Bob's saved deals", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).collection("saved_deals").doc("deal_1").set({
      userId: BOB_UID,
      dealId: "deal_1",
      merchant: "Amazon",
      title: "Super Sale"
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("saved_deals").doc("deal_1").get());
});

test("Authenticated user: can create and read reminders", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("reminders").doc("rem_1").set({
    userId: ALICE_UID,
    reminderId: "rem_1",
    dealId: "deal_1",
    dealTitle: "Flight discount",
    merchant: "FlyNas",
    notifyBeforeDays: 2,
    isDismissed: false
  }));
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("reminders").doc("rem_1").get());
});
