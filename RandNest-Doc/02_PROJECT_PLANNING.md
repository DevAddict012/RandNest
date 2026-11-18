# RandNest — SDLC Phase 2: Project Planning & Feasibility

> **Project:** RandNest (working name)  
> **Tagline:** Your money. Your plan. Your control.  
> **Document ID:** RN-PLAN-002  
> **Version:** 1.0 — Proposed project plan  
> **Prepared:** 18 November 2026  
> **Status:** For project-owner review and approval  
> **SDLC phase:** 2 of 6 — Project Planning & Feasibility  
> **Previous phase:** [01_REQUIREMENTS_ANALYSIS.md](./01_REQUIREMENTS_ANALYSIS.md) — requirements approved by the project owner in conversation on 18 November 2026  
> **Next phase:** 03_SYSTEM_DESIGN.md — System Design  
> **Audience:** Project owner, Java/Android developer, tester, reviewer, future maintainer

---

## 1. Purpose, authority and reading instructions

### 1.1 Purpose

This plan explains **how, in what order, with what resources, at what estimated effort, and under what controls** the approved RandNest requirements will be delivered. A developer should be able to derive a practical task board, project schedule, testing plan and release checklist from this document without guessing the scope or priorities.

It is deliberately **not** the final schema, UML package diagram, Java class design, activity navigation map or XML wireframe. Those are Phase 3 deliverables. It also is not permission to modify the approved Phase 1 requirements silently.

### 1.2 Source-of-truth hierarchy

1. **RN-SRS-001, Phase 1**: user needs, all `FR`, `NFR`, `BR` and `AC` requirements, scope and constraints.
2. **RN-PLAN-002, Phase 2 (this file)**: work scope, schedule, resources, feasibility, risk, quality gates and proposed decisions.
3. **Phase 3 design**, once approved: technical implementation contracts that satisfy Phase 1 and Phase 2.
4. **Code and tests**, once built: verifiable implementation, never the authority for silently changing requirements.

If documents disagree, the developer must raise a change request rather than choose whichever interpretation is easier. **Phase 1 has been approved as the requirements baseline**, but its explicitly open questions still require decisions; approval does not magically turn every proposed technical default into a tested fact.

### 1.3 Status vocabulary

| Status | Meaning |
|---|---|
| **Approved** | Explicitly stated by the owner or fixed by approved requirements. |
| **Proposed** | Recommended planning default; requires owner approval at the Phase 2 gate. |
| **Design pending** | Outcome agreed, implementation mechanism to be decided in Phase 3. |
| **Deferred** | Not required for the MVP; revisit only through a later release or approved change. |
| **Blocked** | A missing dependency prevents the next work item from starting safely. |

### 1.4 Executive summary

RandNest is an **offline-first, single-user Android banking simulator and personal finance planner** using **Java, XML views and Android's built-in SQLite**. The portfolio MVP will let a user create local virtual accounts, add demo funds, post expenses, transfer virtual funds between their **own** accounts, view posted activity, manage unpaid one-time bills, set a planned savings reserve and see a transparent **30-day safe-to-spend estimate**. It will **never** process real money, connect to a real bank, or solicit authentic banking credentials.

**Recommended delivery strategy:** one developer, small vertical increments, end-to-end tests for money movements, clear milestone reviews, no paid external backend, and a contingency allowance for device/API issues. The project is feasible as a **portfolio simulator**, not as a licensed, production banking system.

---

## 2. Business case and expected value

### 2.1 Problem to solve

A displayed balance is not the same as discretionary money. Users may forget near-term commitments and mistake their account balance for spendable funds. RandNest addresses the misunderstanding by showing **posted money, outstanding bills and a separate savings planning deduction**. It also provides a safe learning environment for common banking workflows.

### 2.2 Desired outcomes

| Business outcome | Delivery evidence | Linked requirement |
|---|---|---|
| Demonstrate banking actions without real funds | Demo deposits, expenses and internal transfers can be completed and audited. | OBJ-01; FR-10–18 |
| Prevent virtual-money inconsistency | All balances reconcile; transfers are atomic; duplicates are prevented. | OBJ-02; BR-01–08; NFR-03–04, 13 |
| Help plan near-term commitments | One-time upcoming/overdue bills and their states are visible. | OBJ-03; FR-20–23 |
| Explain spendable money | Estimate follows the approved formula; negative results show a shortfall. | OBJ-04; FR-24, 26–30; BR-11–15 |
| Operate without a server | MVP works in airplane mode and persists after restart. | OBJ-05; NFR-01–02 |
| Create a credible developer portfolio piece | Readable code, documentation, traceable tests, screenshots and a runnable build. | OBJ-06; NFR-14–15 |

### 2.3 Success metrics — product

- **100%** of mandatory functional requirements (`MUST`) are implemented, or an owner-approved change explicitly removes/reclassifies one.
- **12/12** acceptance scenarios `AC-01`–`AC-12` pass at the Phase 5 release gate.
- **0** known critical/major financial consistency defects at release: missing or duplicate postings, incorrect balance reconciliation, or partially committed transfers/settlements.
- **100%** of core MVP workflows function in airplane mode.
- The safe-to-spend worked example returns **R500.00** from R3,000.00 − R2,200.00 − R300.00 and displays the calculation components.
- The app repeatedly and clearly labels balances and transfers as **simulated**.

### 2.4 Success metrics — portfolio

- Repository contains a clear README, prerequisite/setup steps, sample walkthrough, screenshots and limitations.
- Another developer can build and run the app using documented Android Studio/SDK instructions.
- Source contains Java code for controllers/business logic, XML layouts and native SQLite database handling; no Kotlin application code.
- At least a representative set of financial rules have automated tests, and all mandatory acceptance scenarios have recorded manual results.
- Documents 1–6 are internally consistent and link requirements to designs and test cases.

**Not a success metric:** the number of screens, animations, added features or a Play Store listing. Reliability and clarity are more important than imitating a real bank's size.

---

## 3. Scope baseline and prioritisation

### 3.1 Mandatory MVP product scope

The **minimum releasable scope** is exactly the Phase 1 `MUST` requirements:

1. **Local profile and access:** onboarding, display name, appropriate app unlock, and user-confirmed local reset (`FR-01`–`FR-04`).
2. **Virtual accounts:** create and display owned Spending/Savings accounts, correct balances, selected Spending account, unique IDs (`FR-05`–`FR-08`).
3. **Virtual money movement:** demo funding, manual expenses, transfers only between owned virtual accounts, validations, audit records and transfer confirmation (`FR-10`–`FR-15`).
4. **History:** per-account newest-first list and detailed posted transaction view (`FR-17`–`FR-18`).
5. **Bills and planning:** one-time bills, upcoming/overdue/paid states, edit/cancel unpaid bills, settle via one linked expense, reserve (`FR-20`–`FR-24`).
6. **Safe-to-spend:** clear display, approved formula, breakdown, shortfall support and refresh on changes (`FR-26`–`FR-30`).
7. **Safety and feedback:** simulation disclosure, no authentic banking credentials, committed success/failure feedback (`FR-32`–`FR-34`).
8. **Quality:** all 18 `NFR`s and all applicable `BR`s must be accounted for; see Section 11.

This includes **multiple owned virtual accounts**, but not separate people's accounts, shared wallets, external beneficiaries or bank-to-bank payments.

### 3.2 Features reserved for a later release

These are explicitly outside the MVP acceptance gate unless the owner issues a formal change request:

