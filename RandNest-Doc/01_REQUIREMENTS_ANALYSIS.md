# RandNest — SDLC Phase 1: Requirements Gathering & Analysis

> **Project:** RandNest (working name)  
> **Tagline:** Your money. Your plan. Your control.  
> **Document ID:** RN-SRS-001  
> **Version:** 1.0 — Proposed requirements baseline  
> **Date:** 18 November 2026  
> **Status:** Draft for product-owner review; not yet approved  
> **SDLC phase:** 1 of 6 — Requirements Gathering & Analysis  
> **Next phase:** Project Planning & Feasibility  
> **Intended audience:** Project owner, Android developer, UI/UX designer, tester, portfolio reviewer

---

## 1. Document purpose and usage

This document specifies **what the RandNest Android application must do, who it serves, the problem it solves, and the conditions by which completion will be judged**. It is intended to be clear enough for another developer to understand the proposed product and to plan a working implementation without relying on undocumented conversations.

This is a **requirements document**, not a database schema, wireframe, or implementation tutorial. Exact class names, SQL table structures, navigation structure, libraries, and layouts are deliberately deferred to **Phase 3: System Design**. Where an implementation choice is already fixed by the project owner, it is recorded as a constraint.

**Reading guide:**

- **Confirmed direction:** An Android banking simulator **combined** with practical personal money management.
- **Owner-stated technical constraints:** Android Studio; **Java only** (no Kotlin); Android XML layouts; Android's built-in SQLite for persistence; portfolio scale; developed incrementally by the project owner.
- **Working decisions proposed in this draft:** The product name *RandNest*, single local user profile, multiple virtual accounts, South African rand (ZAR), offline-first, and the exact MVP boundaries. These are explicit assumptions to review before Phase 2.
- **Hard boundary:** RandNest is **not a real bank**, does **not** handle actual funds, and does **not** link to external banks or execute real transfers.

### 1.1 Document conventions

| Convention | Meaning |
|---|---|
| `MUST` | Required for the MVP to be accepted. |
| `SHOULD` | Valuable next-release requirement, not required for MVP acceptance. |
| `COULD` | Optional enhancement if time permits. |
| `OUT` | Explicitly excluded from current scope. |
| `FR-xx` | Functional requirement identifier. |
| `NFR-xx` | Non-functional requirement identifier. |
| `BR-xx` | Business rule identifier. |
| `UC-xx` | Use-case identifier. |
| `AC-xx` | Testable acceptance criterion identifier. |

---

## 2. Product vision and problem statement

### 2.1 The problem

A banking account balance alone does not tell a person how much money they can safely spend. Someone might see **R3,000** available, forget that **R2,200** in bills are due soon and **R300** is earmarked for savings, and make a spending decision based on the wrong number.

Traditional digital banking features such as balances and transaction lists show **what has happened**. RandNest will also help the user record **what is coming next** and calculate an easy-to-understand **safe-to-spend estimate**.

A second problem is learning how accounts, deposits, expenses and transfers work. A simulation allows users and portfolio reviewers to explore these flows without moving real funds or sharing actual banking credentials.

### 2.2 Proposed solution

**RandNest is an offline Android banking simulator with personal finance planning.** It enables one person to:

1. Set up a local profile.
2. Create and manage virtual accounts with simulated ZAR balances.
3. Add demo funds, record expenses and transfer virtual money between their accounts.
4. See an accurate ledger and transaction history.
5. Record upcoming bills and a planned savings reserve.
6. See how those commitments affect a transparent **safe-to-spend estimate**.

All amounts are **virtual or manually entered**. The app must consistently and prominently indicate that it is a **simulation**.

### 2.3 Product vision statement

> For people who want to understand and practise managing money, RandNest is an offline Android banking simulator that combines account activity with upcoming financial commitments so they can see not just their displayed balance, but also an estimate of what remains after planned expenses.

### 2.4 Primary value proposition

**“Know what your money needs to cover before you decide what is free to spend.”**

### 2.5 Product objectives

| Objective ID | Objective | Evidence of success |
|---|---|---|
| OBJ-01 | Simulate common personal banking actions. | A user can fund a virtual account, record an expense, and transfer funds between their own accounts. |
| OBJ-02 | Provide correct, auditable account balances. | Balances reconcile to posted virtual transactions after restart and across transfers. |
| OBJ-03 | Show future commitments clearly. | A user can create, view, update, and settle upcoming bills. |
| OBJ-04 | Estimate discretionary money. | The safe-to-spend figure matches the documented formula and explains its components. |
| OBJ-05 | Work reliably without an internet connection. | Every MVP operation succeeds in airplane mode. |
| OBJ-06 | Demonstrate sound software engineering. | Each feature is traceable from requirement to design and test; source is maintained in version control. |

---

## 3. Stakeholders and users

### 3.1 Stakeholders

| Stakeholder | Interest | Role in review |
|---|---|---|
| Project owner / developer | Build a realistic and credible Android portfolio project. | Prioritises scope; approves requirements and design. |
| Primary app user | Understand virtual balances and planned spending. | Validates terminology, workflows, and usability. |
| Portfolio reviewer / potential employer | Evaluate practical Android, Java, persistence, and problem-solving skills. | Evaluates finished functionality, quality, and documentation. |
| Tester | Confirm the product meets its specification. | Derives test cases from FR, NFR, BR, and AC identifiers. |

### 3.2 Primary persona — student / early-career budget planner

**Context:** Uses an Android phone and wants to understand spending and short-term financial commitments. May have irregular income or limited experience planning for bills.

**Needs:** An approachable overview of a balance, upcoming payments, and a simple estimate of money left to spend.

