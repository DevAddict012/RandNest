# RandNest — SDLC Phase 4: Implementation Guide

> **Project:** RandNest — *Your money. Your plan. Your control.*  
> **Document ID:** RN-IMP-004  
> **Version:** 1.0 — Developer implementation guide, for owner review  
> **Prepared:** 18 November 2026  
> **Status:** Phase 3 approved; **Phase 4 guide prepared, implementation not yet completed**  
> **SDLC phase:** 4 of 6 — Implementation  
> **Approved inputs:** [01_REQUIREMENTS_ANALYSIS.md](./01_REQUIREMENTS_ANALYSIS.md) (RN-SRS-001), [02_PROJECT_PLANNING.md](./02_PROJECT_PLANNING.md) (RN-PLAN-002), and [03_SYSTEM_DESIGN.md](./03_SYSTEM_DESIGN.md) (RN-DES-003). The owner approved these in conversation on 18 November 2026.  
> **Next:** `05_TESTING.md` after an integrated, feature-complete build has evidence for the Phase 4 gate.  
> **Primary audience:** Solo Java/Android developer (project owner), independent developer taking over the project, reviewer and tester.

---

## 1. Purpose, contract and non-negotiable boundaries

### 1.1 What this document is

This is an **implementation playbook**: the exact order in which to build RandNest, how components communicate, which source files and Android resources to create, how to convert the approved design into Java/XML/SQLite, and what evidence proves each increment is working. It includes coding patterns, SQL integration guidance, interface contracts, test cases, failure-handling rules, and a handoff checklist.

**This file is not a claim that a working Android app has already been written or tested.** The owner will implement the project personally in Android Studio, one small feature at a time. Examples in this document are illustrative starting points; do not present them as an entire compiled app. The implementation phase is complete only when source code, an installable debug build and the specified passing checks exist.

### 1.2 Sources of truth

1. **Phase 1:** requirements, acceptance criteria and business rules (`FR`, `NFR`, `BR`, `AC`).
2. **Phase 2:** project constraints, time and resources, milestones, quality gates and scope control.
3. **Phase 3:** **approved** architecture, schema v1, UI-01–UI-18 screen contract, financial posting protocol and interfaces.
4. **This document:** construction order, implementation tickets, examples and completion evidence. It must not override the first three sources.
5. **The implementation and tests:** observable proof, not permission to silently change the requirements.

If an example here conflicts with the exact schema or data contract in Phase 3, **stop and write a change request**. Phase 3 §4.3 is the authoritative DDL; Phase 3 §§5–6 define posting and planning behaviour.

### 1.3 Scope: build this, not a real bank

**MVP:** one local profile; PIN gate; multiple virtual `SPENDING` and `SAVINGS` accounts; simulated funding, expense and internal transfer; immutable transaction history/receipts; one-time planned bills; full linked simulated bill settlement; per-Spending-account planning reserve; 30-day Safe-to-Spend dashboard; offline persistence; deliberate reset; explicit simulation messaging.

**Excluded:** real money, banking APIs, beneficiaries outside owned virtual accounts, remote accounts, Firebase, server, cloud sync, collection of real banking credentials, real card payments, KYC, loans, multi-currency, biometric unlock, recurring bills, savings-goal engine and analytics in this first release. Deferred features in `FR-09`, `FR-16`, `FR-19`, `FR-25`, `FR-31` require later approval.

### 1.4 Technical baseline (from approved design)

| Concern | Mandatory selection |
|---|---|
| IDE | Android Studio capable of the approved AGP version |
| Application language | **Java only** for app code and tests; **no Kotlin** |
| UI | XML layouts with platform `Activity`/Android Views; no Compose |
| Gradle DSL | Groovy `.gradle` scripts, not Kotlin `.gradle.kts` |
| Data | Android platform `SQLiteOpenHelper` and `SQLiteDatabase`; **no Room** |
| Database | `randnest.db`, schema v1, single source of truth = signed ledger |
| Project identity | Provisional `com.randnest.app`; `versionName = 0.1.0` |
| Android | `minSdk=26`, `compileSdk=36`, `targetSdk=36` |
| Toolchain | AGP `8.13.2`, Gradle `8.13`, JDK 17, Java 17 source/target |
| Unit/instrumentation | JUnit `4.13.2`, AndroidX JUnit `1.3.0`, Espresso `3.7.0` (tests only) |
| Connectivity | None required; no `INTERNET` permission |

Do not automatically substitute later library/SDK versions simply because Android Studio recommends them. First establish that the approved versions sync/build. If an installed Studio version cannot support them, capture the error and write a small version-change decision with compatibility evidence.

### 1.5 Project responsibilities

The owner writes, runs, debugs and commits the app. Any helper can supply explanations, review Java/XML/SQL changes, discuss failures and propose tests. A milestone becomes accepted only after the **owner observes** its successful execution on an Android emulator or device and records evidence. No need to finish every screen before beginning test coverage.

---

## 2. Development workflow and task-completion policy

### 2.1 Working cadence

Implement **vertical slices**, not all XML first and all SQL last. For every slice, follow:

1. Re-read its `FR`/`BR`/`AC` and Phase 3 contract.
2. Define valid inputs, rejected inputs, side effects and restart expectations.
3. Write a small test or manual assertion reproducing the desired result.
4. Implement the domain rule and SQLite repository before wiring a real UI.
5. Connect Java `Activity` and XML views; run the happy path.
6. Run negative/rollback, rotation and restart scenarios relevant to that slice.
7. Record evidence, update the traceability table and commit.
8. Proceed only once that milestone's **exit gate** passes.

Keep a visible board: **Backlog → Ready → In progress → Review/test → Done**. If a feature introduces new schema, behavior or permissions beyond approved scope, use a change request instead of quietly coding it.

### 2.2 Implementation milestones

| ID | Slice | Owner-visible demonstration | Required gate |
|---|---|---|---|
| M00 | Environment, Git and project skeleton | Java/XML launch screen runs on an emulator | App starts; Gradle clean build; no Kotlin/unsafe permissions |
| M01 | Schema and storage foundation | DB v1 created and verified | FK/integrity tests, schema matches Phase 3 |
| M02 | Onboarding, PIN, unlock and reset | New profile, lock/restart/unlock, erase demo data | Auth/security/lifecycle tests pass |
| M03 | Accounts and Spending selection | Create Spending and Savings at R0.00; select Spending | Ownership/type/zero-balance tests pass |
| M04 | Money parser, intents, funding and expense | Fund and spend exact virtual cents, see receipts | FT-01, FT-02, FT-04, FT-05, FT-14 |
| M05 | Internal transfer and resilient confirmation | Transfer R350 as one two-leg operation | FT-03, FT-06, FT-07 and interruption tests |
| M06 | History and transaction detail | Inspect each posted operation and signed effects | Ordering, pagination, receipt tests |
| M07 | Bills (unpaid state machine) | Create/edit/cancel bill without spending money | FT-08 and date/status tests |
| M08 | Linked bill settlement | Settle one bill with a committed expense | FT-10 and FT-16; no double counting |
| M09 | Reserve, dashboard and Safe-to-Spend | R3,000 − R2,200 − R300 = R500 | FT-09, FT-11, FT-12, FT-15 |
| M10 | Integration and hardening | Complete demo in airplane mode, data survives relaunch | AC-01–AC-12 provisional pass, accessibility/privacy/load review |

**Milestones are not deadlines.** Phase 2's 196–286 effort-hour baseline (plus contingency) remains a planning estimate. Record actual effort and pace without sacrificing financial tests.

### 2.3 Consistent Git practice

Suggested branches: `main` (reviewed milestones), `feature/m01-sqlite`, `feature/m02-auth`, etc. Create commits that tell a story, for example:

```text
chore(android): initialize Java XML app skeleton
feat(db): implement schema v1 and foreign-key checks
test(money): verify transfer rollback after first ledger leg
feat(planning): calculate inclusive 30-day Safe-to-Spend
```

