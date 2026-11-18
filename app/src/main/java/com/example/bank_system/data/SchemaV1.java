package com.example.bank_system.data;

import android.database.sqlite.SQLiteDatabase;

/**
 * RandNest database schema version 1.
 *
 * Contains the authoritative DDL for all nine tables and five indexes.
 * Execute via {@link #createAll(SQLiteDatabase)} inside
 * {@link RandNestDbHelper#onCreate(SQLiteDatabase)}.
 *
 * Rules:
 *  - No balance_cents column on accounts; balances are derived from ledger_entries.
 *  - Execute tables in FK-dependency order (parent before child).
 *  - FK constraints are enabled by RandNestDbHelper.onConfigure() before this runs.
 */
public final class SchemaV1 {

    private SchemaV1() { }

    // -------------------------------------------------------------------------
    // Table: profiles
    // Exactly one row; profile_id is locked to 1 by a CHECK constraint.
    // -------------------------------------------------------------------------
    private static final String CREATE_PROFILES =
        "CREATE TABLE profiles (" +
        "    profile_id    INTEGER PRIMARY KEY CHECK (profile_id = 1)," +
        "    display_name  TEXT    NOT NULL CHECK (length(trim(display_name)) BETWEEN 1 AND 60)," +
        "    setup_completed INTEGER NOT NULL DEFAULT 0 CHECK (setup_completed IN (0, 1))," +
        "    created_at_ms INTEGER NOT NULL CHECK (created_at_ms > 0)" +
        ")";

    // -------------------------------------------------------------------------
    // Table: auth_state
    // Stores the PBKDF2 PIN verifier and failed-attempt throttling metadata.
    // -------------------------------------------------------------------------
    private static final String CREATE_AUTH_STATE =
        "CREATE TABLE auth_state (" +
        "    profile_id       INTEGER PRIMARY KEY" +
        "                         REFERENCES profiles(profile_id) ON DELETE RESTRICT," +
        "    kdf_algorithm    TEXT    NOT NULL CHECK (kdf_algorithm = 'PBKDF2WithHmacSHA256')," +
        "    kdf_iterations   INTEGER NOT NULL CHECK (kdf_iterations >= 100000)," +
        "    pin_salt         BLOB    NOT NULL CHECK (length(pin_salt) >= 16)," +
        "    pin_verifier     BLOB    NOT NULL CHECK (length(pin_verifier) >= 32)," +
        "    failed_attempts  INTEGER NOT NULL DEFAULT 0 CHECK (failed_attempts >= 0)," +
        "    blocked_until_ms INTEGER NOT NULL DEFAULT 0 CHECK (blocked_until_ms >= 0)" +
        ")";

    // -------------------------------------------------------------------------
    // Table: accounts
    // Each virtual account owned by the single local profile.
    // account_type is immutable after creation.
    // -------------------------------------------------------------------------
    private static final String CREATE_ACCOUNTS =
        "CREATE TABLE accounts (" +
        "    account_id    TEXT    PRIMARY KEY NOT NULL," +
        "    profile_id    INTEGER NOT NULL" +
        "                      REFERENCES profiles(profile_id) ON DELETE RESTRICT," +
        "    account_name  TEXT    NOT NULL CHECK (length(trim(account_name)) BETWEEN 1 AND 50)," +
        "    account_type  TEXT    NOT NULL CHECK (account_type IN ('SPENDING', 'SAVINGS'))," +
        "    created_at_ms INTEGER NOT NULL CHECK (created_at_ms > 0)" +
        ")";

    // -------------------------------------------------------------------------
    // Table: app_settings
    // Nullable pointer to the currently selected Spending account.
    // The service layer enforces that selected_spending_id is a SPENDING account.
    // -------------------------------------------------------------------------
    private static final String CREATE_APP_SETTINGS =
        "CREATE TABLE app_settings (" +
        "    profile_id           INTEGER PRIMARY KEY" +
        "                             REFERENCES profiles(profile_id) ON DELETE RESTRICT," +
        "    selected_spending_id TEXT    REFERENCES accounts(account_id) ON DELETE RESTRICT" +
        ")";