**Frustrations:** Forgetting commitments, confusing account balance with discretionary funds, and using scattered notes.

**Success:** Can open the app and quickly see account balance, upcoming bills, and the safe-to-spend figure.

### 3.3 Secondary persona — banking workflow learner

**Context:** Wants to understand account movements without using real funds.

**Needs:** Simulate deposits, expenses and internal transfers; inspect a complete history.

**Success:** Can observe how every posted action changes account balances, and that a transfer does not create new funds.

### 3.4 Usage environment

- Personal Android smartphone or emulator.
- Primarily a single-device, single-user experience.
- No internet connection required for core features.
- The phone may be closed, put in the background, restarted, or temporarily lose power.
- Currency format in the initial release: South African rand (`ZAR`, displayed as `R`).

---

## 4. Project constraints, assumptions, and dependencies

### 4.1 Fixed technology constraints

| ID | Constraint |
|---|---|
| CON-01 | Native Android application built in **Android Studio**. |
| CON-02 | Business logic and UI controller code written in **Java**, not Kotlin. |
| CON-03 | Screen layouts created using Android **XML views/layouts**; no Jetpack Compose. |
| CON-04 | Persistent relational data stored locally using Android's built-in **SQLite** APIs, with `SQLiteOpenHelper` / `SQLiteDatabase` as the planned foundation. |
| CON-05 | No backend server, remote database, Firebase dependency, or banking API in the MVP. |
| CON-06 | The MVP must be usable fully offline. |
| CON-07 | Use demo funds and synthetic account information only; never request real banking login credentials. |
| CON-08 | The owner intends to build the app personally, with guidance, rather than receive a generated complete codebase. |

### 4.2 Proposed product assumptions requiring owner approval

| ID | Proposed assumption | Status |
|---|---|---|
| ASS-01 | App name is **RandNest**. | Proposed — name can change. |
| ASS-02 | One local profile per installation, with multiple virtual accounts owned by that profile. | Proposed. |
| ASS-03 | At least two virtual account types: **Spending** and **Savings**. | Proposed. |
| ASS-04 | ZAR only; no currency conversion or exchange rates. | Proposed. |
| ASS-05 | Safe-to-spend initially uses the selected **spending account** and a fixed **next 30 calendar days** horizon. | Proposed. |
| ASS-06 | Planned bills are one-time records in the MVP, not automatic recurring payments. | Proposed. |
| ASS-07 | The user supplies a single optional planned savings reserve for the 30-day calculation; the reserve does not automatically transfer money. | Proposed. |
| ASS-08 | Bills recorded as paid are linked to an actual virtual expense entry, avoiding double counting. | Proposed. |
| ASS-09 | No real personal identification, email address, phone number, or bank account number is required. | Proposed. |
| ASS-10 | Minimum supported Android version, authentication UX, and theme are decided in Phase 2/3. | Open. |

### 4.3 Important limitations

- On-device SQLite is not a substitute for a bank's secure server ledger.
- A local access lock alone is not equivalent to production banking authentication.
- SQLite databases are **not automatically encrypted just because they are stored on Android**. Sensitive-data handling and the final protection strategy need explicit design and testing.
- Without a cloud service, uninstalling or resetting the application may result in permanent loss of local records. The application must communicate this.
- Estimates are educational, based on information entered by the user; the app cannot know about bills the user has not entered.

---

## 5. Scope and release boundaries

### 5.1 MVP — MUST build

- One-time local profile setup and basic local access protection.
- Creation and display of at least one virtual Spending account; support another virtual account for savings/transfers.
- Virtual account balances with currency formatting.
- A clearly labelled **Add demo funds** function.
- Manual expense posting with optional category and note.
- Transfers **between the user's own virtual accounts**.
- Complete transaction history, with newest entries first and transaction details.
- Upcoming one-time bill registration, editing (when unpaid), and payment/settlement.
- Optional planned savings reserve.
- Safe-to-spend estimate and clear breakdown of its formula.
- Offline local persistence, validation, and error handling.
- Clear “Simulated money — not a real bank” indication.

### 5.2 Next release — SHOULD build

- Savings goals with targets and progress.
- Monthly spending summaries by category.
- Transaction history filtering and searching.
- Repeating planned bills and local reminders.
- Optional user-adjustable safe-to-spend horizon.
- Export of non-sensitive transaction reports after reviewing privacy implications.

### 5.3 Future enhancements — COULD build

- Biometric unlock as a convenience feature, implemented using supported Android authentication APIs.
- Multiple budget categories, monthly limits, and alerts.
- Accessibility and localization improvements beyond the MVP baseline.
- Optional sample-data reset and guided first-run demo.

### 5.4 Explicitly out of scope — OUT

- Real deposits, withdrawals, bank-to-bank transfers, payment processing, debit cards, or card issuance.
- Integration with financial institutions or access to real bank account data.
- A public financial service, financial advice, lending, credit scoring, or investment recommendations.
- Multi-user cloud accounts, online synchronisation, or shared wallets.
- Actual beneficiary payments to people outside the app.
- Regulatory claims that the app is a licensed bank or an authorised financial services provider.
- Production-grade fraud detection, KYC/AML processing, and bank-level infrastructure.

---

## 6. High-level user journey