Avoid checking in `.gradle/`, local machine secrets, emulator images, database dumps containing personal data or local SDK paths. Commit the Gradle wrapper files, meaningful documentation, the versioned schema and tests.

**Milestone evidence template:**

```text
Milestone: M0x
Commit SHA:
Date and developer:
Android Studio / JDK / AGP / SDK / emulator API:
Requirements and design section:
Build result:
Unit tests (number passed/failed):
Instrumentation tests (number passed/failed):
Manual steps performed:
Screenshot or test-log reference (synthetic data only):
Defects and follow-ups:
Decision: PASS / BLOCKED
```

---

## 3. M00 — Configure Android Studio and build a clean skeleton

### 3.1 Environment checklist

- Install Android Studio compatible with the baseline and install Android SDK Platform **36** and Android SDK Platform **26** for testing.
- Ensure **JDK 17** is the Gradle JDK and the project uses a checked-in **Gradle 8.13 wrapper**.
- Create at least two AVDs: one **API 26** and one **API 36**. Add an intermediate API device when available. A physical phone is optional.
- Create an **Empty Views Activity**, choose **Java**, application ID `com.randnest.app`, minimum API 26. Do not choose Compose or a Kotlin template.
- Choose Groovy Gradle files; verify `app/src/main/java/.../*.java` and `app/src/main/res/layout/*.xml` exist.
- Do not add external backend, banking integration or runtime permissions.

### 3.2 Build configuration

Approved root `build.gradle`:

```groovy
plugins {
    id 'com.android.application' version '8.13.2' apply false
}
```

Approved Gradle wrapper value (`gradle/wrapper/gradle-wrapper.properties`):

```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-8.13-bin.zip
```

The standard Android Studio `settings.gradle` should resolve plugins/dependencies from `google()` and `mavenCentral()`. For the app module:

```groovy
plugins {
    id 'com.android.application'
}

android {
    namespace 'com.randnest.app'
    compileSdk 36

    defaultConfig {
        applicationId 'com.randnest.app'
        minSdk 26
        targetSdk 36
        versionCode 1
        versionName '0.1.0'
        testInstrumentationRunner 'androidx.test.runner.AndroidJUnitRunner'
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
    buildTypes {
        release { minifyEnabled false }
    }
}

dependencies {
    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test.ext:junit:1.3.0'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.7.0'
}
```

**Important:** This assumes the compatible Android Studio template manages plugin resolution and repository configuration. Treat Gradle sync and actual build as validation, not as a guarantee merely because the snippet appears in a document.

### 3.3 Manifest, app identity and launch

Create `GateActivity` as the **only launcher/exported Activity**. All Setup, Unlock and protected Activities use `android:exported="false"`. In the application manifest set `android:name=".RandNestApp"`, `android:allowBackup="false"`, `android:fullBackupContent="@xml/legacy_backup_rules"` and `android:dataExtractionRules="@xml/data_extraction_rules"`. Create the referenced XML resources before building (see §14). Do not declare `INTERNET`, contacts, camera, SMS, location, external storage or banking permissions.

Initially `GateActivity` can display a neutral **RandNest — DEMO** launch screen. **Do not bypass auth in later milestones**; this temporary screen is replaced by the setup/unlock router in M02.

### 3.4 Build and run commands

From the project root on macOS/Linux:

```bash
./gradlew clean assembleDebug
./gradlew testDebugUnitTest
./gradlew connectedDebugAndroidTest   # with an API emulator/device running
```

On Windows PowerShell:

```powershell
.\gradlew.bat clean assembleDebug
.\gradlew.bat testDebugUnitTest
.\gradlew.bat connectedDebugAndroidTest
```

Record the actual AGP/JDK/SDK version, then install/launch on API 26 and 36. Never state a device test has passed if it was not run.

**M00 exit gate:** Gradle sync succeeds; debug APK installs and launches; XML screen appears; rotating and reopening do not crash; one starter unit test runs; repository has a clean first commit.

---

## 4. Project layout and dependency boundaries

Use the Phase 3 §8.1 package map as authoritative. Expand packages as they become necessary rather than generating dozens of unused empty files:

```text
RandNest/
├── README.md
├── docs/sdlc/01_REQUIREMENTS_ANALYSIS.md ... 04_IMPLEMENTATION.md
├── settings.gradle
├── build.gradle
├── gradle/wrapper/gradle-wrapper.properties
└── app/src/
    ├── main/
    │   ├── AndroidManifest.xml
    │   ├── java/com/randnest/app/
    │   │   ├── RandNestApp.java
    │   │   ├── common/                 # Result<T>, ResultCode, Clock/fault seams
    │   │   ├── domain/model/           # VirtualAccount, Receipt, PlannedBill ...
    │   │   ├── domain/util/            # MoneyParser, MoneyFormatter, DateWindow, PinKdf
    │   │   ├── domain/service/         # Auth, Account, Money, Bill, Planning services
    │   │   ├── data/RandNestDbHelper.java
    │   │   ├── data/repository/        # Interfaces from Phase 3 §8.2
    │   │   ├── data/sqlite/            # SQLite repository implementations
    │   │   ├── presentation/           # Plain Java presenters, UiState, callbacks
    │   │   └── ui/                     # Activity classes from Phase 3 §7.2
    │   └── res/
    │       ├── layout/                 # activity_*.xml
    │       ├── values/                 # strings.xml, themes/styles, colors
    │       └── xml/                    # backup rules
    ├── test/java/com/randnest/app/     # Pure Java JUnit tests
    └── androidTest/java/com/randnest/app/ # On-device SQLite/Activity tests
```

### 4.1 Dependencies flow in one direction

```text
Android XML + Activity
       ↓
Plain Java presenter / UiState
       ↓
Domain services and validators
       ↓
Repository interfaces
       ↓
SQLite repository implementations
       ↓
SQLiteOpenHelper → randnest.db
```

- Activities **may not** issue SQL, perform a posting calculation, store a plaintext PIN, or maintain their own authoritative balances.
- Services **may not** reference `TextView`, `EditText`, `Activity` or layout resources.
- SQLite repositories **may not** leak `Cursor`s or `SQLiteDatabase` write handles to Activities.
- The `domain` layer uses plain `long`, `String`, `LocalDate`, `Clock` and immutable data classes; keep most of it JUnit-testable without an emulator.
- `RandNestApp` is a small composition root: it owns the shared `ExecutorService`, `RandNestDbHelper`, repositories and RAM-only `SessionManager`. Avoid turning it into an untestable global object.
- **No SQL or expensive PBKDF2 on Android's main thread.** Serialize initial repository operations on one `ExecutorService` to simplify ordering; marshal display callbacks to the main thread and ignore callbacks after an Activity is destroyed.

### 4.2 Minimum service and repository contracts

Retain the signatures in Phase 3 §8.2 rather than making up per-screen data access. At a minimum:

| API | Inputs → Output | Responsibility |
|---|---|---|
| `AuthService.completeSetup(...)` | name, PIN/confirmation → `Result<Void>` | Atomic profile/auth/settings setup |
| `AuthService.unlock(...)` | PIN, time → `Result<Void>` | PIN verification, lockout, in-memory unlock |
| `AccountRepository.createAccount(...)` | profile, name, type → account ID | Persist valid owned account with zero ledger entries |
| `AccountRepository.readPostedBalanceCents(...)` | account ID → `long` cents | `SUM(ledger_entries.delta_cents)` |
| `MoneyRepository.prepareIntent(...)` | validated immutable payload → intent | Persist `PREPARED` review request |
| `MoneyRepository.postIntent(...)` | same intent ID/payload → receipt | Atomic posting + idempotency |
| `MoneyRepository.listHistory(...)` | account, pagination → rows | Newest first; signed amounts |
| `BillRepository` | create/edit/cancel/list/unpaid subtotal | Bill-only writes never affect balance |
| `ReserveRepository` | set/read reserve | Planning data, not ledger posting |
| `PlanningService.calculate(...)` | Spending ID and `Clock` → snapshot | 30-day read-consistent Safe-to-Spend |

