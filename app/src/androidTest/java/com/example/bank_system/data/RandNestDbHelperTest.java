package com.example.bank_system.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * M01 instrumentation tests — DB-01 through DB-10.
 *
 * Each test opens a fresh in-memory or temp database so they are independent.
 * Run with:  .\gradlew.bat connectedDebugAndroidTest
 */
@RunWith(AndroidJUnit4.class)
public class RandNestDbHelperTest {

    private RandNestDbHelper helper;
    private SQLiteDatabase   db;

    @Before
    public void setUp() {
        Context ctx = ApplicationProvider.getApplicationContext();
        // Use a unique name per test run to guarantee isolation, or delete first.
        ctx.deleteDatabase(RandNestDbHelper.DB_NAME);
        helper = new RandNestDbHelper(ctx);
        db     = helper.getWritableDatabase();
    }

    @After
    public void tearDown() {
        db.close();
        helper.close();
        // Remove the test database file so the next test starts clean.
        ApplicationProvider.getApplicationContext()
                           .deleteDatabase(RandNestDbHelper.DB_NAME);
    }

    // -------------------------------------------------------------------------
    // DB-01: Exactly nine tables and five indexes are created; user_version = 1
    // -------------------------------------------------------------------------
    @Test
    public void DB01_schemaHasCorrectTablesAndVersion() {
        // Check database version
        assertEquals("DB version must be 1", 1, db.getVersion());

        // Collect all user-created table names
        Set<String> tables = queryNames(
            "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' AND name NOT LIKE 'android_%'"
        );

        Set<String> expectedTables = new HashSet<>();
        expectedTables.add("profiles");
        expectedTables.add("auth_state");
        expectedTables.add("accounts");
        expectedTables.add("app_settings");
        expectedTables.add("operation_intents");
        expectedTables.add("money_operations");
        expectedTables.add("ledger_entries");
        expectedTables.add("planned_bills");
        expectedTables.add("savings_reserves");

        assertEquals("Exactly 9 user tables", expectedTables, tables);

        // Collect all user-created index names
        Set<String> indexes = queryNames(
            "SELECT name FROM sqlite_master WHERE type='index' AND name NOT LIKE 'sqlite_%'"
        );

        Set<String> expectedIndexes = new HashSet<>();
        expectedIndexes.add("idx_accounts_owner_type");
        expectedIndexes.add("idx_ledger_account_entry");
        expectedIndexes.add("idx_operations_date");
        expectedIndexes.add("idx_bills_account_status_due");
        expectedIndexes.add("idx_intents_state_created");

        // Indexes may include auto-created ones for PRIMARY KEY / UNIQUE constraints,
        // so verify all expected indexes are present (not strict equality).
        assertTrue("All 5 named indexes must exist", indexes.containsAll(expectedIndexes));
    }

    // -------------------------------------------------------------------------
    // DB-02: Foreign keys are enabled on this connection
    // -------------------------------------------------------------------------
    @Test
    public void DB02_foreignKeysAreEnabled() {
        Cursor c = db.rawQuery("PRAGMA foreign_keys", null);
        try {
            assertTrue(c.moveToFirst());
            assertEquals("PRAGMA foreign_keys must return 1", 1, c.getInt(0));
        } finally {
            c.close();
        }
    }

    // -------------------------------------------------------------------------
    // DB-03: Orphan ledger entry is rejected by FK constraint
    // -------------------------------------------------------------------------
    @Test
    public void DB03_orphanLedgerEntryFailsForeignKey() {
        try {
            db.execSQL(
                "INSERT INTO ledger_entries (operation_id, account_id, delta_cents)" +
                " VALUES ('nonexistent-op', 'nonexistent-acc', 100)"
            );
            fail("FK violation must throw an exception");
        } catch (android.database.SQLException expected) {
            // Pass — FK constraint prevented the orphan row
        }
    }