1. **Install and open:** User sees RandNest branding and a conspicuous simulation disclosure.
2. **Set up:** User creates a local profile and unlock method according to the selected authentication design.
3. **Create accounts:** User has a Spending account and can create a Savings account.
4. **Fund demo account:** User adds a simulated R3,000.00 to Spending. A traceable transaction is recorded.
5. **Plan commitments:** User records an upcoming bill of R2,200.00 and a planned savings reserve of R300.00.
6. **See financial picture:** App shows R3,000.00 balance and **R500.00 safe to spend**.
7. **Make a virtual expense:** User records R100.00 spent; the account balance becomes R2,900.00 and safe-to-spend becomes R400.00.
8. **Transfer internally:** User transfers R200.00 from Spending to Savings; Spending becomes R2,700.00, Savings increases by R200.00, and safe-to-spend becomes R200.00 if commitments are unchanged.
9. **Pay a planned bill:** When a bill is paid, RandNest creates or links one posted expense and marks the bill as paid; it is no longer counted as an unpaid upcoming obligation.
10. **Reopen:** After restarting the app, all balances, history and commitments remain consistent.

Amounts above are **sample virtual values**, not real financial transactions.

---

## 7. Detailed functional requirements

### 7.1 Profile and access

| ID | Priority | Requirement | Acceptance condition |
|---|---|---|---|
| FR-01 | MUST | First launch offers local profile setup and clearly explains the simulation. | A new install shows onboarding; returning to the app does not create another profile. |
| FR-02 | MUST | User may enter a display name; no legal identity or actual bank credentials are required. | Valid display name is saved and shown after reopening. |
| FR-03 | MUST | The app protects entry with an approved local authentication mechanism. | A user without the configured unlock method cannot access the account screens through ordinary UI navigation. |
| FR-04 | MUST | The app offers a deliberate local data reset with an explicit warning. | User confirmation clears locally held profile and finance records and returns to onboarding; accidental taps do not. |

> **Design dependency:** Choose the exact unlock mechanism in Phase 2/3. If an app PIN/password is chosen, credentials must never be stored in plaintext, and failed-attempt limiting must be designed. The access mechanism must not be represented as bank-grade security.

### 7.2 Virtual accounts

| ID | Priority | Requirement | Acceptance condition |
|---|---|---|---|
| FR-05 | MUST | User can create a virtual account with name and type (`Spending` or `Savings`). | A valid account appears in the list and has an initial balance of R0.00 until a recorded funding event. |
| FR-06 | MUST | Dashboard displays each account's name, type, and current posted balance. | Balances match underlying posted virtual transactions. |
| FR-07 | MUST | User can select a Spending account to use for the safe-to-spend calculation. | The calculation and breakdown consistently reference only the selected account. |
| FR-08 | MUST | System identifies accounts using unique internal IDs and optionally synthetic display identifiers. | Transfers and history resolve to the correct account even if account display names are the same. |
| FR-09 | SHOULD | User can rename an account without changing its ledger history. | Renaming preserves all transaction records and balances. |

### 7.3 Demo funds, expenses and transfers

| ID | Priority | Requirement | Acceptance condition |
|---|---|---|---|
| FR-10 | MUST | User can add simulated funds to an account. | A positive amount creates a posted `DEMO_FUNDING` transaction and increases that account's balance once. |
| FR-11 | MUST | User can record a manual expense against an account. | A valid expense creates a posted transaction, decreases the source balance once, and appears in history. |
| FR-12 | MUST | User can move simulated money from one owned virtual account to a different owned account. | Amount is deducted from source and added to destination as one indivisible operation. |
| FR-13 | MUST | Insufficient funds, zero/negative amounts, missing required fields, and same-account transfers are rejected. | Clear messages are displayed; failed requests do not change account balances or create partial transactions. |
| FR-14 | MUST | Every successful balance-changing operation has a persistent, inspectable record. | History contains account, transaction kind, amount, date/time, and optional user note/category. |
| FR-15 | MUST | User must confirm a simulated transfer before posting it. | Confirmation shows source, destination and amount; cancellation has no effect. |
| FR-16 | SHOULD | Allow correction of a mistaken transaction through an audited reversal, not silent balance rewriting. | Original record remains inspectable and a corresponding correcting record is created. |

### 7.4 Transaction history

| ID | Priority | Requirement | Acceptance condition |
|---|---|---|---|
| FR-17 | MUST | User can view transaction history for a chosen account, newest first. | Each posted event appears exactly once in that account's history, with an understandable debit/credit sign. |
| FR-18 | MUST | User can open a transaction detail view. | Detail shows transaction type, amount, affected account(s), timestamp, category/note when provided, and an internal reference. |
| FR-19 | SHOULD | User can filter by type/date/category and search notes. | Matching history is shown without modifying stored transactions. |

### 7.5 Upcoming bills and planning

| ID | Priority | Requirement | Acceptance condition |
|---|---|---|---|
| FR-20 | MUST | User can create an upcoming bill with label, amount, due date and spending account. | Unpaid bill is saved, displayed, and considered in that account's safe-to-spend calculation if in scope. |
| FR-21 | MUST | User can view upcoming and overdue unpaid bills separately from paid bills. | Status and due dates are visible and consistent with stored state. |
| FR-22 | MUST | User can update or cancel an unpaid bill. | Saved changes immediately affect planning totals; cancellation removes it from future estimates without changing account balance. |
| FR-23 | MUST | User can settle a bill by posting a linked simulated expense, or link an eligible existing expense, without double posting. | Bill becomes paid; posted account expense exists exactly once; unpaid commitment is no longer deducted by the estimate. |
| FR-24 | MUST | User can set an optional non-negative planned savings reserve for their selected Spending account for the 30-day planning window. | Saving/updating the reserve changes safe-to-spend, but does not change actual account balance or cause a transfer. |
| FR-25 | SHOULD | User can create named savings goals and track progress. | Goals and contributions can be traced without falsely duplicating account money. |

