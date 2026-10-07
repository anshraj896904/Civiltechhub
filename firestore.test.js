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

test("Unauthenticated user: cannot read user study profile", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
});

test("Authenticated user: can create and read own study profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice Civil Student",
      selectedDiplomaTrack: "BIM_MODELING_3D",
      preferredSoftwareVersion: "Revit 2025",
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
});

test("Authenticated user: cannot read or write another user's study profile", async () => {
  const now = new Date();
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      displayName: "Bob",
      selectedDiplomaTrack: "ALL_TRACKS",
      preferredSoftwareVersion: "AutoCAD 2025",
      createdAt: now,
      updatedAt: now,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
});

test("Authenticated user: rejects shadow field injection on study profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      selectedDiplomaTrack: "ALL_TRACKS",
      preferredSoftwareVersion: "AutoCAD 2025",
      isAdmin: true,
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("Authenticated user: can create and query own bookmarked commands, but not another user's", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date();

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("bookmarked_commands")
      .doc("cmd_ac_layer")
      .set({
        id: "cmd_ac_layer",
        userId: ALICE_UID,
        software: "AutoCAD",
        versionScope: "AutoCAD 2025",
        taskTitle: "Layer Properties Manager",
        command: "LAYER",
        shortcut: "LA",
        menuPath: "Home > Layers > Layer Properties",
        siteExample: "Isolate structural grid lines",
        createdAt: now,
        updatedAt: now,
      })
  );

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("bookmarked_commands")
      .where("userId", "==", ALICE_UID)
      .get()
  );

  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("bookmarked_commands")
      .doc("cmd_ac_layer")
      .get()
  );
});

test("Authenticated user: can create and query own interview attempts and rejects invalid score > 10", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("interview_attempts")
      .doc("attempt_1")
      .set({
        id: "attempt_1",
        userId: ALICE_UID,
        topic: "Navisworks 4D/5D",
        questionType: "Technical",
        question: "What is a hard clash vs clearance clash?",
        userAnswer: "Hard clash is geometry intersection; clearance is buffer violation.",
        score: 9,
        whatWasGood: "Clear distinction between physical intersection and tolerance buffer.",
        betterSampleAnswer: "Mention Search Sets and tolerance values in Clash Detective.",
        createdAt: now,
        updatedAt: now,
      })
  );

  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("interview_attempts")
      .doc("attempt_bad_score")
      .set({
        id: "attempt_bad_score",
        userId: ALICE_UID,
        topic: "Navisworks 4D/5D",
        questionType: "Technical",
        question: "What is a hard clash?",
        userAnswer: "Intersection.",
        score: 15,
        whatWasGood: "Good",
        betterSampleAnswer: "Better",
        createdAt: now,
        updatedAt: now,
      })
  );
});