    // -------------------------------------------------------------------------
    // DB-04: Invalid account_type is rejected by CHECK constraint
    // -------------------------------------------------------------------------
    @Test
    public void DB04_invalidAccountTypeRejected() {
        // Insert a valid profile first
        db.execSQL(
            "INSERT INTO profiles (profile_id, display_name, setup_completed, created_at_ms)" +
            " VALUES (1, 'Test User', 0, 1700000000000)"
        );

        try {
            db.execSQL(
                "INSERT INTO accounts (account_id, profile_id, account_name, account_type, created_at_ms)" +
                " VALUES ('acc-1', 1, 'My Account', 'CURRENT', 1700000000000)"
            );
            fail("CHECK constraint must reject invalid account_type 'CURRENT'");
        } catch (android.database.SQLException expected) {
            // Pass
        }
    }

    // -------------------------------------------------------------------------
    // DB-05: Negative reserve_cents is rejected by CHECK constraint
    // -------------------------------------------------------------------------
    @Test
    public void DB05_negativeReserveRejected() {
        // Need a profile and a SPENDING account first
        db.execSQL(
            "INSERT INTO profiles (profile_id, display_name, setup_completed, created_at_ms)" +
            " VALUES (1, 'Test User', 0, 1700000000000)"
        );
        db.execSQL(
            "INSERT INTO accounts (account_id, profile_id, account_name, account_type, created_at_ms)" +
            " VALUES ('acc-spend', 1, 'Spending', 'SPENDING', 1700000000000)"
        );

        try {
            db.execSQL(
                "INSERT INTO savings_reserves (account_id, reserve_cents, updated_at_ms)" +
                " VALUES ('acc-spend', -100, 1700000000000)"
            );
            fail("CHECK constraint must reject negative reserve_cents");
        } catch (android.database.SQLException expected) {
            // Pass
        }
    }

    // -------------------------------------------------------------------------
    // DB-06: Duplicate (operation_id, account_id) in ledger_entries is rejected
    // -------------------------------------------------------------------------
    @Test
    public void DB06_duplicateLedgerLegRejected() {
        insertMinimalFundingOperation();

        // First insert succeeds
        db.execSQL(
            "INSERT INTO ledger_entries (operation_id, account_id, delta_cents)" +
            " VALUES ('op-fund-1', 'acc-spend', 100000)"
        );

        // Second insert with same (operation_id, account_id) must fail
        try {
            db.execSQL(
                "INSERT INTO ledger_entries (operation_id, account_id, delta_cents)" +
                " VALUES ('op-fund-1', 'acc-spend', 50000)"
            );
            fail("UNIQUE constraint must reject duplicate (operation_id, account_id)");
        } catch (android.database.SQLException expected) {
            // Pass
        }
    }

    // -------------------------------------------------------------------------
    // DB-07: PRAGMA foreign_key_check returns no violations
    // -------------------------------------------------------------------------
    @Test
    public void DB07_foreignKeyCheckReturnsNoViolations() {
        Cursor c = db.rawQuery("PRAGMA foreign_key_check", null);
        try {
            assertFalse("PRAGMA foreign_key_check must return no violation rows", c.moveToFirst());
        } finally {
            c.close();
        }
    }

    // -------------------------------------------------------------------------
    // DB-08: PRAGMA integrity_check reports "ok"
    // -------------------------------------------------------------------------
    @Test
    public void DB08_integrityCheckReturnsOk() {
        Cursor c = db.rawQuery("PRAGMA integrity_check", null);
        try {
            assertTrue(c.moveToFirst());
            assertEquals("PRAGMA integrity_check must return 'ok'", "ok", c.getString(0));
        } finally {
            c.close();
        }
    }