### 7.6 Dashboard and safe-to-spend

| ID | Priority | Requirement | Acceptance condition |
|---|---|---|---|
| FR-26 | MUST | Dashboard distinguishes **Posted balance** from **Estimated safe to spend**. | Both use clear labels and do not appear interchangeable. |
| FR-27 | MUST | System calculates safe-to-spend from posted balance, unpaid relevant bills, and planned savings reserve. | Output follows BR-11 through BR-15 and AC-08 exactly. |
| FR-28 | MUST | User can view the safe-to-spend breakdown and the 30-day coverage window. | Balance, bill subtotal, savings reserve and final estimate are separately visible. |
| FR-29 | MUST | When commitments exceed balance, show the shortfall rather than silently displaying R0.00 as if obligations were covered. | Negative estimate is displayed as a shortfall with understandable wording. |
| FR-30 | MUST | Dashboard updates after successful funding, expense, transfer, bill or reserve change. | No stale balance/estimate remains after returning to the dashboard. |
| FR-31 | SHOULD | User can view monthly spending totals and category insights. | Totals reconcile to posted expenses within selected dates. |

### 7.7 Simulation and transparency

| ID | Priority | Requirement | Acceptance condition |
|---|---|---|---|
| FR-32 | MUST | App visibly labels all balances and transfers as simulated. | Onboarding and money-moving screens plainly say that no real funds are involved. |
| FR-33 | MUST | App does not ask for real bank login credentials, payment card numbers or banking authorisation. | No flow, screen or stored field solicits that information. |
| FR-34 | MUST | App shows clear feedback for successful and failed operations. | Success feedback reflects committed data; failure feedback appears only after no ledger change has been made. |

---

## 8. Business rules and calculations

These rules govern **correct financial behaviour inside the simulation**. They take precedence over visual design decisions.

| ID | Rule |
|---|---|
| BR-01 | Every monetary amount is in **ZAR** for the MVP. Use integer **cents** in persisted calculations to avoid floating-point rounding errors. |
| BR-02 | Any posted balance-changing event must have an audit record; displayed balances must reconcile to posted activity. |
| BR-03 | User-entered posted amounts must be greater than zero. Negative amounts cannot be entered as a shortcut for refunds or adjustments. |
| BR-04 | A transaction must never reduce a source virtual account's posted balance below zero in the MVP. |
| BR-05 | A virtual transfer's source and destination must be different accounts belonging to the local profile. |
| BR-06 | A completed transfer decreases the source and increases the destination by the **same number of cents**. Total virtual money held across those accounts is unchanged. |
| BR-07 | Posting a transfer must be **atomic**: both sides succeed or neither side changes. |
| BR-08 | Retrying or double-tapping a submission must not accidentally post the same operation twice. |
| BR-09 | Draft, unpaid, cancelled and paid **planned bills** are commitments, not posted account transactions. Creating a bill never changes the posted balance. |
| BR-10 | Settling a bill must not subtract both the bill and its linked payment from safe-to-spend. After it posts, the expense lowers the balance and the unpaid commitment is removed from the bill subtotal. |
| BR-11 | The safe-to-spend calculation applies to **one selected Spending account**, not the total of unrelated accounts. |
| BR-12 | Relevant bills are unpaid, non-cancelled bills for that Spending account with due dates **on or before today + 30 calendar days**, including overdue unpaid bills. |
| BR-13 | Planned savings reserve is a **planning deduction only**. It never changes the posted balance or Savings account until the user separately posts a transfer. |
| BR-14 | `EstimatedSafeToSpend = SelectedAccountPostedBalance − RelevantUnpaidBillTotal − PlannedSavingsReserve`. Calculate in cents; convert to an `R` formatted value for display. |
| BR-15 | A negative result indicates a **projected shortfall**. Do not clamp the underlying result to zero. This estimate is guidance, not a hard transaction authorisation limit. |
| BR-16 | Date comparisons use device-local calendar dates for the MVP; timestamps for transaction history must distinguish ordering reliably. |
| BR-17 | Posted transactions should not be silently deleted or edited. Corrections require the authorised audit mechanism agreed for the release. |
| BR-18 | Account names and notes are display fields, **not** unique financial identifiers. |
| BR-19 | A new account starts at R0.00. Any example or initial funding must appear as a labelled simulated funding transaction. |
| BR-20 | A cancelled unpaid bill is excluded from projections. A paid bill remains available for historical review. |

### 8.1 Illustrative safe-to-spend calculation

| Component | Amount |
|---|---:|
| Posted Spending balance | R3,000.00 |
| Unpaid relevant bill(s) | − R2,200.00 |
| Planned savings reserve | − R300.00 |
| **Estimated safe to spend** | **R500.00** |

**Scenario A — another expense:** If the user posts an unrelated R100.00 expense, the account balance becomes R2,900.00 and the estimate becomes R400.00.

**Scenario B — transfer to savings:** If R200.00 moves from Spending to Savings, Spending decreases by R200.00 and Savings increases by R200.00. The total across the two accounts is unchanged; the safe-to-spend estimate for Spending decreases by R200.00.

**Scenario C — settling an upcoming bill:** Suppose the user pays the full R2,200.00 bill from Spending. The posted Spending balance decreases to R800.00, while the R2,200.00 unpaid commitment is removed. If the R300.00 savings reserve remains, the estimate is still R500.00. This illustrates why the bill must not be deducted twice.

**Scenario D — shortfall:** With an R800.00 balance, R1,100.00 of unpaid relevant bills and R100.00 of planned savings, safe-to-spend is **−R400.00**; the UI should indicate a projected R400.00 shortfall.

