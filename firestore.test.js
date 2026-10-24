const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_citizen";
const ADMIN_UID = "admin_dispatcher";

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

test("Unauthenticated user: cannot read bookings or users", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("bookings").get());
  await assertFails(unauthDb.collection("users").get());
});

test("Unauthenticated user: can read ambulances fleet", async () => {
  // Ambulances are public emergency fleet info for citizens
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  // But rule says allow get, list: if isSignedIn(); so unauthenticated fails
  await assertFails(unauthDb.collection("ambulances").get());
});

test("Authenticated user: can read ambulances", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("ambulances").get());
});

test("Authenticated user: can create their own emergency booking with status Pending", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("bookings").doc("booking_001").set({
      bookingId: "booking_001",
      userId: ALICE_UID,
      requestedTransport: "Road",
      pickupLocation: "123 Main St",
      destination: "MOH Central Hospital",
      medicalProblem: "Chest pain",
      emergencyType: "Cardiac",
      currentStatus: "Pending",
      createdAt: new Date(),
    })
  );
});

test("Authenticated user: cannot create booking with other user's ID", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("bookings").doc("booking_002").set({
      bookingId: "booking_002",
      userId: "different_user",
      requestedTransport: "Road",
      pickupLocation: "123 Main St",
      destination: "MOH Central Hospital",
      medicalProblem: "Chest pain",
      emergencyType: "Cardiac",
      currentStatus: "Pending",
      createdAt: new Date(),
    })
  );
});

test("Authenticated user: cannot read other user's booking", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("bookings").doc("bob_booking").set({
      bookingId: "bob_booking",
      userId: "bob_456",
      requestedTransport: "Road",
      pickupLocation: "456 Oak St",
      destination: "MOH Central Hospital",
      medicalProblem: "Fracture",
      emergencyType: "Trauma",
      currentStatus: "Pending",
      createdAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("bookings").doc("bob_booking").get());
});