    // -------------------------------------------------------------------------
    // Table: operation_intents
    // Durable, reviewable posting requests that survive process death.
    // Canonical JSON payload + SHA-256 digest prevent tampered retries.
    // -------------------------------------------------------------------------
    private static final String CREATE_OPERATION_INTENTS =
        "CREATE TABLE operation_intents (" +
        "    intent_id          TEXT    PRIMARY KEY NOT NULL," +
        "    operation_type     TEXT    NOT NULL CHECK (operation_type IN" +
        "                           ('DEMO_FUNDING', 'EXPENSE', 'TRANSFER', 'BILL_SETTLEMENT'))," +
        "    payload_json       TEXT    NOT NULL CHECK (length(payload_json) > 1)," +
        "    payload_sha256     TEXT    NOT NULL CHECK (length(payload_sha256) = 64)," +
        "    intent_status      TEXT    NOT NULL DEFAULT 'PREPARED'" +
        "                           CHECK (intent_status IN ('PREPARED', 'COMPLETED', 'CANCELLED'))," +
        "    created_at_ms      INTEGER NOT NULL CHECK (created_at_ms > 0)," +
        "    updated_at_ms      INTEGER NOT NULL CHECK (updated_at_ms > 0)," +
        "    acknowledged_at_ms INTEGER," +
        "    CHECK (acknowledged_at_ms IS NULL OR intent_status = 'COMPLETED')" +
        ")";

    // -------------------------------------------------------------------------
    // Table: money_operations
    // One committed financial event; operation_id equals the originating intent_id.
    // -------------------------------------------------------------------------
    private static final String CREATE_MONEY_OPERATIONS =
        "CREATE TABLE money_operations (" +
        "    operation_id   TEXT    PRIMARY KEY NOT NULL" +
        "                       REFERENCES operation_intents(intent_id) ON DELETE RESTRICT," +
        "    operation_type TEXT    NOT NULL CHECK (operation_type IN" +
        "                       ('DEMO_FUNDING', 'EXPENSE', 'TRANSFER', 'BILL_SETTLEMENT'))," +
        "    note           TEXT," +
        "    category       TEXT," +
        "    posted_at_ms   INTEGER NOT NULL CHECK (posted_at_ms > 0)" +
        ")";

    // -------------------------------------------------------------------------
    // Table: ledger_entries
    // Signed financial effects of a posted operation.
    // Each FUNDING/EXPENSE/BILL_SETTLEMENT has 1 entry; each TRANSFER has 2.
    // UNIQUE(operation_id, account_id) prevents duplicate legs.
    // -------------------------------------------------------------------------
    private static final String CREATE_LEDGER_ENTRIES =
        "CREATE TABLE ledger_entries (" +
        "    entry_id     INTEGER PRIMARY KEY AUTOINCREMENT," +
        "    operation_id TEXT    NOT NULL" +
        "                     REFERENCES money_operations(operation_id) ON DELETE RESTRICT," +
        "    account_id   TEXT    NOT NULL" +
        "                     REFERENCES accounts(account_id) ON DELETE RESTRICT," +
        "    delta_cents  INTEGER NOT NULL CHECK (delta_cents <> 0)," +
        "    UNIQUE (operation_id, account_id)" +
        ")";

    // -------------------------------------------------------------------------
    // Table: planned_bills
    // One-time future/overdue commitments; OVERDUE is never stored,
    // it is derived at runtime by comparing due_date to today's local date.
    // -------------------------------------------------------------------------
    private static final String CREATE_PLANNED_BILLS =
        "CREATE TABLE planned_bills (" +
        "    bill_id              TEXT    PRIMARY KEY NOT NULL," +
        "    spending_account_id  TEXT    NOT NULL" +
        "                             REFERENCES accounts(account_id) ON DELETE RESTRICT," +
        "    title                TEXT    NOT NULL CHECK (length(trim(title)) BETWEEN 1 AND 80)," +
        "    amount_cents         INTEGER NOT NULL CHECK (amount_cents > 0)," +
        "    due_date             TEXT    NOT NULL CHECK (length(due_date) = 10)," +
        "    bill_status          TEXT    NOT NULL DEFAULT 'UNPAID'" +
        "                             CHECK (bill_status IN ('UNPAID', 'PAID', 'CANCELLED'))," +
        "    paid_operation_id    TEXT    UNIQUE" +
        "                             REFERENCES money_operations(operation_id) ON DELETE RESTRICT," +
        "    paid_at_ms           INTEGER," +
        "    created_at_ms        INTEGER NOT NULL CHECK (created_at_ms > 0)," +
        "    updated_at_ms        INTEGER NOT NULL CHECK (updated_at_ms > 0)," +
        "    CHECK (" +
        "        (bill_status = 'PAID'" +
        "             AND paid_operation_id IS NOT NULL AND paid_at_ms IS NOT NULL)" +
        "        OR (bill_status IN ('UNPAID', 'CANCELLED')" +
        "             AND paid_operation_id IS NULL AND paid_at_ms IS NULL)" +
        "    )" +
        ")";