Use an explicit `ResultCode` enum from Phase 3 §3.3. A result must distinguish `OK`, `VALIDATION_ERROR`, `INSUFFICIENT_FUNDS`, `ALREADY_COMPLETED`, `ALREADY_CANCELLED`, `CONFLICT`, `OVERFLOW`, `LOCKED` and `STORAGE_ERROR`. UI messages should come from string resources, not exposed SQL exception messages.

---

## 5. Shared domain conventions (implement before money screens)

### 5.1 Exact money semantics

- **All amounts are integer ZAR cents stored as SQLite `INTEGER` and Java `long`.** `1_000` cents = **R10.00**, not R1,000.00.
- Never use Java `double`, `float` or SQLite `REAL` for money, and do not parse a user's amount using `Double.parseDouble`.
- User posting amounts must be **strictly positive** and at most `1_000_000_000` cents (**R10,000,000.00**) as approved in Phase 3.
- Account balances must not exceed `100_000_000_000` cents (**R1,000,000,000.00**) or go below zero after a debit.
- A reserve is nonnegative and may exceed the current balance; it is capped at the account-balance ceiling and must **not** post a transfer.
- Strict text parser accepts `1234`, `1234.5` and `1234.56`; reject currency symbols inside the numeric field, commas, signs, blank values, exponent notation and more than two decimal places.
- Display always uses exactly two decimal places, with an `R` prefix; history debits/credits use appropriate signs. Implement one `MoneyFormatter` used by every screen.
- Use `Math.addExact`, `Math.subtractExact`, and `Math.negateExact`, with typed overflow errors.

Illustrative **pure Java** parser suitable for an initial JUnit test (the owner should integrate it with domain errors and UI feedback):

```java
package com.randnest.app.domain.util;

import java.math.BigDecimal;

public final class MoneyParser {
    public static final long MAX_POSTING_CENTS = 1_000_000_000L;

    private MoneyParser() { }

    public static long parsePositivePostingCents(String raw) {
        if (raw == null ||
            !raw.matches("(?:0|[1-9][0-9]*)(?:\\.[0-9]{1,2})?")) {
            throw new IllegalArgumentException("invalid_money_format");
        }
        long cents;
        try {
            cents = new BigDecimal(raw).movePointRight(2).longValueExact();
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("money_out_of_range", ex);
        }
        if (cents <= 0 || cents > MAX_POSTING_CENTS) {
            throw new IllegalArgumentException("money_out_of_range");
        }
        return cents;
    }
}
```

Example assertions:

```java
assertEquals(12575L, MoneyParser.parsePositivePostingCents("125.75"));
assertEquals(100000L, MoneyParser.parsePositivePostingCents("1000"));
assertThrows(IllegalArgumentException.class,
    () -> MoneyParser.parsePositivePostingCents("12.345"));
```

`assertThrows` is available with the pinned JUnit 4.13.2. Use different parsing rules for reserves (zero allowed) and never silently round an invalid posting.

### 5.2 Stable identity and timestamps

- Account, bill, and intent IDs are UUID strings generated with `UUID.randomUUID().toString()`; **names are not identifiers**.
- `profiles.profile_id` is the integer `1`. Both account ownership checks and current-session checks are explicit.
- Store operation creation/post times as epoch **milliseconds**; display local date/time via Android/Java date formatting.
- Store bill due dates as strictly validated `yyyy-MM-dd` ISO `LocalDate` strings; don't convert them through UTC midnight.
- Use injected `Clock` for date boundaries and PIN timing where appropriate; for the **60-second background timeout**, use a monotonic elapsed clock while the process remains alive.

### 5.3 Immutable model expectations

Suggested plain Java immutable models: `VirtualAccount`, `OperationPayload`, `SubmissionIntent`, `Receipt`, `LedgerHistoryRow`, `PlannedBill`, `SafeToSpendSnapshot` and `Result<T>`. Prefer `final` fields and constructors that enforce required data. Do not pass mutable `ContentValues` or raw `Cursor` instances outside the data layer.

### 5.4 Consistent validation pipeline

`EditText` convenience validation → normalized plain-Java input → domain service validation → **fresh DB state validation inside the write transaction** → committed result → fresh UI query. Screen-level validation reduces user frustration but is never the only protection against negative balances, duplicate posting or invalid account types.

---

## 6. M01 — Build the SQLite foundation

### 6.1 Schema is a copy, not a reinterpretation

Copy **all nine tables and five indexes exactly from Phase 3 §4.3**:

1. `profiles`
2. `auth_state`
3. `accounts`
4. `app_settings`
5. `operation_intents`
6. `money_operations`
7. `ledger_entries`
8. `planned_bills`
9. `savings_reserves`

Indexes: `idx_accounts_owner_type`, `idx_ledger_account_entry`, `idx_operations_date`, `idx_bills_account_status_due`, `idx_intents_state_created`.

Put the DDL in a clearly named `SchemaV1` Java class or well-structured SQL constants used by `RandNestDbHelper.onCreate()`. **Do not add `balance_cents` to the `accounts` table.** Avoid a second copy of the full DDL in this guide: the approved Phase 3 schema is the only authoritative schema text.

### 6.2 Database helper responsibilities

```java
public final class RandNestDbHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "randnest.db";
    public static final int DB_VERSION = 1;

    public RandNestDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Execute the approved SchemaV1 CREATE TABLE statements in FK order,
        // then its five CREATE INDEX statements. No sample money is seeded.
        SchemaV1.createAll(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        throw new SQLiteException("No approved schema migration yet");
    }
}
```

This snippet is not self-contained until `SchemaV1` is written using Phase 3 SQL and imports are added (`Context`, `SQLiteDatabase`, `SQLiteOpenHelper`, `SQLiteException`). An unapproved upgrade must **fail safely**, not wipe user data. Add explicit sequential migrations when the schema changes in a future release.

### 6.3 Parameterized queries and cursor ownership

Balances must be read from the ledger. Query pattern:

```sql
SELECT COALESCE(SUM(delta_cents), 0) AS balance_cents
FROM ledger_entries
WHERE account_id = ?;
```

Use `rawQuery(sql, new String[]{accountId})` or `query(...)` with `selectionArgs`, and close the `Cursor` with try-with-resources. If the account ID does not exist, return `NOT_FOUND` rather than confusing it with a real zero-balance account. Handle integer overflow from aggregate SUM as a data integrity problem.

### 6.4 M01 test matrix

| Test | Action | Expected |
|---|---|---|
| DB-01 | Open a fresh app database | Exactly nine tables and five indexes created; `user_version=1` |
| DB-02 | Query `PRAGMA foreign_keys` | Returns `1` for configured DB connection |
| DB-03 | Attempt orphan ledger entry | Fails FK check; no data committed |
| DB-04 | Invalid account type (`CURRENT`, etc.) | `CHECK` rejects it |
| DB-05 | Invalid negative reserve | `CHECK` rejects it |
| DB-06 | Duplicate `(operation_id, account_id)` ledger row | Unique constraint rejects it |
| DB-07 | Query `PRAGMA foreign_key_check` | No rows (empty violation report) |
| DB-08 | Query `PRAGMA integrity_check` | Returns `ok` |
| DB-09 | Close/reopen database | Schema and any legitimate fixture records persist |
| DB-10 | Simulated unsupported upgrade | Throws without destructive `DROP TABLE` |

Use an Android **instrumentation test** for platform SQLite behaviour (not just desktop SQLite). Make each test independent using a fresh temporary database or reliable teardown. Test DDL on **API 26** before calling the schema baseline proven.

**M01 exit gate:** Schema tests pass, FK enforcement is verified, no preloaded fictional balance exists without an operation, and the DB helper never runs expensive work on the main thread.

---

## 7. M02 — Local profile, PIN, session and reset

### 7.1 First-run flow

`GateActivity` asks `AuthService` asynchronously whether `profiles.setup_completed=1` exists. If not, navigate to Setup; otherwise, **start locked** and navigate to Unlock. The profile includes a display name only; do not collect email, ID number, phone number or actual bank details.