| Deferred item | Phase 1 reference | Why deferred |
|---|---|---|
| Rename accounts | FR-09 (`SHOULD`) | Not essential to demonstrate accurate transfers and balances. |
| Audited reversal of posted mistakes | FR-16 (`SHOULD`), DEC-12 | Significant transaction-rule design; immutable posting remains required. |
| Transaction search and filters | FR-19 (`SHOULD`) | Basic newest-first history is enough for MVP. |
| Named savings goals | FR-25 (`SHOULD`) | Keep one planning reserve without allocating the same money twice. |
| Monthly/category analytics | FR-31 (`SHOULD`) | Introduces report/query and chart work without changing core value. |
| Repeating bills and reminders | Phase 1 Section 5.2 | Requires scheduling, recurring-state logic and possibly notification permissions. |
| Variable planning horizon | Phase 1 Section 5.2 | Fixed 30 calendar days is testable and easier to explain. |
| Report export | Phase 1 Section 5.2 | Requires a separate privacy/share threat review. |
| Biometrics, multi-currency, cloud sync | Phase 1 Sections 5.3–5.4 | Adds compatibility, security, sync or exchange-rate complexity. |

### 3.3 Explicit exclusions

No real funds; no transfers to third parties; no banking APIs; no external authentication service; no Firebase, remote database or server for the MVP; no card/payment credentials; no financial advice or regulatory claims; no lending, investment, KYC/AML or online user accounts.

### 3.4 Scope-control rule

A “small improvement” still counts as scope if it introduces new data, rules, permissions, screens or test cases. Add it to the backlog, assess impact, and **do not start it before MVP completion** unless approved as a requirement change.

---

## 4. Project delivery approach

### 4.1 Six-stage SDLC model

| Phase | Purpose | Exit evidence |
|---|---|---|
| **1. Requirements Gathering & Analysis** | Define users, problem, scope and acceptance conditions. | Owner approves RN-SRS-001. **Completed.** |
| **2. Project Planning & Feasibility** | Decide approach, schedule, resources, risks and feasibility. | Owner approves this document and closes release-blocking decisions. **Current.** |
| **3. System Design** | Detail architecture, ERD/schema, contracts, navigation, screens and security design. | Approved `03_SYSTEM_DESIGN.md` and traceability map. |
| **4. Implementation** | Build tested, integrated Java/XML/SQLite functionality in increments. | Feature-complete version with passing component/integration checks. |
| **5. Testing & Validation** | Verify all acceptance, integrity, offline, security and device requirements. | Signed test report and no blocking defects. |
| **6. Deployment & Maintenance** | Package portfolio release, README/screenshots, version and maintenance plan. | Reproducible release artifact and approved handoff. |

The six phases are **governance gates**, not an excuse to write all code only once. During Phase 4, work in short iterations and test each increment. Phase 5 is the comprehensive release verification stage; routine tests still happen during implementation.

### 4.2 Recommended working method

- **Solo developer** is the project owner. A second developer or tester may review but is not a dependency.
- Keep a small task board: `Backlog → Ready → In Progress → Review/Test → Done` plus `Blocked`.
- Limit active implementation to **one significant feature at a time**; avoid half-finishing many screens.
- Implement as vertical slices: user action → Java validation and business rule → SQLite write/read → displayed feedback → automated/manual check.
- Use Git from the beginning, with small commits and a stable main branch. Avoid committing test secrets or personal data.
- Review progress at the end of each milestone using the actual demonstration, not just whether code compiles.
- Use short study/research spikes (e.g., 2–4 hours) when an Android/API point is uncertain; record what was learned.

### 4.3 Suggested iteration cadence

For a developer learning parts of Android and SQLite, prefer **1–2 week iterations** with a narrow demonstration target. A feature that cannot be explained and tested end-to-end is not finished. Re-plan unfinished work instead of hiding it in the next milestone.

---

## 5. Feasibility assessment

### 5.1 Technical feasibility

| Area | Feasibility | Conditions / constraints | Assessment |
|---|---|---|---|
| Android Studio + native Android | **Feasible** | Maintain a compatible installed SDK, emulator and JDK. | Low technical risk. |
| Java-only app code | **Feasible** | Choose Java-capable project template and Java-source tests. Kotlin source is not required. | Low technical risk. |
| XML layout-based UI | **Feasible** | Use Android views and XML; no Compose requirement. | Low technical risk. |
| Platform SQLite with Java | **Feasible** | `SQLiteOpenHelper` and `SQLiteDatabase` are supported Android APIs. Database design and lifecycle need care. | Medium risk from correctness, not support. |
| Offline-first operation | **Feasible** | Persist all core state locally and avoid required network calls. | Low–medium risk. |
| Virtual ledger/transfers | **Feasible** | Need atomic database posting, invariants, integer cents and idempotency protection. | **High** implementation attention. |
| Bill settlement and projections | **Feasible** | One payment must not be counted twice; due-date boundaries must be tested. | **High** implementation attention. |
| Local access lock and privacy | **Feasible for a simulator** | Implement a defined unlock policy, protect secrets, address backups and device differences. | Medium–high risk. |
| Real banking system | **Not feasible under this scope** | Would need regulated financial infrastructure, online ledger, security operations and compliance. | **Excluded.** |

**Conclusion:** the specified **portfolio MVP is technically feasible** if implemented incrementally and tested against ledger and planning invariants. “SQLite supports transactions” is not the same as “our transfer code is correct”; that is a quality responsibility.

### 5.2 Operational feasibility

- Users have one app installation, one profile and demo-only data; no cloud registration or customer support is required.
- App should open to useful account and planning information without a network dependency.
- Planned bills are **entered manually**: the app cannot guarantee completeness or real-world accuracy.
- User can reset all local data intentionally; documentation must explain uninstall and backup limitations.
- For demonstration, prepare synthetic data (sample names, virtual accounts and balances), never a real personal bank statement.

**Conclusion:** feasible for an individual on a compatible Android device, provided onboarding and documentation set expectations.

### 5.3 Schedule feasibility

The major uncertainty is **learning/debugging Android lifecycle behavior and financial data correctness**, not drawing screens. For a solo developer investing approximately **12 hours a week**, the working baseline is roughly **196–286 hands-on hours**, plus a **20% buffer**, yielding **235–343 hours** (about **20–29 weeks** at that weekly capacity) after Phase 3 is approved. These are planning estimates, not a commitment or prediction of an exact delivery date.

The owner should adjust capacity for school, exams, employment, available hardware and skill. Working fewer hours changes the calendar; it should not remove tests or weaken accounting rules.

### 5.4 Economic feasibility

| Resource / cost | Expected need | Budget treatment |
|---|---|---|
| Android Studio, Android SDK, platform SQLite | Required | Free to obtain/use. |
| Git and local repository | Required | Free tools available. |
| GitHub public repository | Recommended | Free-tier hosting is sufficient; publishing is optional until release. |
| Android emulator | Required or substitute physical test device | No service fee; demands computer RAM/CPU/disk. |
| Physical Android phone | Optional but beneficial | Use an owned device if available; no purchase mandated. |
| Server, domain, cloud database | Not needed | **R0 planned recurring infrastructure cost.** |
| Google Play developer account | Not needed for portfolio APK/repository | **Excluded from MVP budget.** |
| UI design software | Optional | Use free options or sketches. |
| Developer time | Main investment | Record actual hours and compare with this estimate. |

**Conclusion:** financial costs can remain very low. Computer hardware capability and developer time are the key constraints. Do not promise universal “zero cost”: power, connectivity for SDK downloads and optional hardware have real costs.

### 5.5 Legal, ethical and safety feasibility

- The name/logo must not imitate a licensed bank or imply regulatory authorisation.
- Clearly describe RandNest as a **simulation** with **virtual money**.
- Avoid collection of real account numbers, PANs, bank login credentials or real financial statements.
- Do not present the safe-to-spend estimate as a guarantee or regulated financial advice.
- Check final app name, artwork, fonts and any external assets for permitted use before publishing. **RandNest is a working name, not a registered-brand assertion.**
- A future real-money product would require a completely new feasibility and compliance assessment, not merely connecting an API.