    // -------------------------------------------------------------------------
    // Table: savings_reserves
    // Per-Spending-account planning reserve.
    // reserve_cents >= 0 and never posts a ledger entry.
    // -------------------------------------------------------------------------
    private static final String CREATE_SAVINGS_RESERVES =
        "CREATE TABLE savings_reserves (" +
        "    account_id    TEXT    PRIMARY KEY NOT NULL" +
        "                      REFERENCES accounts(account_id) ON DELETE RESTRICT," +
        "    reserve_cents INTEGER NOT NULL DEFAULT 0 CHECK (reserve_cents >= 0)," +
        "    updated_at_ms INTEGER NOT NULL CHECK (updated_at_ms > 0)" +
        ")";

    // -------------------------------------------------------------------------
    // Indexes — improve balance queries, history paging, bill lookups.
    // -------------------------------------------------------------------------
    private static final String IDX_ACCOUNTS_OWNER_TYPE =
        "CREATE INDEX idx_accounts_owner_type" +
        "    ON accounts(profile_id, account_type, created_at_ms)";

    private static final String IDX_LEDGER_ACCOUNT_ENTRY =
        "CREATE INDEX idx_ledger_account_entry" +
        "    ON ledger_entries(account_id, entry_id DESC)";

    private static final String IDX_OPERATIONS_DATE =
        "CREATE INDEX idx_operations_date" +
        "    ON money_operations(posted_at_ms DESC, operation_id)";

    private static final String IDX_BILLS_ACCOUNT_STATUS_DUE =
        "CREATE INDEX idx_bills_account_status_due" +
        "    ON planned_bills(spending_account_id, bill_status, due_date)";

    private static final String IDX_INTENTS_STATE_CREATED =
        "CREATE INDEX idx_intents_state_created" +
        "    ON operation_intents(intent_status, created_at_ms DESC)";

    // -------------------------------------------------------------------------
    // Public entry point — called once from RandNestDbHelper.onCreate()
    // -------------------------------------------------------------------------

    /**
     * Creates all schema v1 tables and indexes in FK-safe order.
     * Must be called inside {@link SQLiteDatabase#beginTransaction()} or
     * directly from {@link android.database.sqlite.SQLiteOpenHelper#onCreate}.
     */
    public static void createAll(SQLiteDatabase db) {
        // Tables: parents before children
        db.execSQL(CREATE_PROFILES);
        db.execSQL(CREATE_AUTH_STATE);
        db.execSQL(CREATE_ACCOUNTS);
        db.execSQL(CREATE_APP_SETTINGS);
        db.execSQL(CREATE_OPERATION_INTENTS);
        db.execSQL(CREATE_MONEY_OPERATIONS);
        db.execSQL(CREATE_LEDGER_ENTRIES);
        db.execSQL(CREATE_PLANNED_BILLS);
        db.execSQL(CREATE_SAVINGS_RESERVES);

        // Indexes
        db.execSQL(IDX_ACCOUNTS_OWNER_TYPE);
        db.execSQL(IDX_LEDGER_ACCOUNT_ENTRY);
        db.execSQL(IDX_OPERATIONS_DATE);
        db.execSQL(IDX_BILLS_ACCOUNT_STATUS_DUE);
        db.execSQL(IDX_INTENTS_STATE_CREATED);
    }
}