> **Financial interpretation:** “Safe to spend” is a simple educational estimate from the user's entered commitments. It is not a guarantee, a credit decision, or personal financial advice.

---

## 9. Primary use cases

### UC-01 — First-time setup

- **Actor:** Local user.
- **Precondition:** App has no completed profile.
- **Trigger:** User launches the app.
- **Main flow:** Show simulation disclosure → user enters display name → configures the agreed local unlock mechanism → creates or confirms the initial Spending account → reaches dashboard.
- **Alternative:** User abandons setup; app must not present incomplete profile data as completed setup.
- **Postcondition:** Valid local profile exists, and dashboard is accessible according to unlock rules.
- **Related:** FR-01 to FR-05; NFR-05; NFR-07.

### UC-02 — Add demo money

- **Actor:** Local user.
- **Precondition:** Profile and chosen virtual account exist; app is unlocked.
- **Trigger:** User selects **Add demo funds**.
- **Main flow:** Choose destination account → enter positive virtual amount → confirm the amount and simulation disclosure → post funding → show updated balance and history.
- **Alternatives:** Blank, zero, negative or invalid amount; double submission; storage failure. None may produce a partial/duplicate posting.
- **Postcondition:** Destination balance rises by exactly the posted amount.
- **Related:** FR-10, FR-13, FR-14, BR-01 to BR-03, BR-08.

### UC-03 — Transfer between own accounts

- **Actor:** Local user.
- **Precondition:** At least two accounts exist; source has adequate funds.
- **Trigger:** User selects **Transfer**.
- **Main flow:** Choose source → choose different destination → enter amount → review → confirm → atomic posting → display success with reference → refresh both balances and histories.
- **Alternatives:** Same account, insufficient funds, invalid amount, user cancels, duplicate tap, posting failure.
- **Postcondition:** Source decreases and destination increases equally; combined funds remain unchanged.
- **Related:** FR-12 to FR-15, BR-04 to BR-08.

### UC-04 — Record an expense

- **Actor:** Local user.
- **Precondition:** Chosen account exists with sufficient virtual funds.
- **Trigger:** User selects **Record expense**.
- **Main flow:** Choose account → enter positive amount → choose optional category/note → review → post → display updated balance, history and estimate.
- **Alternatives:** Insufficient funds, invalid amount, posting failure.
- **Postcondition:** One recorded expense decreases the account by exactly the entered amount.
- **Related:** FR-11, FR-13, FR-14, BR-02 to BR-04.

### UC-05 — Create and settle a bill

- **Actor:** Local user.
- **Precondition:** Spending account exists.
- **Trigger:** User selects **Upcoming bills**.
- **Main flow:** Enter title, amount, due date and account → save unpaid bill → verify it affects safe-to-spend when in horizon → select **Pay / mark settled** → confirm linked expense → post expense and mark bill paid as a consistent operation.
- **Alternatives:** User edits/cancels an unpaid bill; due date is past (show overdue); insufficient funds; user cancels payment; a matching existing posted expense is linked if supported by the chosen UI.
- **Postcondition:** Bill paid status and ledger agree; bill is not double-counted in estimate.
- **Related:** FR-20 to FR-23, BR-09, BR-10, BR-12, BR-20.

### UC-06 — Understand safe-to-spend

- **Actor:** Local user.
- **Precondition:** Selected Spending account exists.
- **Trigger:** User opens dashboard or estimate breakdown.
- **Main flow:** Retrieve selected posted balance → sum eligible unpaid bills → retrieve planned savings reserve → calculate estimate → show balance, bills, reserve, coverage window and result.
- **Alternatives:** No bills; no reserve; zero balance; negative estimate; overdue bill; updates after posting a payment.
- **Postcondition:** User can explain where the estimate came from.
- **Related:** FR-24, FR-26 to FR-30, BR-11 to BR-15.

### UC-07 — Inspect transaction history

- **Actor:** Local user.
- **Precondition:** Chosen virtual account exists.
- **Trigger:** User selects an account's activity/history.
- **Main flow:** Display ordered list → select transaction → show detail and reference.
- **Alternative:** No transactions; show informative empty state, not an error.
- **Postcondition:** User can see the balance-changing operations associated with the account.
- **Related:** FR-14, FR-17, FR-18, BR-02.

---

## 10. Conceptual data requirements

This section identifies **business entities and required information**, not final SQL tables. Phase 3 will convert these into a database model and decide constraints, indexing, transaction representation, and upgrade strategy.

| Entity | Required information | Key relationships |
|---|---|---|
| LocalProfile | Internal ID; display name; setup status; approved unlock metadata | Owns all virtual accounts. |
| VirtualAccount | Internal ID; owner ID; display name; type; lifecycle status | Owns posted movements; may be selected for planning. |
| PostedTransaction / ledger event | Internal reference; event type; amount in cents; timestamp; account association; optional note/category; link to transfer or bill when applicable | Each posted activity reconciles to balance. |
| Transfer | Unique operation reference; source account; destination account; amount in cents; timestamp; completion state | Represents both sides of one atomic virtual movement. |
| PlannedBill | Internal ID; title; amount in cents; due date; account; status; optional linked posted expense | Affects estimate until paid/cancelled. |
| SavingsReserve | Selected Spending account; non-negative reserve amount; defined planning period | Changes estimate, not posted balance. |
| Category (optional in MVP) | Internal ID; category label | Groups expense records. |

### 10.1 Required data relationships

- One local profile **owns many** virtual accounts.
- One virtual account participates in **many** posted transactions.
- One transfer references **two distinct** owned virtual accounts and its ledger effects must be consistent.
- One Spending account may have **many** planned bills.
- A paid bill can reference **one** qualifying posted expense; a posted expense must not be counted as payment of more than one bill without an explicit supported allocation rule.
- Display names are not primary identifiers.