### 5.6 Feasibility decision

**GO, subject to Phase 2 gate:** proceed to detailed design for **the offline educational simulator only**. No external platform, banking partner or paid service is a dependency. Security and data integrity require additional planned testing rather than marketing claims.

---

## 6. Proposed technical and product planning baselines

These items inform estimates and testing. Where appropriate, they are **proposals to approve**, not a substitute for Phase 3 detailed designs.

### 6.1 Technology baseline

| ID | Proposed/approved baseline | State | Reason / follow-up |
|---|---|---|---|
| TECH-01 | Android Studio; native Android app | **Approved** | Owner constraint `CON-01`. |
| TECH-02 | **Java** for all authored app/controller/business logic and tests; no authored Kotlin source | **Approved** | `CON-02`. |
| TECH-03 | **XML views/layout resources**; no Jetpack Compose | **Approved** | `CON-03`. |
| TECH-04 | **SQLiteOpenHelper + SQLiteDatabase**, app-private local DB; not Room | **Approved direction** | `CON-04`; detailed database contracts Phase 3. |
| TECH-05 | **Groovy Gradle** scripts (`build.gradle`), if template selection allows | **Proposed** | Avoid writing Gradle Kotlin DSL when owner prefers no Kotlin. |
| TECH-06 | **Minimum SDK: API 26 (Android 8.0)** | **Proposed** | Manageable compatibility scope; still requires low-API emulator tests. |
| TECH-07 | **Target SDK: API 36** as initial build baseline | **Proposed** | Meets the published 2026 Google Play mobile-app target requirement if later publishing; recheck at actual release. |
| TECH-08 | Compile SDK: an installed, supported stable SDK **at least equal to the selected target** | **Design pending** | Freeze exact version and Android Gradle Plugin/JDK compatibility in Phase 3. |
| TECH-09 | Currency **ZAR**, money represented by signed `long` integer **cents** | **Approved requirement** | `BR-01`; never persisted as `float`/`double` currency. |
| TECH-10 | No `INTERNET` permission or remote API required for MVP | **Proposed** | Reinforces offline-first and minimises permissions. |
| TECH-11 | Test stack: local Java unit tests + Android instrumentation/UI tests, with manual device cases | **Proposed** | Validate business rules separately from UI and device behavior. |
| TECH-12 | Release path: runnable APK + source repository + docs, **not** mandatory Play Store publication | **Proposed** | Appropriate to a portfolio-scale project. |

**Platform version caveat:** Android versions and Play policies change. API 36 is a planning baseline as of **18 November 2026**, not a guarantee of future publishing eligibility. Check the official current policies and tooling again before a public release.

### 6.2 Product baseline decisions

| Source decision | Proposed selection for Phase 2 approval | Consequence |
|---|---|---|
| DEC-01 — Name | **RandNest**, subject to final name/logo checks | Consistent app/package/readme branding; package ID to be set in Phase 3. |
| DEC-02 — Audience | Portfolio reviewers and beginner/student/early-career money planners | Simple language and useful planning flows. |
| DEC-03 — Users | One local profile, with multiple owned virtual accounts | No sign-up server or sharing features. |
| DEC-04 — Currency | ZAR only, cents precision | No FX, no currency conversion. |
| DEC-05 — Account types | Spending and Savings | At least two accounts can demonstrate an internal transfer. |
| DEC-06 — Unlock | **Proposed:** six-digit-or-longer local PIN; salted, deliberately slow password-derived verifier, no plaintext secret, throttled failed attempts, clear reset policy | Detailed KDF, storage, re-lock timing and attack review required in Phase 3. A six-digit PIN alone is not bank-grade protection. |
| DEC-07 — Horizon | Today's local date through **today + 30 calendar days**, inclusive; overdue unpaid bills included | Fixed and testable date rules. |
| DEC-08 — Bills | One-time bills only; no automatic recurring posting | Keeps settlement and projections deterministic. |
| DEC-09 — Savings | One optional non-negative planning reserve per selected Spending account | No automatic movement to Savings. |
| DEC-10 — Devices | Minimum API 26, target 36, plus test matrix in Section 11.6 | Compatibility verified rather than assumed. |
| DEC-11 — Backup | **Proposed:** no app-managed/cloud backup or exports for MVP; explicitly configure platform backup/transfer rules to protect PIN verifier and private records | Android 12+ device-transfer behavior needs testing; `allowBackup=false` alone may not cover all device-to-device paths. |
| DEC-12 — Corrections | Posted entries cannot be silently edited/deleted in MVP; full audited reversal UI deferred (`FR-16`) | Explain limitation; avoid treating reset as an individual-transaction edit. |
| DEC-13 — Bill payment | Default: create **one linked posted expense** atomically with marking the bill paid. Existing-expense linking is optional if design supports it safely | Satisfies `FR-23` without double counting. |

**Important distinction:** a PIN prevents casual access through the normal UI; it does **not** automatically encrypt the database or provide bank-grade security. Phase 3 must explicitly describe the threat model, Android app-private storage, PIN-verifier protection, lock state, and restore/reset behavior. Never store the raw PIN, use unsalted fast hashing as the only check, or put secrets into logs. Because all financial records are synthetic, this modest app-lock baseline is appropriate to evaluate, not to overstate.

### 6.3 Choices intentionally delegated to Phase 3

- ER diagram, foreign keys and indexes; exact transfer and settlement ledger representation.
- Single source of truth for balances; ledger-derived vs transactional cached balance strategy.
- Idempotency/duplicate-submit operation IDs and database uniqueness constraints.
- Exact app navigation, activity/fragment choice, XML layout organization, colors and type scale.
- Local PIN verifier details, error delays/lockouts, re-lock timeout, password-derived key parameters, storage and recovery behavior.
- Precise backup rules for Android 11 and earlier, and Android 12+ cloud/device-transfer channels.
- Source package organization, repository interfaces, migrations and error-handling contracts.
- Date/time formatting, selected-account persistence and transaction ordering tie-break rules.

---

## 7. Team roles and responsibilities

| Role | Assigned to | Responsibilities | Decision power |
|---|---|---|---|
| Product owner | User/project owner | Prioritise requirements, approve scope, respond to design decisions, sign off on milestones. | Final scope/acceptance decisions. |
| Android developer | User/project owner | Write Java/XML/SQLite code, commit, run tests, fix defects, maintain README. | Implementation choices within approved contracts. |
| Analyst / planning advisor | ChatGPT as requested | Help structure requirements, examine trade-offs, review plans and explain concepts. | Advisory; cannot execute asynchronous work or approve on owner's behalf. |
| Tester | Developer initially; optional peer reviewer | Verify scenarios, regressions, compatibility and usability. | Report failures; release accepted by owner. |
| Portfolio reviewer | Future reader/employer | Evaluate clarity, reproducibility, code quality and demo. | No product scope authority. |

**RACI shortcut:** the owner is **accountable and responsible** for every release gate; external reviewers are **consulted** when available; README readers are **informed**. There is no assumed hired team or support service.

---

## 8. Work breakdown structure and effort estimate

### 8.1 Estimation assumptions

- One person owns development and most tests; Java proficiency is assumed, Android/SQLite experience may still be growing.
- Estimates represent **focused engineering hours**, not elapsed calendar hours.
- Hours include implementation and immediate component tests; a separate test-hardening package covers broader regression.
- Phase 1 and Phase 2 documentation effort already spent is excluded; **Phase 3 design work should be estimated separately at its start** if not included in the owner's available time.
- No backend, Play Store submission, paid services, or future-release features are included.
- Unknown device-specific defects and learning time are buffered separately; figures are ranges, not guarantees.

### 8.2 Work packages