Setup validates **6–12 numeric digits** for PIN, requires a matching confirmation, and shows **“RandNest is a simulation. No real money is used.”** Also show: **“All demo data is stored on this device. Resetting, clearing data or uninstalling may erase it.”**

`AuthService.completeSetup` performs password hashing on the worker executor, then **atomically** inserts `profiles`, `auth_state`, `app_settings` and sets completed state in one SQL transaction. `profiles.profile_id` is fixed at `1`; double-submit cannot create another profile.

### 7.2 PIN verifier

- Produce at least a 16-byte salt with `SecureRandom`.
- Derive 256 bits with `SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")`, `PBEKeySpec(pin, salt, iterations, 256)`.
- Start with approved **200,000 iterations** and time verification on the lowest device; persist the count for later migration.
- Persist only algorithm label, salt, derived verifier bytes and lockout metadata in `auth_state`; never store the PIN itself, even in preferences/logs.
- Compare verifier arrays using `MessageDigest.isEqual` and clear temporary `char[]`, derived byte arrays and key specs when practical.
- Count failures persistently. After the fifth consecutive wrong attempt, delay retries by **30 seconds**, with escalating delay capped at **5 minutes**. Clear counters on successful unlock.
- Treat this as a convenience access barrier for a demo: **SQLite is not encrypted** and a short local PIN is not production banking authentication.

### 7.3 UI session

- `SessionManager` exists only in RAM and starts locked on every cold process start.
- Manual Lock immediately invalidates it.
- After the app stays in the background for **60 seconds**, return to Unlock. Use a process-level lifecycle tracker rather than treating transitions from Dashboard to Accounts as leaving the app. Handle configuration-change transitions so rotation doesn't spuriously lock.
- All screens except Gate/Setup/Unlock call a shared guard from `BaseProtectedActivity` on visibility/resume and cannot be reached via external exported intents.
- If an async response arrives after a screen locks or is destroyed, **do not render protected data**.
- App switcher previews and back-stack should not reveal protected balances while locked; check this on devices instead of making an unsupported security claim.

### 7.4 Reset policy

From Settings and the lost-PIN path, require an irreversible-loss warning **and explicit typed confirmation**. Do not require the forgotten PIN just to erase demo data, but **never** expose balances or records during locked reset.

**Choose the Phase 3-approved option here:** within one DB transaction, delete dependents before parents and verify row counts. Recommended child-to-parent deletion order:

```text
planned_bills → savings_reserves → ledger_entries →
money_operations → operation_intents → app_settings →
accounts → auth_state → profiles
```

Clear app-specific preferences/session only **after** the transaction commits; then route to Setup. If deletion fails, do not report success. Verify foreign keys and no partial reset. Do not introduce a second reset strategy silently.

### 7.5 M02 acceptance checks

- Setup requires matching, correctly sized digits; wrong input never creates a profile.
- Re-launching after setup opens Unlock, **not** Setup.
- Wrong PIN doesn't show any protected Activity; lockout survives ordinary process restart.
- Correct PIN opens Dashboard; 60-second backgrounding demands unlock.
- Short foreground navigation and rotation do not inappropriately clear the session.
- Reset after typed confirmation clears all demo records and restarts onboarding; cancelled reset leaves data untouched.
- No plaintext PIN is visible in logs, screenshots, preferences or database inspection.

**M02 exit gate:** AC-01, FR-01–04 and PIN/session checks pass on at least an API 26 test device. Lock and backup restrictions are reviewed again in M10.

---

## 8. M03 — Create virtual accounts and choose a Spending account

### 8.1 Account creation

User can enter a trimmed display name (1–50 characters) and choose `SPENDING` or `SAVINGS`. Create UUID in Java, persist with `profile_id=1`, type and timestamp. Account names may be identical; IDs may not. New account **must** read R0.00 from the empty ledger and must not contain a hidden starting balance.

### 8.2 Account list and selection

Display account name, type, posted balance and selected-planning marker. `app_settings.selected_spending_id` may point only to an existing owned `SPENDING` account. The service rejects `SAVINGS` selection even if the UI presents an invalid deep link or stale identifier. If no Spending account exists, dashboard shows an empty state and a Create Spending Account action rather than fabricating a balance or calculation.

### 8.3 Storage responsibilities

`AccountRepository`: `createAccount`, `getAccounts`, `getAccount`, `readPostedBalanceCents`, `selectSpendingAccount`. The select operation checks the DB type/ownership and updates settings transactionally. When reading an account balance, distinguish a nonexistent account (`NOT_FOUND`) from a legitimate account with no ledger entries (`0`).

**M03 exit gate:** create two accounts (e.g. Spending and Savings), see R0.00 each, select only a Spending account, restart and see identical data. Tests cover duplicate names, wrong type, invalid account ID and zero opening balance.

---

## 9. M04–M05 — Posting engine, prepared intents and transfers

**This is the highest-priority correctness area in the entire project.** No Activity is allowed to alter `ledger_entries` directly. Use one `MoneyService` entry point and one serial database write lane for all money operations.

### 9.1 Ledger model

A `money_operations` record represents the event. One or two `ledger_entries` represent effects on accounts. Account balances are `SUM(delta_cents)` for each account—**no cached mutable balance**.

| Posting type | Ledger row(s) | Validation |
|---|---|---|
| `DEMO_FUNDING` | `(+amount)` in one owned account | Positive bounded amount; destination cap |
| `EXPENSE` | `(−amount)` in one owned account | Balance at commit time >= amount |
| `TRANSFER` | `(−amount)` source and `(+amount)` destination | Both owned; different IDs; source funds; dest cap |
| `BILL_SETTLEMENT` | `(−fullBillAmount)` in bill's Spending account | Bill still UNPAID; full amount; sufficient funds; linked PAID update |

For transfer: R1,000 Spending + R200 Savings, transfer R350 → R650 Spending + R550 Savings. The total R1,200 **never changes**.

### 9.2 Persistent review identity

**Do not generate a new operation ID every time Confirm is pressed.** A durable intent is generated once when the form moves to Review.

1. Validate the form and produce a typed immutable `OperationPayload`.
2. Serialize it as deterministic UTF-8 **canonical ordered JSON**; compute lowercase SHA-256 hex of those exact bytes.
3. Generate one UUID `intent_id`; insert `PREPARED` into `operation_intents` **before** opening Review.
4. The review screen loads that **persisted ID and payload**, not only an Activity's transient extras; its buttons are Confirm/Cancel.
5. The same ID must survive rotation, process death, double tap and retry.
6. When payload changes, cancel the old PREPARED intent and create a new ID; do not edit a reviewed payload.

**Canonical payload format — implementation contract v1:** serialize the following fields **in this exact fixed order** using a deterministic JSON writer. `null` is represented as JSON `null`, not omitted, for optional fields. Amount is numeric integer cents, and all IDs and strings are JSON-escaped; never assemble unsafe JSON by concatenating user input.

```json
{"schema":1,"type":"TRANSFER","accountId":null,"sourceId":"<uuid>","destinationId":"<uuid>","billId":null,"amountCents":35000,"note":"move to savings","category":null}
```

For `DEMO_FUNDING` and `EXPENSE`: populate `accountId`; set `sourceId`, `destinationId`, `billId` to `null`. For `TRANSFER`: use source/destination and `accountId=null`. For `BILL_SETTLEMENT`: set `accountId` to bill's Spending account, `billId` to its ID, `amountCents` to its **stored full amount**; source/destination null. The `type` must match the intent row. Normalize note/category once before preparing, and **never reconstruct JSON from a different field order** on retries.

Suggested type-specific rules: `note` optional, trimmed to an agreed UI cap (recommend <= 200 characters; change-control if Phase 1/3 says otherwise), `category` optional with an allowed finite set or a clearly documented length cap. Include a unit test that serializing the same payload twice gives identical bytes and SHA-256; a field change must alter the hash. Canonical JSON is an internal protocol—**not** user-generated free-form SQL.

### 9.3 Atomic post procedure — authoritative order

For `MoneyRepository.postIntent(intentId, payload)`:

```text
Worker thread only; reject LOCKED session before starting.
BEGIN SQLite write transaction.
  Lookup persisted intent by ID; missing → NOT_FOUND.
  Compare persisted canonical JSON, SHA-256 and operation type.
  CANCELLED → ALREADY_CANCELLED; insert nothing.
  COMPLETED → fetch/check existing receipt; insert nothing.
  PREPARED → re-validate current account(s), balance, bill and limits.
  Insert money_operations (operation_id = intent_id).
  Insert expected one or two ledger_entries.
  If settling bill, conditionally mark it PAID and link operation; require 1 row.
  Set intent PREPARED → COMPLETED with conditional update; require 1 row.
  Mark transaction successful only after all required writes pass.
END transaction in finally (commit or rollback).
After endTransaction returns, load the persisted receipt and report success.
```

**Java transaction shape (illustrative only):**

```java
Result<Receipt> outcome = null;
boolean committed = false;
db.beginTransaction();
try {
    // Read durable intent and revalidate inside this DB transaction.
    // Insert an operation + its required ledger entries + bill update (if any).
    // Update intent state conditionally. Validate each insert/update result.
    db.setTransactionSuccessful();
    committed = true; // provisional; NOT yet visible success
} catch (DomainValidationException ex) {
    outcome = Result.error(ex.code());
} catch (SQLiteException | ArithmeticException ex) {
    outcome = Result.error(ResultCode.STORAGE_ERROR);
} finally {
    try {
        db.endTransaction();
    } catch (SQLiteException ex) {
        committed = false;
        outcome = Result.error(ResultCode.STORAGE_ERROR);
    }
}
// After transaction has ended, reconcile by the SAME intent ID when
// outcome may be uncertain; display saved receipt only if it exists.
```

**Do not copy this partial shape as a full posting method**; it deliberately omits the actual inserts and early-branch handling. If `endTransaction()` throws, the outcome can be uncertain, not necessarily rolled back. Re-read the persisted intent using the **same ID**; never silently regenerate an ID or claim the transfer has posted. Database exceptions must not expose SQL/notes/PIN to the user.

### 9.4 Test-only fault injection

Create a small `FaultInjector` interface with checkpoints `AFTER_OPERATION_INSERT`, `AFTER_FIRST_LEDGER_ENTRY`, `AFTER_SECOND_LEDGER_ENTRY`, `BEFORE_BILL_STATUS_UPDATE` and `BEFORE_COMMIT`. Production defaults to no-op. Instrumentation tests can configure one checkpoint to throw. For each injected failure verify:

- All ledger and related bill changes rolled back **together**, with no orphan operation.
- The original `PREPARED` intent remains retryable unless the transaction actually completed.
- Retrying the **same ID** after removing the fault posts **exactly once**.

### 9.5 User feedback and receipt protocol

On the Review screen show account names fetched fresh by ID, amount, simulated effects, and **“SIMULATION ONLY — no real money will move.”** Disable Confirm while waiting, but rely on intent/SQL uniqueness for true deduplication. Success navigates to `ReceiptActivity` after reading the committed operation. A `COMPLETED` request retried with the same ID returns the **existing** receipt. A `CANCELLED` intent stays cancelled. On cold start, offer to reopen an existing PREPARED review; **never auto-post it**.

### 9.6 M04 and M05 test gates

| Test ID | Scenario | Expected outcome |
|---|---|---|
| FT-01 | Fund R1,000 once | Balance +R1,000, one credit operation |
| FT-02 | Spend R125.75 from R1,000 | Exactly R874.25 remains |
| FT-03 | Transfer R350 between R1,000/R200 | R650/R550, two rows, zero sum |
| FT-04 | Try spending/transferring more than balance | No ledger posting; no negative balance |
| FT-05 | Zero, negative, same-account request | Rejected; no posting |
| FT-06 | Double-confirm/retry same intent | One operation; existing receipt on retry |
| FT-07 | Crash/injected failure during posting | All-or-nothing; recovered by intent ID |
| FT-14 | Oversized amount, account/aggregate cap | Typed rejection; no wrap-around |

**M04 exit gate:** exact funding and expense posted through intents, stored receipts and history. **M05 exit gate:** atomic two-leg transfer, repeat-confirm and fault-injection tests pass on Android SQLite, including process restart cases.

---

## 10. M06 — History, operation detail and receipts

### 10.1 History query

Use the approved Phase 3 §4.4 parameterized query: join `ledger_entries` to `money_operations`, filter by `account_id`, order **`posted_at_ms DESC, entry_id DESC`**, and paginate **50 rows/page** using `LIMIT ? OFFSET ?`. The per-account view shows one signed line for a transfer; each account sees its own leg, but the operation ID is shared.

### 10.2 Detail screen

Show operation reference (UUID), type, local timestamp, category/note when present, affected accounts and correct signed amounts, plus any settled bill link. Don't show a fake external transaction reference or actual payment status. Read the operation **from SQLite**; do not trust a value copied from a previous Activity as the full detail.

### 10.3 Receipt screen

Receipts are shown only for a **committed** operation. If intent is PREPARED, do not fake a receipt. If a request's commit succeeded but the process died before acknowledgement, show its unacknowledged receipt after unlock. Mark `acknowledged_at_ms` only after the receipt is actually presented. Do not silently undo a transfer when navigating Back.

**M06 exit gate:** all four operation types can be identified in the appropriate history (bill settlement after M08); results are newest-first across equal timestamps; pagination has no duplicates; opening an operation reveals correct full detail; same-ID re-entry never creates a second operation.

---

## 11. M07–M08 — Planned bills and linked settlement

### 11.1 Unpaid bill lifecycle

Stored statuses: `UNPAID`, `PAID`, `CANCELLED`. For an unpaid bill, derive `OVERDUE`, `DUE_TODAY` or `UPCOMING` from `LocalDate.now(clock)`. **Never persist OVERDUE**.

For creation require valid Spending account, title 1–80 characters, valid `LocalDate`, positive bounded amount. A past due date is allowed and immediately treated as overdue. `CREATE`, `EDIT`, and `CANCEL` **never write to the ledger**. Restrict edits/cancels to the `UNPAID` state by conditional SQL update and require exactly one affected row. Paid/cancelled bills are read-only.

### 11.2 Bill settlement procedure

When the user presses **Pay with simulated funds** on an unpaid bill:

1. Create a `BILL_SETTLEMENT` PREPARED intent containing the persisted bill ID, its own Spending account and **the full stored amount**.
2. Review the simulated payment (no external payment network).
3. Inside the **same SQLite write transaction**, re-fetch bill, assert `UNPAID`, verify account type/ownership and sufficient posted balance; insert operation, one negative ledger row; `UPDATE planned_bills SET bill_status='PAID', paid_operation_id=?, paid_at_ms=?, updated_at_ms=? WHERE bill_id=? AND bill_status='UNPAID'` and require exactly one row updated; mark intent COMPLETED; commit.
4. If any step fails, **both** the bill and ledger remain unchanged. If an already paid bill is targeted via another intent, reject without a second expense.
5. Only on successful commit does bill display PAID, link to its receipt and disappear from unpaid Safe-to-Spend commitments.

The MVP does **not** allow linking an unrelated historical expense to a bill and does not support partial settlement.

### 11.3 Non-double-counting example

Before settlement: Spending balance **R3,000**, one unpaid **R2,200** bill, reserve **R300** → estimate **R500**. After fully settling the bill: Spending balance **R800**, relevant unpaid bills **R0**, reserve **R300** → estimate **R500**. If both balance and bill total decreased, this demonstrates that the payment was not charged twice in planning.

**M07 exit gate:** bill creation/edit/cancel does not alter posted money; state/date labels are correct. **M08 exit gate:** FT-10 and FT-16 pass, paid bill is linked to exactly one committed expense, insufficient funds/SQL fault leaves it UNPAID, and double settlement is impossible.

---

## 12. M09 — Reserve, Safe-to-Spend and dashboard

### 12.1 30-day planning contract

Select **one existing Spending account**, not a sum of every account. At calculation time:

```text
today = LocalDate.now(injectedClock)
end = today.plusDays(30)   // inclusive
relevantBills = SUM(amount_cents) where
    bill belongs to selected Spending account
    AND bill_status = 'UNPAID'
    AND due_date <= end     // includes arbitrarily overdue unpaid bills
reserve = selected account's savings_reserves.reserve_cents, or 0
postedBalance = SUM(selected account's ledger delta_cents)
safeToSpend = postedBalance - relevantBills - reserve
```

All arithmetic stays in exact `long` cents with `Math.subtractExact`. The reserve is a **planning deduction**, not a transfer and not a change to posted balance. A negative number is a **projected shortfall**, not clamped to zero. The estimate must not prevent an otherwise valid expense; affordability is decided only from **posted** account balance.

### 12.2 Read-consistent snapshot

Calculate balance, bill subtotal and reserve from a **consistent SQLite read** (one DB handle/read transaction or equivalent serialized snapshot) so a posted expense/bill settlement cannot result in mixing old and new totals. Use the shared executor to avoid main-thread work and synchronize with committed writes. Never just subtract from a cached TextView to refresh the display.

Illustrative pure Java calculation:

```java
public static long calculateSafeToSpendCents(
        long balanceCents, long dueBillsCents, long reserveCents) {
    if (balanceCents < 0 || dueBillsCents < 0 || reserveCents < 0) {
        throw new IllegalArgumentException("invalid_snapshot");
    }
    return Math.subtractExact(
        Math.subtractExact(balanceCents, dueBillsCents), reserveCents);
}
```

### 12.3 Test cases

| Scenario | Expected |
|---|---|
| R3,000 − R2,200 − R300 | **R500.00** and complete breakdown |
| R800 − R1,100 − R100 | **−R400.00**, labelled projected shortfall |
| Bill due today, overdue or exactly day+30 | Included in unpaid total |
| Bill due day+31 | Excluded |
| Bill PAID or CANCELLED | Excluded |
| Bill on different Spending account | Excluded |
| Reserve changes from 0 to R300 | Balance unchanged; estimate decreases R300 |
| Transfer from Spending to Savings | Spending estimate falls with its posted balance; total virtual money conserved |
| Fully settled R2,200 bill from prior example | Balance R800; bill total 0; estimate remains R500 |
| No selected Spending account | Clear empty state; no invented financial amount |

### 12.4 Dashboard refresh

After **every** successful funding, expense, transfer, bill create/edit/cancel/settle, Spending switch or reserve change, reload the dashboard from SQLite. Also refresh on screen return, unlock and when the local calendar day changes. Provide loading, empty, normal, shortfall and error states. Show posted balance and estimate as **separate labels**, with the 30-day coverage explanation and a “View breakdown” route.

**M09 exit gate:** AC-08–AC-10, FR-24, FR-26–30 and FT-09/11/12/15 pass, including inclusive dates and negative result wording.

---

## 13. XML UI implementation and navigation contract

### 13.1 Screen-to-file map (exact approved inventory)

| Design screen | Activity | XML resource | Main action |
|---|---|---|---|
| UI-01 | `GateActivity` | `activity_gate.xml` | route Setup vs Unlock |
| UI-02 | `SetupActivity` | `activity_setup.xml` | create local profile |
| UI-03 | `UnlockActivity` | `activity_unlock.xml` | unlock / erase demo data |
| UI-04 | `DashboardActivity` | `activity_dashboard.xml` | inspect balance + Safe-to-Spend |
| UI-05 | `AccountsActivity` | `activity_accounts.xml` | view/select accounts |
| UI-06 | `CreateAccountActivity` | `activity_create_account.xml` | create Spending/Savings |
| UI-07 | `AccountDetailActivity` | `activity_account_detail.xml` | inspect one account |
| UI-08 | `FundingActivity` | `activity_funding.xml` | prepare demo funding |
| UI-09 | `ExpenseActivity` | `activity_expense.xml` | prepare simulated expense |
| UI-10 | `TransferActivity` | `activity_transfer.xml` | prepare transfer |
| UI-10 review | `TransferReviewActivity` | `activity_review_transfer.xml` | confirm/cancel persisted intent |
| UI-11 | `HistoryActivity` | `activity_history.xml` | paginated account history |
| UI-12 | `TransactionDetailActivity` | `activity_transaction_detail.xml` | inspect operation |
| UI-12A | `ReceiptActivity` | `activity_receipt.xml` | display saved receipt |
| UI-13 | `BillsActivity` | `activity_bills.xml` | list bills by state |
| UI-14 | `BillEditorActivity` | `activity_bill_editor.xml` | create/edit unpaid bill |
| UI-15 | `BillDetailActivity` | `activity_bill_detail.xml` | details / settle full bill |
| UI-16 | `ReserveActivity` | `activity_reserve.xml` | edit planned reserve |
| UI-17 | `BreakdownActivity` | `activity_breakdown.xml` | calculation explanation |
| UI-18 | `SettingsActivity` | `activity_settings.xml` | lock, reset, data policy |

**Shared review note:** Phase 3 explicitly names a transfer review screen; funding, expenses and bill settlement also require **review before posting** through the common persisted-intent protocol. Either reuse a carefully parameterized review Activity or implement specific equivalents, while preserving the same confirmed receipt rules. If introducing a new class, record its UI responsibility and test it.

### 13.2 Minimum UI states

Every screen that loads persistence must support `LOADING`, `CONTENT`, `EMPTY` (if legitimate), `ERROR`, and `LOCKED` transitions as applicable. Forms additionally support `VALIDATION_ERROR` and `SUBMITTING`; Review supports `PREPARED`, `CONFIRMING`, `COMPLETED`, `CANCELLED` and retry/unexpected-outcome resolution.

Don't invoke UI setters from executor threads. Do not allow a delayed callback to populate a now-locked screen. On rotation save unsent form text in `onSaveInstanceState`, but use SQLite's `operation_intents` to persist money confirmations.

### 13.3 XML guidelines

- One readable screen title, visible labels for all `EditText`s, meaningful error messages and XML string resources.
- Touch targets roughly **48 dp**; flexible height and scrolling; support large text and TalkBack. Do not lock orientation to hide layout problems.
- Use `inputType="numberDecimal"` for unsigned amount entry but still perform strict parsing, and `numberPassword` for PIN. `inputType` is **not** authoritative validation.
- Do not use colour alone to indicate a projected shortfall or a successful transfer.
- Keep a visible **DEMO / simulated money** notice on dashboard, review, funding, receipts and onboarding.
- Design your own branding rather than imitating a real bank's official application.
- Show real (virtual) values from the repository, not hard-coded screenshots or Activity-local balance increments.

Example dashboard content hierarchy, following Phase 3 wireframe:

```text
DashboardActivity (vertical scroll)
  Toolbar: RandNest + Lock
  DemoNotice: "DEMO • all transactions are simulated"
  SpendingAccountSelector
  PostedBalanceCard: R...
  SafeToSpendCard: estimate OR projected shortfall
  ViewBreakdown action
  QuickActions: Add funds / Expense / Transfer
  UnpaidBillPreview
  RecentHistoryPreview
  Navigation: Home / Accounts / Bills / History / Settings
```

### 13.4 Routing and parameters

Pass stable UUID IDs between Activities (for example, `EXTRA_ACCOUNT_ID`, `EXTRA_INTENT_ID`, `EXTRA_OPERATION_ID`, `EXTRA_BILL_ID`), then **look up the underlying record and validate access** in service/repository. Never pass a supposedly authoritative balance via Intent extras. After lock, clear protected Activity history and route to Unlock. Back from Review cancels or preserves PREPARED only according to the Phase 3 explicit recovery path, never posts automatically.

---

## 14. Privacy, backup rules, error handling and stability

### 14.1 Demo application disclosure

Use the exact idea consistently: **“RandNest is a banking simulation. Balances and transfers are virtual. No real funds move.”** The Safe-to-Spend result is an **estimate based on manually entered commitments**, not financial advice or a spending guarantee. All examples and screenshots use fictional accounts and money.