### 10.2 Data integrity requirements

1. Primary keys must uniquely identify each business record.
2. References between accounts, bills and posted events must not become orphaned.
3. Monetary values must fit in a signed 64-bit integer number of cents and be checked for overflow.
4. Every source-account balance restriction must hold at the **moment of posting**, not only when the form first loads.
5. Failed writes must leave no partial transfer or bill-settlement changes.
6. Database version changes must be handled intentionally; deleting all user data during a normal app upgrade is not acceptable.
7. App reset is a separate, user-confirmed operation, not a database migration strategy.
8. Choose a single source of truth for balances in Phase 3. Any cached balance must remain reconcilable to the ledger.

### 10.3 Data lifecycle

- **Create:** User-initiated profile, virtual account, planned bill, or posted event.
- **Read:** Dashboard, transaction history, bill list and breakdown.
- **Update:** Approved profile/account display metadata and unpaid bill details; posted transactions are immutable unless corrected with an auditable mechanism.
- **Archive/cancel:** Unpaid bills can be cancelled; account deletion with existing ledger records requires a later explicitly designed archive policy.
- **Erase:** User-confirmed local reset erases all local application data in the defined scope.
- **Uninstall:** Warn that local data may be lost; backup/restore policy must be explicitly decided, rather than assumed.

---

## 11. Non-functional requirements

| ID | Category | Measurable requirement / evaluation |
|---|---|---|
| NFR-01 | Offline availability | All MVP functions must work with airplane mode enabled; no core screen requires a network request. |
| NFR-02 | Persistence | Posted transactions, accounts, bills and reserves survive app process termination and device/app restart without inconsistency. |
| NFR-03 | Financial accuracy | Monetary values use integer cents; posted balances and transfers reconcile exactly with no floating-point rounding loss. |
| NFR-04 | Atomicity | Interrupted or failed transfers and bill settlements leave records and balances in a fully consistent previous or completed state, never an in-between state. |
| NFR-05 | Access control | Account screens are not available through ordinary UI navigation when the app is locked; exact unlock and relock policy is finalised before coding. |
| NFR-06 | Privacy | Do not collect real banking credentials or transmit financial records to a server; minimise requested Android permissions and use app-private internal storage for local records. |
| NFR-07 | Credential safety | Never store unlock secrets in plaintext, hardcode keys, print secrets to logs, or claim bank-grade encryption without implementing and testing it. |
| NFR-08 | Error handling | Invalid input, unavailable storage, insufficient balance and user cancellation produce clear feedback and no corrupt/partial data. |
| NFR-09 | Usability | Core actions (fund, expense, transfer, bills, breakdown) are discoverable from a simple dashboard/navigation structure. |
| NFR-10 | Accessibility | Form fields have labels; controls have useful descriptions; error state is not communicated only by colour; common increased font sizes remain usable. |
| NFR-11 | Display consistency | All money displays use `R` and two decimal places; date wording makes upcoming/overdue status unambiguous. |
| NFR-12 | Responsiveness | On the project's test Android emulator/device with **up to 10,000 posted entries**, normal dashboard actions should respond within about **2 seconds**, and long database work must not freeze UI input. This is a project acceptance target, not a platform guarantee. |
| NFR-13 | Reliability | Rapid repeated taps and app-background/resume transitions do not create duplicate posted activity. |
| NFR-14 | Maintainability | Java UI, domain rules, and persistence responsibilities are separated sufficiently to test calculation and ledger rules without rewriting the screens. |
| NFR-15 | Testability | Each `MUST` requirement has a documented verification scenario; finance invariants have automated tests where practical. |
| NFR-16 | Scope clarity | Every money-related screen shows enough context to distinguish virtual/simulated transactions from real banking. |
| NFR-17 | Device coverage | Minimum SDK, target SDK and test-device matrix are documented in Phase 2, then verified in Phase 5. |
| NFR-18 | Data safety | App backup, restore and reset behaviour are clearly specified and tested; do not unintentionally include authentication secrets or private financial records in accessible exports. |

### 11.1 Security posture

This portfolio app processes **simulated** money, not real funds. It should still demonstrate responsible Android practices: minimal data collection, protected app-local storage, authenticated access to account screens, avoidance of plaintext credentials, safe handling of exceptions, and no misleading claims. A default SQLite database should not be described as encrypted. The exact credential and at-rest encryption strategy requires a deliberate design decision in Phase 3.

**Not a security certification:** Completing these requirements does not make RandNest suitable for actual banking operations.

---

## 12. Input validation and edge cases

| Situation | Required behaviour |
|---|---|
| Empty mandatory amount/title/account | Reject with a contextual error before posting. |
| Amount `0`, negative, non-numeric or with invalid decimal precision | Reject; no state change. |
| Positive amount too large for permitted maximum or integer-cent representation | Reject clearly; no overflow or wraparound. |
| Source and destination are the same account | Reject transfer. |
| Source has insufficient posted funds | Reject posted expense/transfer; leave every account unchanged. |
| User taps **Confirm** twice rapidly | Exactly one operation posts. |
| User presses Back or cancels review | No operation posts. |
| Database reports write failure | Show failure and roll back associated changes. |
| App closes during posting | On reopening, record is either fully committed or absent; never partially applied. |
| History is empty | Display an informative empty state. |
| Account has no planned bills | Bill subtotal is R0.00. |
| Bill is overdue but unpaid | Show overdue; include in projected obligations until settled or cancelled. |
| Bill due beyond the next 30 days | Display in bills list but exclude from current safe-to-spend estimate. |
| Bill paid using a linked expense | Remove bill from unpaid subtotal and reduce balance exactly once. |
| Commitments exceed account funds | Display a negative estimate / explicit shortfall. |
| Transfer to Savings account | Money moves between accounts; savings reserve is not automatically changed unless a later product rule explicitly supports this. |
| Device/app restarts | Previously committed state remains consistent. |
| Account has transactions and user wants to delete it | Do not silently remove history; defer archiving/deletion policy. |
| Uninstall or data reset | Warn that locally held records can be lost permanently. |

