package com.example.bank_system.data.sqlite;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.example.bank_system.common.Result;
import com.example.bank_system.common.ResultCode;
import com.example.bank_system.data.RandNestDbHelper;
import com.example.bank_system.data.repository.AuthRepository;
import com.example.bank_system.domain.util.PinKdf;

/**
 * SQLite implementation of AuthRepository.
 *
 * All public methods are safe to call from a background executor.
 * They must never be called from the main/UI thread.
 */
public final class SqliteAuthRepository implements AuthRepository {

    private static final String TAG = "SqliteAuthRepo";

    private final RandNestDbHelper helper;

    public SqliteAuthRepository(RandNestDbHelper helper) {
        this.helper = helper;
    }

    // -------------------------------------------------------------------------
    // isSetupCompleted
    // -------------------------------------------------------------------------

    @Override
    public Result<Boolean> isSetupCompleted() {
        try (SQLiteDatabase db = helper.getReadableDatabase();
             Cursor c = db.rawQuery(
                 "SELECT setup_completed FROM profiles WHERE profile_id = 1", null)) {
            if (!c.moveToFirst()) return Result.ok(false);
            return Result.ok(c.getInt(0) == 1);
        } catch (SQLiteException e) {
            Log.e(TAG, "isSetupCompleted failed", e);
            return Result.error(ResultCode.STORAGE_ERROR, "error_storage");
        }
    }

    // -------------------------------------------------------------------------
    // createProfile
    // -------------------------------------------------------------------------

    @Override
    public Result<Void> createProfile(String displayName, byte[] salt,
                                      byte[] verifier, int iterations, long nowMs) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            // Guard: refuse if a profile already exists
            try (Cursor c = db.rawQuery(
                    "SELECT profile_id FROM profiles WHERE profile_id = 1", null)) {
                if (c.moveToFirst()) {
                    return Result.error(ResultCode.CONFLICT, "error_profile_exists");
                }
            }

            // Insert profile
            db.execSQL(
                "INSERT INTO profiles (profile_id, display_name, setup_completed, created_at_ms)" +
                " VALUES (1, ?, 1, ?)",
                new Object[]{ displayName.trim(), nowMs }
            );

            // Insert auth_state — never store raw PIN
            db.execSQL(
                "INSERT INTO auth_state" +
                " (profile_id, kdf_algorithm, kdf_iterations, pin_salt, pin_verifier," +
                "  failed_attempts, blocked_until_ms)" +
                " VALUES (1, ?, ?, ?, ?, 0, 0)",
                new Object[]{ PinKdf.ALGORITHM, iterations, salt, verifier }
            );

            // Insert app_settings row (selected_spending_id starts NULL)
            db.execSQL(
                "INSERT INTO app_settings (profile_id, selected_spending_id)" +
                " VALUES (1, NULL)"
            );

            db.setTransactionSuccessful();
            return Result.ok();
        } catch (SQLiteException e) {
            Log.e(TAG, "createProfile failed", e);
            return Result.error(ResultCode.STORAGE_ERROR, "error_storage");
        } finally {
            db.endTransaction();
        }
    }

    // -------------------------------------------------------------------------
    // loadAuthState
    // -------------------------------------------------------------------------

    @Override
    public Result<AuthState> loadAuthState() {
        try (SQLiteDatabase db = helper.getReadableDatabase();
             Cursor c = db.rawQuery(
                 "SELECT kdf_algorithm, kdf_iterations, pin_salt, pin_verifier," +
                 "       failed_attempts, blocked_until_ms" +
                 " FROM auth_state WHERE profile_id = 1", null)) {

            if (!c.moveToFirst()) {
                return Result.error(ResultCode.NOT_FOUND, "error_no_profile");
            }

            AuthState state = new AuthState(
                c.getString(0),
                c.getInt(1),
                c.getBlob(2),
                c.getBlob(3),
                c.getInt(4),
                c.getLong(5)
            );
            return Result.ok(state);
        } catch (SQLiteException e) {
            Log.e(TAG, "loadAuthState failed", e);
            return Result.error(ResultCode.STORAGE_ERROR, "error_storage");
        }
    }

    // -------------------------------------------------------------------------
    // recordFailedAttempt
    // -------------------------------------------------------------------------

    @Override
    public Result<Void> recordFailedAttempt(long blockedUntilMs) {
        try {
            SQLiteDatabase db = helper.getWritableDatabase();
            db.execSQL(
                "UPDATE auth_state" +
                " SET failed_attempts = failed_attempts + 1," +
                "     blocked_until_ms = ?" +
                " WHERE profile_id = 1",
                new Object[]{ blockedUntilMs }
            );
            return Result.ok();
        } catch (SQLiteException e) {
            Log.e(TAG, "recordFailedAttempt failed", e);
            return Result.error(ResultCode.STORAGE_ERROR, "error_storage");
        }
    }

    // -------------------------------------------------------------------------
    // clearFailedAttempts
    // -------------------------------------------------------------------------

    @Override
    public Result<Void> clearFailedAttempts() {
        try {
            SQLiteDatabase db = helper.getWritableDatabase();
            db.execSQL(
                "UPDATE auth_state SET failed_attempts = 0, blocked_until_ms = 0" +
                " WHERE profile_id = 1"
            );
            return Result.ok();
        } catch (SQLiteException e) {
            Log.e(TAG, "clearFailedAttempts failed", e);
            return Result.error(ResultCode.STORAGE_ERROR, "error_storage");
        }
    }

    // -------------------------------------------------------------------------
    // deleteAllData — child-to-parent deletion order
    // -------------------------------------------------------------------------

    @Override
    public Result<Void> deleteAllData() {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL("DELETE FROM planned_bills");
            db.execSQL("DELETE FROM savings_reserves");
            db.execSQL("DELETE FROM ledger_entries");
            db.execSQL("DELETE FROM money_operations");
            db.execSQL("DELETE FROM operation_intents");
            db.execSQL("DELETE FROM app_settings");
            db.execSQL("DELETE FROM accounts");
            db.execSQL("DELETE FROM auth_state");
            db.execSQL("DELETE FROM profiles");

            db.setTransactionSuccessful();
            return Result.ok();
        } catch (SQLiteException e) {
            Log.e(TAG, "deleteAllData failed", e);
            return Result.error(ResultCode.STORAGE_ERROR, "error_reset_failed");
        } finally {
            db.endTransaction();
        }
    }
}