### 14.2 Storage and backup

Store SQLite in Android's **app-private internal storage**, not on shared external storage. Set `allowBackup=false` and configure **both** legacy and modern data-extraction rules from Phase 3 §9.4. Include explicit exclusions for cloud backup and device transfer; note that Android versions and manufacturers may behave differently. The official platform docs also describe **cross-platform transfer** configuration from Android 16 QPR2/API 36.1; investigate whether that mode is applicable on the test matrix before claiming universal non-exportability. Do **not** treat an XML file's existence as a successful restore/transfer test.

Suggested first pass resources copied from the approved design:

`res/xml/legacy_backup_rules.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<full-backup-content>
    <exclude domain="root" path="." />
</full-backup-content>
```

`res/xml/data_extraction_rules.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<data-extraction-rules>
    <cloud-backup><exclude domain="root" path="." /></cloud-backup>
    <device-transfer><exclude domain="root" path="." /></device-transfer>
</data-extraction-rules>
```

These are **proposed implementation artifacts requiring Android device verification**; additional extraction modes must be evaluated if used, and database/verifier exposure must be audited. No app-managed export or restore in MVP.

### 14.3 Error handling contract

| Failure | User-facing text pattern | Data outcome |
|---|---|---|
| Invalid amount | “Enter an amount greater than R0.00 with at most two decimal places.” | No operation posted |
| Insufficient posted balance | “Insufficient simulated funds in this account.” | No operation posted |
| Same transfer account | “Choose two different accounts.” | No operation posted |
| Invalid Spending selection | “Choose a Spending account.” | No setting/bill/reserve change |
| Bill already paid/cancelled | “This bill has already been settled or cancelled.” | No second payment |
| Duplicate same-intent confirm | “Already posted — showing your receipt.” | Original receipt; no new rows |
| Storage failure | “Could not save. Check the transaction status before retrying.” | Resolve by ID, no unsupported success claim |
| Locked session | “Unlock RandNest to continue.” | No protected navigation or posting |
| Overflow / cap | “Amount is outside the supported demo range.” | No posting |

Do not log raw SQL with user fields, PIN-derived bytes or financial notes. Use `try/finally` for transactions and try-with-resources for cursors. Show real errors only after verifying database state; interrupted commits may have an uncertain UI result.

### 14.4 Responsiveness and performance

Use a shared worker executor for all disk operations and PIN verification. A 10,000-entry history fixture should still load pages of 50 entries without freezing the UI. Benchmark on API 26 and API 36; if query limits are exceeded, profile SQL indexes and app thread scheduling first. Do not add a balance cache without redesigning ledger reconciliation. Support rotation and low-memory process restarts.

---

## 15. M10 — Integration, quality and owner demo walkthrough

### 15.1 End-to-end walkthrough (synthetic data only)

1. Install fresh debug app; confirm **RandNest / DEMO** appears. Create profile with a six-digit PIN.
2. Create `Everyday Spending` (`SPENDING`) and `My Savings` (`SAVINGS`); both begin at **R0.00**.
3. Add **R3,000.00** simulated money to Spending. Confirm exactly one funding receipt and posted balance R3,000.00.
4. Add a **R2,200.00** unpaid bill due within 30 days. Confirm account balance is still R3,000.00.
5. Set reserve **R300.00** for Spending; confirm balance stays R3,000.00.
6. Open Dashboard and Breakdown. Expect posted balance **R3,000.00**, unpaid due bills **R2,200.00**, reserve **R300.00**, Safe-to-Spend **R500.00**.
7. Transfer **R350.00** from Spending to Savings after review. Expect Spending **R2,650.00**, Savings **R350.00**; combined balance R3,000.00; no duplicate rows.
8. Open History on each account. Confirm the same transfer operation produces the appropriately signed leg; open its detail and receipt.
9. Try confirming the **same** transfer intent again; expect the original receipt, not another R350 movement.
10. Try an **R5,000.00** expense from Spending; expect insufficient funds and unchanged balances.
11. Settle the R2,200 bill with simulated funds. Expect Spending **R450.00**, bill PAID, Savings R350.00, and Safe-to-Spend **R150.00** (R450 − R0 unpaid − R300 reserve).
12. Put device in airplane mode; force-close and reopen. Unlock and verify the same ledger, bill, reserve and balances.
13. Lock manually; background longer than 60 seconds and return; verify no protected screen exposure without PIN.
14. Cancel a reset and verify data survives. Then perform typed-confirmation reset and verify setup starts with empty demo data.

**Important:** Steps 6 and 11 form two consistent milestones within *this* walkthrough. The original design's separate **R800/R500** bill-settlement illustration assumes **no prior R350 transfer**. Do not mix example states when checking test results.

### 15.2 Phase 4 provisional acceptance map

| Source | Phase 4 evidence |
|---|---|
| `AC-01` | M02 setup/unlock and restart proof |
| `AC-02–AC-06` | M04–M06 funding/expense/transfer, retry and ledger tests |
| `AC-07` | M07 bill-create-no-debit proof |
| `AC-08–AC-10` | M08–M09 calculation/settlement/shortfall tests |
| `AC-11` | M10 airplane-mode persistence/relaunch proof |
| `AC-12` | Screenshot review of simulation notices |
| `FR` MVP MUSTs | See Phase 3 §12.4 per-requirement traceability; mark each implemented/tested |
| `NFR-01–18` | See Phase 3 §12.5; record checks and remaining Phase 5 obligations |
| `BR-01–20` | See Phase 3 §12.6; ensure implemented rules have tests |
| `FT-01–16` | See Phase 2 §11.3 and Phase 3 §12.1; store test names and results |

### 15.3 Phase 4 completion gate (G-IMPLEMENT)

**All of the following are required before declaring Phase 4 complete:**

- [ ] Fresh clone/check-out builds reproducibly with documented tool versions.
- [ ] All MVP activities and resources compile; no Kotlin, Room, real-bank APIs or unapproved permissions.
- [ ] Schema v1 matches Phase 3, FK checks and integrity checks pass on device.
- [ ] Every MVP MUST requirement has linked code and at least one passing test/manual check.
- [ ] All funding/expense/transfer/settlement postings persist correctly, atomically, without duplicate submission.
- [ ] Account balances reconcile to signed ledger entries; no negative posted balances.
- [ ] Bills, reserves and Safe-to-Spend respect the 30-day horizon and do not double count.
- [ ] PIN onboarding, unlock, session lock, restricted navigation and reset work.
- [ ] Successful operations have persisted inspectable receipts and history.
- [ ] Airplane mode/restart tests pass; there is no internet dependency.
- [ ] App-private storage/backup policies are configured, with remaining device transfer checks flagged for Phase 5.
- [ ] UI has clear DEMO notices, accessible labels and understandable error states.
- [ ] Known defects are recorded; **no open critical data-integrity defect** is accepted for this gate.
- [ ] Git commits, test output, demo screenshots and this updated checklist are available.

Passing this gate means **feature-complete candidate ready for independent comprehensive Phase 5 testing**, not a published or production-secure banking product. Phase 5 retains final security, compatibility, load, restore/transfer and acceptance-test authority.

---

## 16. Initial backlog — tickets a second developer can pick up

Use this format when creating issues:

```text
Ticket: RN-Mxx-Tyy
Linked source: FR-xx / BR-xx / AC-xx / Phase 3 section
Outcome/user-visible result:
Files/classes to create or modify:
Inputs and guards:
SQLite/transaction effects:
UI loading/success/error/cancel behaviour:
Automated tests (name and expected values):
Device/emulator steps:
Dependencies:
Definition of done and evidence:
```

**Starter tickets:**