| WP | Work package | Linked mandatory requirements / quality | Estimate (hours) | Prerequisite | Evidence of done |
|---|---|---|---:|---|---|
| WP-01 | Android project foundation, build/run, Git, baseline integration | CON-01–08; NFR-14, 17 | **10–14** | Approved Phase 3 build plan | Clean build and launch on selected emulator; first commit. |
| WP-02 | Onboarding, profile, PIN access, local reset | FR-01–04; NFR-05–07, 18 | **12–18** | WP-01, security design | Secure-by-design setup/lock/reset flows tested. |
| WP-03 | Virtual account creation, list, selected account | FR-05–08; BR-18–19 | **12–18** | WP-02, schema | Two account types, IDs and zero opening balance. |
| WP-04 | Posting/ledger foundation and demo funding | FR-10, 13–14; BR-01–04, 08 | **20–28** | WP-03, posting contract | One auditable posting path and reconciliation tests. |
| WP-05 | Manual expenses and input validation | FR-11, 13–14, 34; BR-03–04 | **12–18** | WP-04 | Expense accuracy, insufficient funds and invalid-input tests. |
| WP-06 | Internal transfers and confirmation | FR-12–15; BR-05–08 | **16–24** | WP-04–05 | Atomic same-cents posting, no duplicates, AC-04–06. |
| WP-07 | History list and transaction details | FR-17–18; BR-02, 17 | **12–18** | WP-04–06 | Correct ordering and references per account. |
| WP-08 | One-time bills, edit/cancel, statuses, atomic settlement | FR-20–23; BR-09–10, 20 | **24–34** | WP-05, schema | Complete bill lifecycle; AC-07, AC-09. |
| WP-09 | Reserve, estimate engine and breakdown | FR-07, 24, 26–30; BR-11–16 | **14–22** | WP-03, WP-08 | Correct 30-day boundaries and shortfall; AC-08, AC-10. |
| WP-10 | Dashboard integration, messages, simulation disclosure, accessibility | FR-06, 26, 28, 30, 32–34; NFR-09–11, 16 | **16–22** | WP-03–09 | Complete, understandable offline user journey. |
| WP-11 | Reliability, lifecycle, backup policy, migrations, performance | NFR-01–04, 08, 12–13, 17–18 | **14–22** | Working functional slices | Restart, rollback, upgrade, 10k-entry, backup/reset checks. |
| WP-12 | Formal regression, device testing, bug fixing | AC-01–12, all `MUST`, all NFRs | **24–32** | Feature-complete MVP | Test results, fixed blocking bugs, owner acceptance. |
| WP-13 | Portfolio packaging, README, images, release notes | OBJ-06; NFR-15–16 | **10–16** | WP-12 | Reproducible APK/repo and honest simulation disclaimer. |
| | **Estimated subtotal** | | **196–286** | | |
| | **Risk contingency: 20%** | | **39–57** | | |
| | **Total planned envelope** | | **235–343** | | |

> Estimates deliberately focus on building **the entire mandatory Phase 1 MVP**, not a stripped-down mockup. Future-release work is excluded. Phase 3 design and learning spikes may require additional calendar time; track actuals and revise forecasts at each milestone.

### 8.3 Capacity examples

| Available focused hours / week | 235-hour scenario | 343-hour scenario | Interpretation |
|---:|---:|---:|---|
| 8 | about 30 weeks | about 43 weeks | Sustainable slow-paced learning. |
| 12 | about 20 weeks | about 29 weeks | **Recommended reference capacity** if personally sustainable. |
| 16 | about 15 weeks | about 22 weeks | Faster only if other commitments permit. |

These are **capacity calculations**, not deadlines. The clock starts **after** the required planning/design gates are approved. No work should be shortened by deleting financial consistency tests.

### 8.4 Time tracking

Record in a simple table or issue tracker: `date`, `work package`, `task ID`, `hours`, `result`, `blocker`, `next action`. Each week compare **completed evidence** and remaining tasks, not hours spent alone. If a package exceeds its upper estimate by >25%, identify the cause and reforecast dependencies.

---

## 9. Milestones, order and dependencies

### 9.1 Critical dependency chain

```text
Phase 1 approved requirements
            |
Phase 2 approved plan and release decisions
            |
Phase 3 approved design (schema, ledger, UX, security, tests)
            |
WP-01 Project setup
            |
WP-02 Profile + WP-03 Accounts
            |
WP-04 Ledger foundation
            |
      +-----+----------+
      |                |
 WP-05 Expenses   WP-07 History foundation
      |
 WP-06 Transfers
      |
 WP-08 Bill settlement
      |
 WP-09 Reserve + safe-to-spend
      |
 WP-10 Dashboard end-to-end
      |
 WP-11 Reliability and lifecycle
      |
 WP-12 Full verification
      |
 WP-13 Portfolio release
```

Actual work may overlap where safe: history can be integrated as postings are added, and UI/accessibility checks can run continuously. **Do not implement bills as posted expenses**; they are commitments until settled. The ledger/posting contract is the main critical-path dependency.

### 9.2 Recommended milestone gates

| Gate | Deliverable | Demo / measurable acceptance | Do not proceed until… |
|---|---|---|---|
| **G0 — Plan approval** | This file and decision log | Scope, effort, resources and risks accepted. | Owner approves Phase 2. |
| **G1 — Design sign-off** | Phase 3 design | ERD/schema, ledger/transaction contracts, screen flows, unlock/backup decisions and test map reviewed. | No release-blocking design ambiguity remains. |
| **G2 — App foundation** | Profile and virtual accounts | First-run works; app unlocks; Spending/Savings exist with R0.00 balances; reset is deliberate. | Core access and persistence tests pass. |
| **G3 — Ledger trusted** | Funding, expenses, transfers and history | AC-02–AC-06 all pass; money reconciles before/after restart and double taps. | No known partial/duplicate ledger posting. |
| **G4 — Planning complete** | Bill lifecycle, reserve and safe-to-spend | AC-07–AC-10 pass, including paid-bill no-double-count. | Estimate equals rules for boundary cases. |
| **G5 — Full MVP integrated** | Dashboard and all mandatory workflows | Every `MUST` requirement demonstrable, offline and clear. | Feature checklist and baseline component tests pass. |
| **G6 — Quality gate** | Phase 5 test report | AC-01–AC-12 pass; minimum devices, persistence, security and performance verified. | No critical blocker, serious data-integrity or misleading disclosure defect. |
| **G7 — Portfolio release** | Phase 6 package | Clean-clone instructions, screenshots, README, demo disclaimer and runnable artifact. | Owner can rebuild and demonstrate from documentation. |

### 9.3 Illustrative sequence at 12 focused hours/week

For planning only, a sensible sequence is: foundation/profile/accounts → ledger/funding/expenses → transfers/history → bills → reserve/estimate/dashboard → reliability/regression → portfolio packaging. Expect **20–29 weeks of hands-on build/release work**, subject to actual estimates and complexity, **after Phase 3**. Do not attach arbitrary calendar deadlines without the owner's actual weekly availability and start date.

### 9.4 Demonstration checkpoints

At the end of each gate, save: a short demo script, app screenshots if useful, a test result note, and one tagged Git commit/release marker. Demonstrations should use fully fabricated data.

---

## 10. Backlog and implementation order (developer-ready)

**Priority policy:** `P0` = mandatory for MVP, `P1` = supporting implementation/test/quality work, `P2` = deferred enhancement. All Phase 1 `MUST` items are `P0` regardless of how small or difficult they are.

Each user story below is expressed as **actor → goal → acceptance**, with cross-references to the approved SRS. Detailed UI/DB subtasks are generated after Phase 3 design, not made up prematurely.

