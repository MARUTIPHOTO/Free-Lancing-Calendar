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

test("Public user can read studio profile and dates", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("studios").doc(ALICE_UID).set({
      studioId: ALICE_UID,
      studioName: "Alice Photography",
      phone: "9876543210"
    });
    await context.firestore().collection("studios").doc(ALICE_UID).collection("dates").doc("2026-11-15").set({
      dateKey: "2026-11-15",
      status: "FREE"
    });
  });

  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertSucceeds(unauthDb.collection("studios").doc(ALICE_UID).get());
  await assertSucceeds(unauthDb.collection("studios").doc(ALICE_UID).collection("dates").doc("2026-11-15").get());
});

test("Public client can create booking inquiry with PENDING status", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertSucceeds(
    unauthDb.collection("studios").doc(ALICE_UID).collection("inquiries").doc("inq_01").set({
      clientName: "Rahul Sharma",
      clientPhone: "9876543210",
      status: "PENDING"
    })
  );
});

test("Public client cannot create booking inquiry directly with CONFIRMED status", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("studios").doc(ALICE_UID).collection("inquiries").doc("inq_02").set({
      clientName: "Rahul Sharma",
      clientPhone: "9876543210",
      status: "CONFIRMED"
    })
  );
});

test("Other user Bob cannot modify Alice's studio dates or inquiries", async () => {
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb.collection("studios").doc(ALICE_UID).collection("dates").doc("2026-11-15").set({
      dateKey: "2026-11-15",
      status: "BOOKED"
    })
  );
});

test("Studio owner Alice can manage her dates and update inquiry status", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("studios").doc(ALICE_UID).collection("dates").doc("2026-11-15").set({
      dateKey: "2026-11-15",
      status: "BOOKED"
    })
  );

  await assertSucceeds(
    aliceDb.collection("studios").doc(ALICE_UID).collection("inquiries").doc("inq_01").set({
      clientName: "Rahul Sharma",
      clientPhone: "9876543210",
      status: "CONFIRMED"
    })
  );
});