---

## 13. Acceptance scenarios and release exit conditions

Every scenario uses **virtual money**. Unless stated otherwise, all amounts use ZAR and begin from a clean simulation profile.

### AC-01 — Local onboarding

**Given** no profile exists, **when** the user completes the approved setup flow, **then** an account dashboard becomes available; reopening the app must not repeat first-time setup.

### AC-02 — Demo funding

**Given** Spending has R0.00, **when** the user posts one R1,000.00 demo funding operation, **then** the balance is R1,000.00 and one labelled demo-funding record appears in history.

### AC-03 — Expense accuracy

**Given** Spending has R1,000.00, **when** a R125.75 expense posts, **then** its balance becomes R874.25 and the expense appears exactly once in history.

### AC-04 — Atomic internal transfer

**Given** Spending has R1,000.00 and Savings has R200.00, **when** R350.00 is transferred, **then** Spending is R650.00, Savings is R550.00 and combined money remains R1,200.00.

### AC-05 — Insufficient funds

**Given** Spending has R100.00, **when** the user tries to transfer R150.00, **then** the transaction is rejected, balances remain unchanged and no completed transfer is recorded.

### AC-06 — No double posting

**Given** a valid pending transfer, **when** confirmation is tapped twice or retried because the UI is slow, **then** at most one completed transfer is posted.

### AC-07 — Bill creation does not spend money

**Given** Spending has R3,000.00, **when** an unpaid R2,200.00 bill due within 30 days is added, **then** posted balance remains R3,000.00 and relevant planned bills total R2,200.00.

### AC-08 — Safe-to-spend calculation

**Given** Spending has R3,000.00, unpaid relevant bills total R2,200.00 and planned savings reserve is R300.00, **when** the dashboard loads, **then** safe-to-spend is **R500.00**, with each component visible.

### AC-09 — Settled bill not double counted

**Given** the AC-08 state, **when** the R2,200.00 bill is settled through one linked posted expense, **then** Spending balance becomes R800.00, that bill leaves the unpaid bill total, and safe-to-spend remains R500.00 (assuming nothing else changes).

### AC-10 — Shortfall display

**Given** balance R800.00, unpaid relevant bills R1,100.00 and reserve R100.00, **when** the estimate is viewed, **then** the UI clearly indicates a **R400.00 shortfall**, not an unexplained zero.

### AC-11 — Offline persistence

**Given** accounts, a transfer, a bill and reserve have been saved offline, **when** the app is force-closed and reopened while still offline, **then** the same committed records and correct balances/estimate are present.

### AC-12 — Simulation disclosure

**Given** any first-run or transfer/funding flow, **when** the user reviews it, **then** the application clearly states that the funds and operations are simulated and no real bank is involved.

### 13.1 Minimum MVP release gate

Phase 5 testing should not consider the MVP accepted until:

- [ ] All `MUST` functional requirements are implemented or an approved change request explicitly narrows scope.
- [ ] Every acceptance scenario AC-01 through AC-12 passes on the agreed test device(s).
- [ ] Transfer, ledger and bill-settlement invariants pass automated and manual tests.
- [ ] All core functionality works offline and persists after app restart.
- [ ] There is no known critical defect causing missing, duplicated or inconsistent virtual money.
- [ ] Clear demo-only language is present on relevant screens.
- [ ] Local access, storage and data reset have been tested against the approved security design.
- [ ] README and basic usage screenshots explain what the app does and does not do.

---

## 14. Requirement-to-implementation traceability (initial)

Phase 3 should fill the **design component**, **screen**, **database object**, and **test ID** columns of the project traceability matrix. The following identifies initial ownership by domain without prematurely dictating class structure.

| Domain | Requirements | Key proof |
|---|---|---|
| Profile and access | FR-01–04, NFR-05–07 | AC-01, access and reset tests |
| Accounts | FR-05–09, BR-18–19 | Account creation and balance reconciliation |
| Virtual ledger | FR-10–16, BR-01–08, BR-17 | AC-02–06, failure-rollback tests |
| History | FR-17–19 | History ordering and detail tests |
| Planning and bills | FR-20–25, BR-09–10, BR-20 | AC-07, AC-09 |
| Safe-to-spend | FR-26–31, BR-11–16 | AC-08–10 |
| Transparency | FR-32–34, NFR-16 | AC-12 |
| Offline data quality | NFR-01–04, NFR-08, NFR-13, NFR-18 | AC-11 and restart tests |
| UI/quality | NFR-09–12, NFR-14–15, NFR-17 | Accessibility, performance, device checks |

---

## 15. Risks, ambiguities and mitigations