| Story ID | User story / capability | Dependencies | Test proof | Priority |
|---|---|---|---|---|
| US-01 | As a new user, I want to create my local profile and unlock it, so I can safely access my simulator. | Design lock and profile fields | AC-01; FR-01–03 | P0 |
| US-02 | As a user, I want to intentionally reset all demo data with a warning. | Profile and DB reset contract | FR-04, NFR-18 | P0 |
| US-03 | As a user, I want Spending/Savings accounts with persistent R0.00 opening balances. | Profile; account design | FR-05–06, 08; BR-19 | P0 |
| US-04 | As a user, I want to choose which Spending account's commitments I am viewing. | Multiple accounts | FR-07, BR-11 | P0 |
| US-05 | As a user, I want to add demo funds and see one traceable posting. | Posting foundation | AC-02; FR-10, 14 | P0 |
| US-06 | As a user, I want to post expenses with exact cents and see my balance decrease once. | Posting foundation | AC-03; FR-11, 13 | P0 |
| US-07 | As a user, I want to transfer only between my own distinct accounts with confirmation. | Two accounts; atomic posting | AC-04–06; FR-12–15 | P0 |
| US-08 | As a user, I want an accurate newest-first history and transaction detail view. | Posted events and timestamps | FR-17–18 | P0 |
| US-09 | As a user, I want to create/edit/cancel upcoming one-time bills. | Spending account; bill contract | AC-07; FR-20–22 | P0 |
| US-10 | As a user, I want to settle a bill once, and keep its payment linked. | Posting + bills; atomicity | AC-09; FR-23 | P0 |
| US-11 | As a user, I want to set a non-negative planned savings reserve without moving money. | Spending-account selection | FR-24; BR-13 | P0 |
| US-12 | As a user, I want a transparent estimate and shortfall for the fixed 30-day window. | Accounts, bills, reserve | AC-08, AC-10; FR-26–29 | P0 |
| US-13 | As a user, I want my dashboard to refresh after changes and communicate successes/failures. | Integrated posting and calculation | FR-30, 34 | P0 |
| US-14 | As a user, I want the app to plainly say all funds are simulated. | Onboarding + money-action screens | AC-12; FR-32–33 | P0 |
| US-15 | As a user, I want the same correct data after closing, restarting or going offline. | Persistent SQLite architecture | AC-11; NFR-01–04 | P0 |
| US-16 | As a reviewer, I want to inspect tests and repeat build instructions. | All milestones | OBJ-06; NFR-14–15 | P1 |
| US-17 | As a user, I may later want reversals/search/goals/analytics. | Separate future release | FR-16, 19, 25, 31 | P2 |

### 10.1 Story template for the actual issue tracker

```text
ID: US-xx / TASK-xx
Title:
Linked requirements: FR-xx, NFR-xx, BR-xx, AC-xx
User outcome:
Preconditions:
Happy path:
Validation/negative paths:
Persistence and restart expectations:
UI feedback and simulation disclosure:
Automated tests:
Manual/device tests:
Dependencies:
Estimate (hours):
Done evidence (PR/commit/screenshot/test report):
Status / owner:
```

### 10.2 Entry criteria for a development task (“ready”)

A task is ready only when its linked requirement, expected behavior, design references, needed preconditions, invalid-input rules and acceptance tests are stated. If the schema or business contract is unresolved, move the task to **Blocked** and resolve the design first.

---

## 11. Quality management and test strategy

### 11.1 Quality priorities

1. **No inconsistency in virtual money** — reliability outranks visuals.
2. **Correct, explainable safe-to-spend** — bills are not money until paid.
3. **Offline persistence** — users do not lose committed work on restart.
4. **Privacy, simulation clarity and access behavior** — no real bank implication.
5. **Usability and accessibility** — a developer and reviewer can follow the workflow.
6. **Maintainability, performance and reproducible build**.

### 11.2 Test layers

| Layer | Tests | Execution timing |
|---|---|---|
| Plain Java unit tests | Cents parsing, overflow rejection, safe-to-spend formulas, date inclusion, validation and pure business functions. | Every relevant commit; automated where possible. |
| Repository/integration tests | SQLite inserts, constraints, unique operation IDs, atomic postings, account reconciliation, linked bill settlement and migrations. | On each persistent-feature milestone. |
| Android instrumentation/UI tests | Onboarding, app lock, forms, dashboard refresh, navigation and simulated transaction confirmations. | Each user-facing milestone; again at Phase 5. |
| Manual exploratory tests | Back/cancel/double taps, rotation/background, error messages, talkback/large text, unexpected input, locked-state navigation. | During feature build and at G6. |
| Device/emulator compatibility | Min API, intermediate API and target API run/restore, keyboard/permissions/back behavior. | Initial app, after security changes, at G6. |
| Performance tests | Dashboard/history responsiveness with up to 10,000 posted entries. | After implementation of history and at G6. |
| Portfolio acceptance | Fresh checkout/build/install, demo without internet, run scripted scenarios and review docs. | G7. |

### 11.3 Priority financial test matrix

| Test ID | Given / action | Expected invariant | Linked source |
|---|---|---|---|
| FT-01 | Funding R1,000.00 once | One funding entry, balance +R1,000.00. | AC-02 |
| FT-02 | Expense R125.75 from R1,000.00 | Exact R874.25 balance. | AC-03 |
| FT-03 | Transfer R350 between R1,000/R200 accounts | Balances R650/R550 and total R1,200 unchanged. | AC-04 |
| FT-04 | Attempt expense/transfer above balance | No balance change and no completed posting. | AC-05; BR-04 |
| FT-05 | Same source/destination or zero/negative amount | Rejection; no transaction. | FR-13; BR-03, 05 |
| FT-06 | Double-submit valid transfer/expense | At most one posting for the same operation. | AC-06; BR-08 |
| FT-07 | Fail/interrupt during transfer DB write | All-or-nothing state on reopening. | NFR-04; BR-07 |
| FT-08 | Add unpaid R2,200 bill | Balance unchanged; bill subtotal includes it only in window. | AC-07; BR-09, 12 |
| FT-09 | R3,000 balance, R2,200 bills, R300 reserve | Estimate = R500 and breakdown visible. | AC-08 |
| FT-10 | Settle R2,200 bill by expense | R800 balance, unpaid bill removed, estimate still R500. | AC-09; BR-10 |
| FT-11 | Bills exceed funds | True negative shortfall, not displayed as covered. | AC-10 |
| FT-12 | Bill overdue, due today, due on day+30, due day+31 | First three relevant (plus overdue); day+31 excluded. | BR-12, 16 |
| FT-13 | End user session then reopen offline | Balance, ledger, bills and reserve remain consistent. | AC-11 |
| FT-14 | Trigger oversized cents input | No integer overflow or posting. | NFR-03; Phase 1 Section 12 |
| FT-15 | Set reserve but do not transfer | Posted balance unchanged. | BR-13 |
| FT-16 | Settlement fails (e.g., insufficient funds) | Bill remains unpaid; no partial expense. | FR-23; NFR-04 |

**Implementation rule:** For every balance-changing operation, verify the account posting and its visible history together. A screenshot of a balance alone is not proof of correct accounting.

### 11.4 Non-functional verification plan

| Group | Required proof |
|---|---|
| Offline (`NFR-01`) | Full scripted MVP flow in airplane mode, fresh app launch after no connectivity. |
| Persistence (`NFR-02`) | Kill process, relaunch and verify ledger, bills, reserve, selection and profile state. |
| Accuracy/atomicity (`NFR-03`–`04`) | Money invariants, cents/overflow, simulated failure/rollback and retry tests. |
| Access/privacy (`NFR-05`–`07`) | Locked route prevention, no raw PIN in DB/logs, no authentic credentials, app-private storage, backup audit. |
| Failure behavior (`NFR-08`, `13`) | Invalid input, DB error, rapid taps and transitions; no duplicates. |
| Usability/a11y (`NFR-09`–`11`) | Discoverable tasks, labels, readable errors, larger fonts, currency/date formats. |
| Performance (`NFR-12`) | At 10,000 entries, ordinary dashboard tasks within approximately 2 seconds on the defined reference test device; avoid UI-blocking DB work. |
| Maintainability/testability (`NFR-14`–`15`) | Separated calculation/persistence/UI responsibilities, named tests and requirement traceability. |
| Simulation/devices/backup (`NFR-16`–`18`) | Disclaimers, API matrix and reset/backup/restore tests. |