| Ticket | Work | Depends on | Deliverable |
|---|---|---|---|
| RN-M00-T01 | Create Java Views project and Git repository | None | Clean build and launcher |
| RN-M00-T02 | Add test dependencies and API 26/36 AVDs | T01 | Starter tests runnable |
| RN-M01-T01 | Convert exact Phase 3 DDL into `SchemaV1` | M00 | 9 tables + 5 indexes |
| RN-M01-T02 | `RandNestDbHelper` and FK/integrity tests | T01 | On-device DB proof |
| RN-M02-T01 | Create PIN KDF utility and unit tests | M01 | Salting/verifier test |
| RN-M02-T02 | Atomic profile setup and lockout repository | T01 | Auth persistence |
| RN-M02-T03 | Gate/Setup/Unlock + session state | T02 | First-use flow |
| RN-M02-T04 | User-confirmed transaction-safe reset | T03 | Erase proof |
| RN-M03-T01 | Account model/repository and selection | M02 | Spending/Savings creation |
| RN-M03-T02 | Account screens + zero-balance state | T01 | Persistent accounts |
| RN-M04-T01 | MoneyParser, MoneyFormatter, exact-cents tests | M00 | Pure Java unit suite |
| RN-M04-T02 | Canonical payload/intent preparation | M01, M03, T01 | Persisted PREPARED intent |
| RN-M04-T03 | Posting service for funding/expense | T02 | Atomic one-leg entries |
| RN-M05-T01 | Transfer two-leg SQL transaction | M04 | Atomic transfer |
| RN-M05-T02 | Double-confirm and fault-injection tests | T01 | Idempotency proof |
| RN-M06-T01 | History paging and detail/receipt views | M04–M05 | Auditable ledger UI |
| RN-M07-T01 | Bill model, editable UNPAID rules and list | M03 | Planned bills |
| RN-M08-T01 | Atomic linked bill payment | M05, M07 | One linked settlement |
| RN-M09-T01 | Reserve and Safe-to-Spend with injected Clock | M03, M07–M08 | Tested exact estimate |
| RN-M09-T02 | Dashboard/breakdown and refresh | T01 | End-to-end planning UI |
| RN-M10-T01 | Offline demo, accessibility, backup review | All | G-IMPLEMENT evidence |

The task ID is stable. If scope changes, create a new ticket and record the approving decision rather than silently editing a completed ticket.

---

## 17. Risks and recovery actions during coding

| Risk / symptom | Likely cause | Correct response |
|---|---|---|
| UI shows changed balance but no ledger event | Activity-local arithmetic | Remove UI-derived balance; load from SQL ledger |
| Partial transfer debit without credit | Two independent writes/no transaction | Route both through one `beginTransaction` and test rollback |
| Same transfer posts twice after rotation | New UUID on each Confirm | Persist intent ID before review; use same ID on retries |
| Prepared request vanishes on process death | Kept only in `Bundle` | Recover `PREPARED` from `operation_intents` |
| Bill payment reduces estimate twice | Paid bill still deducted | Link settlement + PAID in same transaction; bill subtotal UNPAID only |
| Overdue bill unexpectedly excluded | Query has `due_date >= today` filter | Only enforce upper bound `<= today+30` for UNPAID bills |
| API 26 crash on date/KDF | Unverified provider/API code | Reproduce on min API, isolate and test provider/LocalDate availability |
| Freeze on button tap | SQLite or PBKDF2 on main thread | Move work to executor; update UI via main Handler |
| Protected balance reappears after Lock | Activity stack/callback bypass | Guard all protected Activities and stale callbacks |
| Users' data restored across device unexpectedly | Backup policy insufficient | Verify actual backup/transfer modes; adjust XML only with tested change |
| Schema upgrade erases balances | `onUpgrade` drops tables | Require explicit migration; test with populated v1 fixture |
| Java code not matching design | Unrecorded architectural shortcut | Use change-control process and update requirements/test trace |

---

## 18. Decisions frozen for the first coding increment

| ID | Decision | Status |
|---|---|---|
| I4-01 | Start from **Phase 3 schema v1 unchanged** | Frozen by approved design |
| I4-02 | Start with **M00 only**, prove toolchain before features | Implementation sequencing |
| I4-03 | Prefer **single shared executor** for DB operations initially | Approved design; benchmark later |
| I4-04 | Use deterministic fixed-field canonical JSON v1 for intent payload | Implementation-level detail proposed in this document; review at M04 gate |
| I4-05 | Use transactional child-to-parent SQL deletion for reset | Phase 3 selectable option fixed here; verify at M02 gate |
| I4-06 | Source of balances = signed entries; no account balance cache | Frozen by approved design |
| I4-07 | One-time bills only; full linked payment only | Frozen by approved design |
| I4-08 | Review and persist all posting intents; recovery is manual | Frozen by approved design |
| I4-09 | Build only **virtual** funding/expenses/transfers, not external banking operations | Frozen scope |
| I4-10 | Do not declare Phase 4 done on the basis of documentation alone | Required evidence rule |

If I4-04 (canonical JSON shape), note/category length limits or any UI shared-review implementation differs from Phase 3's intentionally open detail, document the agreed convention in the code and its tests. No change is required to approved user goals to choose these implementation details.

---

## 19. Sources, QA checklist and document record

### 19.1 Official Android references

- [Android Gradle plugin 8.13 release notes](https://developer.android.com/build/releases/agp-8-13-0-release-notes) — approved AGP/JDK/Gradle baseline.
- [Android SQLiteOpenHelper reference](https://developer.android.com/reference/android/database/sqlite/SQLiteOpenHelper) — schema/configuration lifecycle.
- [Android SQLiteDatabase reference](https://developer.android.com/reference/android/database/sqlite/SQLiteDatabase) — transactions and FK constraints.
- [Android app architecture guidance](https://developer.android.com/topic/architecture) — separation of concerns and UI/data responsibilities.
- [Android processes and threads](https://developer.android.com/guide/components/processes-and-threads) — keep expensive work off UI thread.
- [Android backup/restore rules](https://developer.android.com/identity/data/autobackup) — legacy/cloud/device/cross-platform mechanisms; test policy.
- [Android `SecretKeyFactory`](https://developer.android.com/reference/javax/crypto/SecretKeyFactory) — PBKDF2 provider/API compatibility.
- [AndroidX Test release notes](https://developer.android.com/jetpack/androidx/releases/test) — instrumentation dependencies.

### 19.2 Review checklist for this *document*

- [x] Uses approved Phase 1–3 inputs and explicit versioned handoff.
- [x] No real-bank scope; Java/XML/platform SQLite only.
- [x] Contains build environment and reproducible Gradle examples.
- [x] Each implementation milestone has dependencies and exit criteria.
- [x] Schema authority, balance query and migrations defined.
- [x] Exact-cents parsing, validation, intent/ledger/receipt protocol detailed.
- [x] Transfer and bill-settlement atomicity, retry, fault injection addressed.
- [x] Auth/PIN/reset, session, backup, no-private-data-in-logs requirements addressed.
- [x] Screen map, UI states and route parameters specified.
- [x] End-to-end demo walkthrough includes verifiable expected values.
- [x] Testing/traceability, risk controls, phase completion gate and backlog included.
- [ ] **Project owner approves this implementation guide.**
- [ ] **Real Android Studio project, code, emulator tests and milestone evidence exist.**

### 19.3 Formal status record

| Field | Value |
|---|---|
| Requirements baseline | RN-SRS-001 v1.0 — **approved** |
| Planning baseline | RN-PLAN-002 v1.0 — **approved** |
| System design baseline | RN-DES-003 v1.0 — **approved** |
| Implementation document | RN-IMP-004 v1.0 — **awaiting owner review** |
| Working app status | **Not implemented in this documentation step** |
| Next action after approval | **Open Android Studio and execute M00**, then progress sequentially |
| Next SDLC document | `05_TESTING.md` after a feature-complete implementation candidate |

### 19.4 Change log

| Date | Version | Summary | Approval status |
|---|---|---|---|
| 2026-11-18 | 1.0 | Detailed coding playbook aligned to approved Phases 1–3. | Pending owner review |

---

**Handoff note:** A competent developer can use this document together with the prior three documents to implement RandNest without inventing financial rules, schema or acceptance criteria. The project owner's first executable action is **M00: Java/XML project skeleton + clean Gradle build**. Treat every later milestone as a separate practical exercise with tests and owner evidence, not a race to complete UI screens.