| Risk / ambiguity | Impact | Suggested mitigation / later decision |
|---|---|---|
| Scope grows into a full banking clone. | Portfolio project becomes too large to finish. | Keep one local profile, internal transfers and fixed 30-day estimate for MVP. |
| Balance changes without a ledger entry. | Financial data becomes inconsistent. | Design one controlled posting path and formal invariants in Phase 3. |
| Transfers only half-post. | Virtual money is created/lost. | Use database transactions and rollback tests. |
| A paid bill is also counted as unpaid. | Safe-to-spend is understated. | Link bill settlement to exactly one posted expense. |
| “Safe to spend” sounds like a guarantee. | Misleading user expectations. | Always say *estimate*; show assumptions, time window, and breakdown. |
| Local PIN/password approach is weak or unclear. | False perception of security. | Select appropriate Android authentication design and avoid plaintext storage. |
| Plain SQLite is mistakenly described as encrypted. | Misrepresentation / privacy risk. | Distinguish Android app-private storage from encryption; choose controls deliberately. |
| App uninstall or upgrade loses data. | User frustration and failed persistence. | Specify reset, backups and migrations; run upgrade tests. |
| Negative safe-to-spend mistaken for negative bank balance. | Confusing UI. | Use separate labels and shortfall wording. |
| Single-device constraints are misunderstood. | Expectations of syncing are unmet. | State offline-only/no-sync in onboarding and README. |
| Complex planned savings goal allocation causes double counting. | Incorrect estimate. | Keep the MVP reserve separate from real money movement; add full goals later. |

---

## 16. Open questions requiring product-owner approval

These choices are **not silently decided**; they should be reviewed before Phase 2. Proposed defaults allow planning to proceed if unchanged.

| ID | Question | Proposed default |
|---|---|---|
| DEC-01 | Final app name? | **RandNest** (working name). |
| DEC-02 | Audience emphasis? | Portfolio demo + student/early-career money planning. |
| DEC-03 | Number of users per installation? | One local profile, multiple own virtual accounts. |
| DEC-04 | Currency? | ZAR only. |
| DEC-05 | Account types? | Spending and Savings. |
| DEC-06 | Authentication approach? | Local access lock, design chosen in Phase 2/3; no real banking credentials. |
| DEC-07 | Financial planning horizon? | Fixed next 30 calendar days for MVP. |
| DEC-08 | Upcoming bills? | One-time bills in MVP, repeating bills later. |
| DEC-09 | Savings? | Simple planned savings reserve in MVP, detailed goals later. |
| DEC-10 | Test-device and minimum Android version? | Agree in Phase 2 before implementation. |
| DEC-11 | Backup/export policy? | No automatic cloud sync; decide if backup is disabled and how users are warned. |
| DEC-12 | Correction of wrongly posted expense? | Audited correction/reversal; define actual UX before MVP launch. |
| DEC-13 | Paid-bill settlement UX? | Post a linked expense as the default; connecting an existing expense is optional if designed safely. |

---

## 17. Phase 1 review checklist

Before formally approving this document, the product owner and developer should confirm:

- [ ] The described user problem is specific and realistic.
- [ ] The combined banking simulator + financial planning vision is correct.
- [ ] The name **RandNest** is accepted or replaced.
- [ ] The MVP includes only work the developer reasonably intends to build.
- [ ] `MUST`, `SHOULD`, `COULD`, and `OUT` boundaries are agreed.
- [ ] The proposed profile, account, currency, bills, and planning assumptions are accepted.
- [ ] The safe-to-spend formula and bill settlement behaviour are understood.
- [ ] Every mandatory feature has testable acceptance conditions.
- [ ] No real bank integrations or funds are mistakenly implied.
- [ ] Open choices are assigned to Phase 2 or Phase 3.
- [ ] Phase 2 may begin with a controlled, versioned requirements baseline.

### 17.1 Approval record

| Field | Value |
|---|---|
| Product owner | To be supplied |
| Reviewer / developer | To be supplied |
| Review date | Pending |
| Decision | **Pending approval** |
| Approved version | Pending |
| Notes / change requests | Pending |

---

## 18. Handoff to Phase 2 — Project Planning & Feasibility

After Phase 1 approval, Phase 2 must use this document to prepare:

1. A realistic MVP effort estimate and milestone sequence.
2. A product backlog and build order based on `MUST` requirements.
3. A feasibility review: Android Studio, Java, XML and built-in SQLite; device constraints; offline and local security considerations.
4. A risk register with owner, likelihood, impact and mitigation priority.
5. A clear **definition of done** and testing strategy.
6. An approved list of deferred features.
7. Decisions for minimum SDK, authentication approach, version control, and data/backup expectations.

**Do not start detailed class, XML layout or SQLite table design until Phase 3.** This keeps problem requirements separate from technical decisions.

---

## 19. References and further reading

External documentation used to check broad Android and mobile-app safety constraints (these are *not* substitutes for this product's own requirements):

1. Android Developers — `SQLiteOpenHelper`: https://developer.android.com/reference/android/database/sqlite/SQLiteOpenHelper
2. Android Developers — `SQLiteDatabase`: https://developer.android.com/reference/android/database/sqlite/SQLiteDatabase
3. Android Developers — App-specific file storage: https://developer.android.com/training/data-storage/app-specific
4. Android Developers — Security best practices: https://developer.android.com/privacy-and-security/security-best-practices
5. OWASP Mobile Application Security Verification Standard — Storage: https://mas.owasp.org/MASVS/05-MASVS-STORAGE/
6. OWASP Mobile Application Security Verification Standard — Authentication: https://mas.owasp.org/MASVS/07-MASVS-AUTH/

**Document governance:** Changes after approval must be recorded with a new version, affected requirement IDs, reason for change, and effects on design, schedule, and test scenarios.

---

## 20. Change log

| Version | Date | Author | Change |
|---|---|---|---|
| 1.0 | 18 Nov 2026 | Project requirements draft | Initial Phase 1 requirements baseline proposal for RandNest. |