### 11.5 Definition of done — individual task

A feature is **Done** only if:

- [ ] It implements the linked SRS requirement and approved Phase 3 behavior.
- [ ] It has clear validation, empty/error states, confirmation and cancellation where applicable.
- [ ] All financial data changes are posted through the approved consistent data path.
- [ ] No sensitive information, real bank identifiers or raw PINs are logged.
- [ ] It behaves correctly offline and persists as required.
- [ ] Focused automated tests pass and manual feature tests are recorded.
- [ ] UI text clearly distinguishes simulated money and estimated planning values.
- [ ] Accessibility basics are considered (labels, descriptions, readable state).
- [ ] Source is committed with meaningful message and docs updated as needed.
- [ ] There are no known critical regressions introduced by the feature.

A feature is **not** done simply because it displays plausible fake data in a screen.

### 11.6 Proposed Android compatibility/test matrix

| Test target | Role | Minimum checks |
|---|---|---|
| **Android 8.0 / API 26 emulator** | Minimum supported OS | App installation, storage, navigation, unlock, transaction/backup behavior compatible with min API. |
| **Android 11 / API 30 emulator** | Pre-Android-12 backup and device behavior | Data restore/reset rules, permission behavior, core workflows. |
| **Android 16 / API 36 emulator** | Initial target API | All acceptance tests, lifecycle, performance reference if device spec recorded. |
| **Android 17 / API 37 or later available stable image** | Optional latest-OS smoke test | Compatibility/regression check; no target claim until tested. |
| **Physical Android handset, if available** | Real hardware usability | Keyboard, touch, relock, rotation, accessibility and offline behavior. |

**Device baselining:** record emulator image, virtual RAM/CPU, app build ID and Android version alongside results. The 2-second `NFR-12` result is meaningful only with a reproducible test setup.

### 11.7 Defect severity and release rule

| Level | Example | Release treatment |
|---|---|---|
| **Critical / S1** | Money duplicated/lost, partial transfer, raw PIN leakage, unsafe real-bank claim | **Block release**; fix and retest. |
| **Major / S2** | Bill settlement double-counts, persistence broken, cannot unlock, essential workflow unusable | **Block release**; fix and retest. |
| **Moderate / S3** | Infrequent layout issue, confusing message without incorrect finances | Record, triage and obtain owner decision. |
| **Minor / S4** | Cosmetic mismatch, nonessential enhancement | May defer with documentation. |

For an educational banking simulator, **financial correctness is never accepted as “just a visual bug.”**

---

## 12. Risk register

**Risk scoring:** Likelihood (L) and Impact (I) each on `1 (low)` to `5 (high)`; score = `L × I`. **15–25 High**, **8–14 Medium**, **1–7 Low**. This is a simple prioritization method, not a quantitative probability model.

| Risk | Event / cause | L | I | Score | Owner | Mitigation | Trigger / contingency |
|---|---|---:|---:|---:|---|---|---|
| R-01 | Two sides of transfer post inconsistently | 3 | 5 | **15 H** | Developer | Single DB transaction and ledger invariant tests; no direct UI balance editing. | Any mismatched reconciliation → freeze feature, fix before more work. |
| R-02 | Rapid taps/retries duplicate entries | 4 | 5 | **20 H** | Developer | In-flight UI guard + persistent operation identity/unique constraint, tested on retry. | Duplicate on tests → release blocker. |
| R-03 | Bill payment deducted twice in estimate | 3 | 5 | **15 H** | Developer | Paid bill linked to one expense; atomic state change; AC-09 test. | Change in estimate differs from BR-10 → stop and repair. |
| R-04 | Scope creep from banking-style extras | 4 | 4 | **16 H** | Owner | Maintain `MUST` vs `SHOULD`; change requests and deferred list. | More than one unapproved feature enters active tasks → backlog freeze. |
| R-05 | PIN/backup behavior gives false privacy assurances | 3 | 5 | **15 H** | Developer | Threat-model lock/restore, limit failed attempts, app-private storage, data-extraction rules, no encryption claims. | Secret appears in logs/backup or locked bypass → block release. |
| R-06 | SQLite schema/migration loses local data | 3 | 5 | **15 H** | Developer | Versioned migration tests, schema design review, reset separate from upgrade. | Upgrade deletes records → block release. |
| R-07 | Developer workload underestimated | 4 | 3 | **12 M** | Owner | 20% buffer, small iterations, track actual vs estimated hours, defer only `SHOULD`. | WP exceeds upper bound by >25% → replan. |
| R-08 | Android tooling/API version mismatch | 3 | 3 | **9 M** | Developer | Freeze known-compatible Studio/AGP/JDK/SDK in Phase 3; early emulator builds. | Build fails after update → pin compatible toolchain. |
| R-09 | Improper dates/time zone yield wrong bills | 3 | 4 | **12 M** | Developer | Local-date rule, overdue and day+30/day+31 boundary tests. | Boundary tests fail → correct date logic. |
| R-10 | Saved balance deviates from transaction ledger | 3 | 5 | **15 H** | Developer | Approved single source of truth and reconciliation; forbid direct updates. | Any divergence → block further money features. |
| R-11 | App blocks or freezes on large ledger | 3 | 3 | **9 M** | Developer | Index/query plan, background DB reads, 10,000-entry test. | Dashboard >2 s reference → profile/optimize. |
| R-12 | Local data lost on reset/uninstall/restore | 3 | 4 | **12 M** | Owner + developer | Confirm reset, warn user, decide platform backup policy and test it. | Unexpected restoration/loss → revise behavior and message. |
| R-13 | User mistakes simulator for a real bank | 2 | 5 | **10 M** | Owner | Simulation disclosure on onboarding, dashboard and money actions; synthetic data only. | Test user uncertain → improve wording before release. |
| R-14 | No real-device/emulator coverage | 3 | 3 | **9 M** | Developer | Minimum API, target API and Android 11 emulator matrix. | Emulator unavailable → reduce promised device coverage; replan. |
| R-15 | “Safe-to-spend” treated as financial advice or guarantee | 3 | 4 | **12 M** | Owner | Use “estimate”, breakdown, assumptions, negative shortfall and simulated balances. | Confusing review feedback → revise content. |
| R-16 | Lost work / broken repository | 3 | 4 | **12 M** | Developer | Git commits, remote push when safe, tagged milestones and verified restore clone. | Local failure → restore from clean checkout. |

### 12.1 Risk review cadence

At every gate, update risk scores, mitigations, incidents and status. Any new risk that could corrupt posted transactions, compromise unlock secrets or mislead users about real payments is an immediate stop-and-review issue.

---

## 13. Security, privacy, backup and data continuity planning

### 13.1 Scope of protection

The application stores demo financial records and a local profile. Although not actual bank data, these still deserve privacy care. The expected threats for this MVP are accidental disclosure from logs/backups, casual app access on an unlocked phone, accidental data loss, malformed input and corrupted ledger state. It is **not** designed to withstand a sophisticated attacker with control of the device or to secure real funds.

### 13.2 Minimum controls