    // -------------------------------------------------------------------------
    // DB-09: Schema and data survive a close/reopen cycle
    // -------------------------------------------------------------------------
    @Test
    public void DB09_dataPersistsAfterCloseAndReopen() {
        // Insert a profile row
        db.execSQL(
            "INSERT INTO profiles (profile_id, display_name, setup_completed, created_at_ms)" +
            " VALUES (1, 'Persistent User', 0, 1700000000001)"
        );
        db.close();
        helper.close();

        // Reopen
        Context ctx = ApplicationProvider.getApplicationContext();
        RandNestDbHelper helper2 = new RandNestDbHelper(ctx);
        SQLiteDatabase db2 = helper2.getReadableDatabase();
        try {
            Cursor c = db2.rawQuery(
                "SELECT display_name FROM profiles WHERE profile_id = 1", null
            );
            try {
                assertTrue("Profile row must still exist after reopen", c.moveToFirst());
                assertEquals("Persistent User", c.getString(0));
            } finally {
                c.close();
            }
        } finally {
            db2.close();
            helper2.close();
        }
    }

    // -------------------------------------------------------------------------
    // DB-10: An unsupported upgrade throws without destroying data
    // -------------------------------------------------------------------------
    @Test
    public void DB10_unsupportedUpgradeThrowsSafely() {
        // Insert a profile so there is real data to protect
        db.execSQL(
            "INSERT INTO profiles (profile_id, display_name, setup_completed, created_at_ms)" +
            " VALUES (1, 'Should Survive', 0, 1700000000002)"
        );
        db.close();
        helper.close();

        Context ctx = ApplicationProvider.getApplicationContext();
        // Open with a deliberately higher version to trigger onUpgrade
        RandNestDbHelper badHelper = new RandNestDbHelper(ctx) {
            @Override
            public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
                // Delegate to real implementation, which should throw
                super.onUpgrade(database, oldVersion, newVersion);
            }
        };

        try {
            // Force a version bump by directly accessing the internal field isn't
            // possible, so we verify the guard by calling onUpgrade directly.
            badHelper.onUpgrade(badHelper.getWritableDatabase(), 1, 2);
            fail("onUpgrade must throw for an unapproved migration");
        } catch (android.database.sqlite.SQLiteException expected) {
            // Pass — no tables were dropped
        } finally {
            badHelper.close();
        }

        // Verify data still present
        RandNestDbHelper verifyHelper = new RandNestDbHelper(ctx);
        SQLiteDatabase verifyDb = verifyHelper.getReadableDatabase();
        try {
            Cursor c = verifyDb.rawQuery(
                "SELECT display_name FROM profiles WHERE profile_id = 1", null
            );
            try {
                assertTrue("Data must survive a failed upgrade attempt", c.moveToFirst());
                assertEquals("Should Survive", c.getString(0));
            } finally {
                c.close();
            }
        } finally {
            verifyDb.close();
            verifyHelper.close();
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /** Inserts the minimum rows needed to have a valid funding operation. */
    private void insertMinimalFundingOperation() {
        db.execSQL(
            "INSERT INTO profiles (profile_id, display_name, setup_completed, created_at_ms)" +
            " VALUES (1, 'Test User', 0, 1700000000000)"
        );
        db.execSQL(
            "INSERT INTO accounts (account_id, profile_id, account_name, account_type, created_at_ms)" +
            " VALUES ('acc-spend', 1, 'Spending', 'SPENDING', 1700000000000)"
        );
        db.execSQL(
            "INSERT INTO operation_intents" +
            " (intent_id, operation_type, payload_json, payload_sha256," +
            "  intent_status, created_at_ms, updated_at_ms)" +
            " VALUES ('op-fund-1', 'DEMO_FUNDING'," +
            "  '{\"schema\":1}', '" + "a".repeat(64) + "'," +
            "  'PREPARED', 1700000000000, 1700000000000)"
        );
        db.execSQL(
            "INSERT INTO money_operations (operation_id, operation_type, posted_at_ms)" +
            " VALUES ('op-fund-1', 'DEMO_FUNDING', 1700000000000)"
        );
    }

    /** Returns a Set of name-column values from a query. */
    private Set<String> queryNames(String sql) {
        Set<String> names = new HashSet<>();
        Cursor c = db.rawQuery(sql, null);
        try {
            while (c.moveToNext()) {
                names.add(c.getString(0));
            }
        } finally {
            c.close();
        }
        return names;
    }
}