- Keep SQLite in Android's **app-private internal storage**.
- Use **no real banking credentials**; labels and identifiers must be synthetic.
- Any PIN/password verifier must not store the secret in plaintext; use a reviewed salted password-derivation/verifier strategy with rate-limiting and realistic claims.
- Avoid logging balances linked to personal data and never log the unlock secret, salts+verifier together for debugging, or cryptographic keys.
- Recheck lock state when opening protected screens; never rely only on a hidden unlock button.
- Use clear confirmation for reset, with irreversible-data-loss warning.
- Do not equate app-private storage, PIN access or default SQLite with full database encryption.
- Explicitly audit Android backup and device-to-device transfer policies for the chosen target and minimum API range; do not assume a single manifest flag covers every OS and transfer pathway.
- Do not add broad storage, location, SMS, contacts or internet permissions without a separately approved need.

### 13.3 Backup/restore decision to validate

**Default proposal:** no cloud backup, no user export and no app-managed sync in MVP; warn that uninstall/device loss/reset may permanently remove data. Configure backup and `dataExtractionRules`/legacy rules appropriately, then **verify actual behavior on test API levels**. Platform documentation notes that Android 12+ may treat cloud backups and device-to-device transfers differently; test instead of asserting blanket protection.

### 13.4 Future security escalation

If the owner later adds actual money, bank APIs or real customer data, **stop**: new requirements, server-side ledger, comprehensive security review, privacy/legal assessment, access controls and independent testing become mandatory. This portfolio architecture must not be described as production-bank-ready.

---

## 14. Project resources, environment and repository planning

### 14.1 Minimum resource checklist

- [ ] Windows, macOS or Linux computer with Android Studio support and available disk space for SDK/emulators.
- [ ] Android Studio installation, compatible JDK/Gradle plugin/SDK confirmed at Phase 3.
- [ ] At least one usable Android emulator or physical Android device; more images as listed in Section 11.6.
- [ ] Git installed and working; a private or public remote repository chosen (public only after checking files/assets).
- [ ] Git ignore rules for generated builds, local SDK paths, caches, machine-specific config and secrets.
- [ ] Java/XML project template, no authored Kotlin files; no mandatory network-based feature dependency.
- [ ] Markdown editor and screenshots/diagram tool; free tools are sufficient.
- [ ] Test data strictly fictional, including display names and account identifiers.

### 14.2 Proposed repository layout (planning-level)

```text
RandNest/
├── README.md
├── docs/
│   └── sdlc/
│       ├── 01_REQUIREMENTS_ANALYSIS.md
│       ├── 02_PROJECT_PLANNING.md
│       ├── 03_SYSTEM_DESIGN.md              # after Phase 3
│       ├── 04_IMPLEMENTATION.md              # after Phase 4
│       ├── 05_TESTING.md                     # after Phase 5
│       └── 06_DEPLOYMENT_MAINTENANCE.md     # after Phase 6
├── app/                                   # Android Studio app module
├── gradle/                                # Gradle wrapper files
├── build.gradle                           # if Groovy is selected
├── settings.gradle                        # if Groovy is selected
└── .gitignore
```

This is a **document/repository convention**, not the mandated internal Java package structure. Phase 3 specifies packages and class responsibilities.

### 14.3 Branches, commits and release tags

- Maintain `main` as a buildable, demonstration-safe branch.
- Use short-lived feature branches such as `feature/internal-transfers` if useful; a solo developer can also use small direct commits when appropriate.
- Suggested commit form: `feat: add virtual account creation`, `test: cover transfer rollback`, `docs: approve phase 2 plan`.
- Tag important gates (e.g., `g3-ledger-trusted`, `v0.1.0-mvp`) only **after** criteria pass.
- Do not commit compiled artifacts unnecessarily, APK signing keys, local-machine identifiers, real financial details or unapproved third-party assets.

### 14.4 Working artifact list

Phase 2 outputs: this plan, approved decision log, risk register, estimates/milestones and initial backlog. Subsequent phases will add ERD/UML/flows, schema/API contracts, code, tests, defect report, release APK, screenshots and README.

---

## 15. Communication, progress and change control

### 15.1 Progress tracking

At a convenient weekly check-in, record:

| Field | Example content |
|---|---|
| Current gate | `G3 — Ledger trusted` |
| Finished this period | Funding and expense test evidence |
| Hours actual / remaining estimate | `8h actual / 15–20h remaining` |
| Open blockers | SQLite retry behavior not yet decided |
| Top risk changes | R-02 moved high to controlled after tests |
| Next narrowly defined task | Add duplicate-submit integration test |
| Decision needed from owner | Approve lock timeout |

### 15.2 Change request process

An approved requirement may change, but not invisibly:

1. Record `CR-###`, requester, date and why the change is needed.
2. Identify changed SRS IDs (`FR`, `NFR`, `BR`, `AC`) and affected users.
3. Analyse impact on data model, screens, business rules, tests, effort, schedule, security and documentation.
4. Decide: **accept now**, **defer**, or **reject**; owner approves.
5. Update affected versioned documents, backlog and regression tests **before** implementing.
6. Maintain a short decision/change history linking the request to commits or milestones.

**Illustration:** adding transfers to contacts is **not** a UI-only change; it conflicts with “own virtual accounts only” and creates new ownership/payment/security requirements. It requires a fresh scope decision.

### 15.3 Decision record template

```text
Decision ID: DEC-XX
Topic:
Options considered:
Chosen option:
Why:
Impact on scope, design, risks, budget and testing:
Owner:
Decision date:
Status: Proposed / Approved / Deferred
Reference(s):
```

---

## 16. Release strategy and acceptance plan

### 16.1 What “first portfolio release” means

An installable Android APK running **offline** on documented compatible devices, demonstrating the full Phase 1 mandatory MVP. It should have a README, installation steps, sample simulated workflow, source repository and known limitations. Google Play publication is optional and **not** the default release gate.

### 16.2 Minimum release checklist

- [ ] All Phase 1 `MUST` requirements implemented and traced to test evidence (or approved change recorded).
- [ ] `AC-01` through `AC-12` pass on agreed test device(s).
- [ ] SQLite ledger/transfer/settlement invariants pass, including crash/rollback and double-tap cases.
- [ ] Minimum API and target API compatibility matrix tested and documented.
- [ ] Airplane-mode launch, complete workflow and restart pass.
- [ ] PIN/local access, reset and platform backup/restore policy tested.
- [ ] No critical or major release blockers; remaining issues documented.
- [ ] Simulation disclaimers are unambiguous on onboarding, balances and transfer/funding screens.
- [ ] Usability checks include large fonts, clear form labels and non-color-only status indicators.
- [ ] Test data is fictional; no real bank branding, credentials or secrets in repository.
- [ ] Clean Git checkout builds, installs and can follow README demo steps.
- [ ] Release version and limitations documented; screenshots show the real build rather than mockups passed off as finished product.

### 16.3 Portfolio demo script (target end state)

1. Launch the app and see that it uses **simulated money**.
2. Create a local profile and unlock it.
3. Create Spending and Savings accounts, starting at R0.00.
4. Add **R3,000.00** demo funds to Spending and view the ledger record.
5. Register **R2,200.00** unpaid bill due in the next 30 days and **R300.00** planned reserve.
6. Open dashboard and show **R3,000.00 posted balance** versus **R500.00 estimated safe-to-spend**.
7. Transfer **R200.00** from Spending to Savings; show both account histories and unchanged combined total.
8. Return to planning; explain that the estimate reflects the selected Spending account (now **R300.00**, assuming no other changes) and the reserve did not itself transfer money.
9. Settle the bill through one linked expense; show paid state, new balance and no double counting.
10. Close the app; reopen offline; demonstrate persistent, reconciled results.

**Accounting cross-check:** after step 7, Spending R2,800.00, Savings R200.00, bills R2,200.00, reserve R300.00 → safe-to-spend **R300.00**. After settling the R2,200.00 bill: Spending R600.00, remaining unpaid bill subtotal R0.00, reserve R300.00 → **R300.00** estimate.

### 16.4 Definition of complete project

“Complete” means the owner can hand the code and six documents to another developer who can explain the purpose, build and run the app, validate its financial rules, identify what is simulated, locate known limitations and make a future enhancement without reverse-engineering undocumented assumptions.

---

## 17. Phase 2 decision register and approval requirements

### 17.1 Approved inputs from Phase 1

- [x] Combined banking simulator **and** money management direction approved.
- [x] Java, Android Studio, XML, SQLite constraints approved.
- [x] Detailed RN-SRS-001 requirements were read and approved by the owner on **18 November 2026**.
- [x] Six-stage SDLC and separate referenceable Markdown files requested by the owner.

**Nuance:** the Phase 1 file was authored with a proposed/draft header. The owner's subsequent explicit chat approval is the approval record for proceeding to Phase 2. If maintaining formal document-control metadata, update a controlled copy of Phase 1 to reflect that approval; do not silently change its requirements.

### 17.2 Phase 2 choices to confirm

| ID | Decision | Current recommendation | Owner response |
|---|---|---|---|
| P2-D01 | Final working name for development | RandNest (subject to name check) | Pending |
| P2-D02 | Scope and deferred list | All `MUST` only for MVP; no `SHOULD` additions before release | Pending |
| P2-D03 | Solo delivery/capacity | Owner-led project; use **12 h/week only as a planning illustration**, not a deadline | Pending |
| P2-D04 | Supported Android baseline | Min API 26, target API 36; revalidate at release | Pending |
| P2-D05 | Unlock method | Provisional local PIN with protected verifier, throttling and defined reset; detailed design Phase 3 | Pending |
| P2-D06 | Data continuity | No app-managed backup/sync/export; explicitly configure/test Android backups/transfers | Pending |
| P2-D07 | Release destination | GitHub portfolio + APK/README; no mandatory Play Store submission | Pending |
| P2-D08 | Package and date/time implementation choices | Defer exact implementation details to Phase 3, keep BR-16 and time horizon unchanged | Pending |
| P2-D09 | Error correction approach | No silent edit of posted transactions; audited reversal deferred | Pending |
| P2-D10 | Release quality gate | All MUSTs, AC-01–AC-12, no major/critical financial defects | Pending |

### 17.3 Gate G0 — Phase 2 sign-off checklist

- [ ] Confirm this file's estimates are **planning ranges**, not required calendar deadlines.
- [ ] Approve the `MUST` MVP and explicit deferred features.
- [ ] Accept the feasibility conclusion: educational simulator, not real banking.
- [ ] Accept resource and device/emulator feasibility on the developer's actual computer.
- [ ] Approve or amend the API baseline, provisional unlock and backup choices.
- [ ] Confirm who owns milestones and release decisions.
- [ ] Understand the high financial-consistency and local-security risks.
- [ ] Accept testing gates, definition of done and release package requirements.
- [ ] Record any changes and formally authorize Phase 3 system design.

### 17.4 Formal approval record

| Field | Value |
|---|---|
| Project | RandNest |
| Document | RN-PLAN-002, version 1.0 |
| Prepared | 18 November 2026 |
| Prepared for | Project owner / Android developer |
| Decision | **Pending owner approval** |
| Approval date | Pending |
| Approved with conditions | None recorded yet |
| Change requests | None recorded yet |

---

## 18. Handoff contract for Phase 3 — System Design

Phase 3 must produce enough **implementable technical detail** to satisfy the approved MVP, including:

1. **Architecture diagram** showing Android views/controllers, domain/business logic, repository/data access and SQLite boundaries.
2. **Screen map / navigation flow** covering onboarding/lock, dashboard, accounts, funding, expense, transfer confirmation, history/detail, bills, reserve, breakdown, reset and errors.
3. **Wireframes** or detailed layouts with essential labels, states, accessibility notes and disclosure placement.
4. **ER diagram**, complete tables/columns/types, keys/foreign keys, indexes, constraints, migrations and reset policy.
5. **Money and ledger contracts**: `long` cents, account balance source of truth, transaction types, transfer representation, atomic transaction boundaries, idempotency/duplicate prevention and reconciliation checks.
6. **Bill contract**: status model, due-date inclusion, payment/expense linkage, update/cancel rules and all-or-nothing settlement.
7. **Safe-to-spend specification**: exact pseudocode, today+30 inclusive rule, overdue handling, reserve semantics, negative shortfall and examples.
8. **Security design**: approved unlock, lock/relock rules, PIN verifier storage and threat model, private storage, backups/transfer rules and loss warnings.
9. **Implementation interface contracts** for account, ledger, bill and dashboard operations with success/error outcomes, input validation and threading expectations (no DB freeze on UI thread).
10. **Traceability matrix** mapping each `MUST` `FR` and `NFR` to screen(s), domain logic, DB object and test ID. Carry `BR-01`–`BR-20`, `AC-01`–`AC-12`, and financial test cases `FT-01`–`FT-16` forward.
11. **Detailed testing design**, including how to inject SQLite failures, force retry scenarios and generate 10,000 demo entries.
12. **Pinned build configuration**: known-compatible Android Studio, Gradle/AGP, JDK, compile SDK, min/target API, Java source level and necessary Java-compatible dependencies.

**Phase 3 exit condition:** a different Android developer should be able to start implementation **without inventing financial rules, table relationships, unlock behavior or screen semantics**. If any required design point is unresolved, keep it in a visible decision list rather than burying it in code.

---

## 19. Official references and verification notes

External documents are supporting technical references; **the project-specific requirements remain in RN-SRS-001**. Verify changing policies and library versions at implementation/release time.

1. Android Developers — Create a project (Java supported): https://developer.android.com/studio/projects/create-project
2. Android Developers — Android Studio project/build files (Groovy and Kotlin DSL): https://developer.android.com/studio/projects
3. Android Developers — `SQLiteOpenHelper`: https://developer.android.com/reference/android/database/sqlite/SQLiteOpenHelper
4. Android Developers — `SQLiteDatabase` and transactions: https://developer.android.com/reference/android/database/sqlite/SQLiteDatabase
5. Android Developers — SQLite performance best practices: https://developer.android.com/topic/performance/sqlite-performance-best-practices
6. Android Developers — App-specific file storage: https://developer.android.com/training/data-storage/app-specific
7. Android Developers — Back up user data / `dataExtractionRules`: https://developer.android.com/identity/data/autobackup
8. Android Developers — Android 12 backup/transfer behavior changes: https://developer.android.com/about/versions/12/behavior-changes-12
9. Android Developers — Android security best practices: https://developer.android.com/privacy-and-security/security-best-practices
10. Android Developers — Android Keystore: https://developer.android.com/privacy-and-security/keystore
11. Android Developers — API levels: https://developer.android.com/guide/topics/manifest/uses-sdk-element
12. Android Developers — Google Play target API requirements (revalidate before release): https://developer.android.com/google/play/requirements/target-sdk
13. OWASP MASVS — Storage: https://mas.owasp.org/MASVS/05-MASVS-STORAGE/
14. OWASP MASVS — Authentication: https://mas.owasp.org/MASVS/07-MASVS-AUTH/

---

## 20. Version history

| Version | Date | Status | Change |
|---|---|---|---|
| **1.0** | **18 Nov 2026** | Proposed for review | Created Phase 2 project plan from owner-approved RN-SRS-001: feasibility, scope, technology baseline, 196–286 hour work packages, contingency, milestones, quality gates, risks and Phase 3 handoff. |

**Document governance:** Once approved, amendments require a version change, reason, owner decision, affected requirement IDs and schedule/test impact. Keep old versions in Git history.
