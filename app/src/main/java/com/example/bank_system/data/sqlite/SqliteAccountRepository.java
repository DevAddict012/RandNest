package com.example.bank_system.data.sqlite;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.example.bank_system.common.Result;
import com.example.bank_system.common.ResultCode;
import com.example.bank_system.data.RandNestDbHelper;
import com.example.bank_system.data.repository.AccountRepository;
import com.example.bank_system.domain.model.AccountType;
import com.example.bank_system.domain.model.LocalProfile;
import com.example.bank_system.domain.model.VirtualAccount;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * SQLite implementation of AccountRepository.
 * All public methods are safe to call from a background executor only.
 */
public final class SqliteAccountRepository implements AccountRepository {

    private static final String TAG = "SqliteAccountRepo";

    private final RandNestDbHelper helper;

    public SqliteAccountRepository(RandNestDbHelper helper) {
        this.helper = helper;
    }

    // -------------------------------------------------------------------------
    // createAccount
    // -------------------------------------------------------------------------

    @Override
    public Result<String> createAccount(String name, AccountType type, long nowMs) {
        String accountId = UUID.randomUUID().toString();

        ContentValues cv = new ContentValues();
        cv.put("account_id",    accountId);
        cv.put("profile_id",    LocalProfile.PROFILE_ID);
        cv.put("account_name",  name.trim());
        cv.put("account_type",  type.name());
        cv.put("created_at_ms", nowMs);

        try {
            SQLiteDatabase db = helper.getWritableDatabase();
            long rowId = db.insertOrThrow("accounts", null, cv);
            if (rowId == -1) {
                return Result.error(ResultCode.STORAGE_ERROR, "error_storage");
            }
            return Result.ok(accountId);
        } catch (SQLiteException e) {
            Log.e(TAG, "createAccount failed", e);
            return Result.error(ResultCode.STORAGE_ERROR, "error_storage");
        }
    }

    // -------------------------------------------------------------------------
    // getAccounts
    // -------------------------------------------------------------------------

    @Override
    public Result<List<VirtualAccount>> getAccounts() {
        String sql =
            "SELECT account_id, profile_id, account_name, account_type, created_at_ms" +
            " FROM accounts" +
            " WHERE profile_id = ?" +
            " ORDER BY created_at_ms ASC";

        try (SQLiteDatabase db = helper.getReadableDatabase();
             Cursor c = db.rawQuery(sql,
                     new String[]{ String.valueOf(LocalProfile.PROFILE_ID) })) {

            List<VirtualAccount> list = new ArrayList<>(c.getCount());
            while (c.moveToNext()) {
                list.add(cursorToAccount(c));
            }
            return Result.ok(list);
        } catch (SQLiteException e) {
            Log.e(TAG, "getAccounts failed", e);
            return Result.error(ResultCode.STORAGE_ERROR, "error_storage");
        }
    }

    // -------------------------------------------------------------------------
    // getAccount
    // -------------------------------------------------------------------------

    @Override
    public Result<VirtualAccount> getAccount(String accountId) {
        String sql =
            "SELECT account_id, profile_id, account_name, account_type, created_at_ms" +
            " FROM accounts" +
            " WHERE account_id = ? AND profile_id = ?";

        try (SQLiteDatabase db = helper.getReadableDatabase();
             Cursor c = db.rawQuery(sql,
                     new String[]{ accountId,
                                   String.valueOf(LocalProfile.PROFILE_ID) })) {

            if (!c.moveToFirst()) {
                return Result.error(ResultCode.NOT_FOUND, "error_account_not_found");
            }
            return Result.ok(cursorToAccount(c));
        } catch (SQLiteException e) {
            Log.e(TAG, "getAccount failed", e);
            return Result.error(ResultCode.STORAGE_ERROR, "error_storage");
        }
    }

    // -------------------------------------------------------------------------
    // readPostedBalanceCents
    // -------------------------------------------------------------------------

    @Override
    public Result<Long> readPostedBalanceCents(String accountId) {
        // First confirm the account exists and belongs to profile 1
        Result<VirtualAccount> accountResult = getAccount(accountId);
        if (!accountResult.isOk()) {
            return Result.error(accountResult.getCode(), accountResult.getMessageKey());
        }

        // Authoritative balance query — SUM of all signed ledger entries
        String sql =
            "SELECT COALESCE(SUM(delta_cents), 0) AS balance_cents" +
            " FROM ledger_entries" +
            " WHERE account_id = ?";

        try (SQLiteDatabase db = helper.getReadableDatabase();
             Cursor c = db.rawQuery(sql, new String[]{ accountId })) {

            if (!c.moveToFirst()) {
                // No rows at all — account exists but ledger is empty → R0.00
                return Result.ok(0L);
            }

            long balance = c.getLong(0);

            // Guard against aggregate overflow (should never happen within
            // our domain caps, but we surface it rather than silently wrong)
            if (balance < -100_000_000_000L || balance > 100_000_000_000L) {
                Log.e(TAG, "Balance overflow detected for account " + accountId);
                return Result.error(ResultCode.OVERFLOW, "error_overflow");
            }

            return Result.ok(balance);
        } catch (SQLiteException e) {
            Log.e(TAG, "readPostedBalanceCents failed", e);
            return Result.error(ResultCode.STORAGE_ERROR, "error_storage");
        }
    }

    // -------------------------------------------------------------------------
    // selectSpendingAccount
    // -------------------------------------------------------------------------

    @Override
    public Result<Void> selectSpendingAccount(String accountId) {
        // accountId may be null (clear selection)
        try {
            SQLiteDatabase db = helper.getWritableDatabase();
            ContentValues cv  = new ContentValues();
            if (accountId == null) {
                cv.putNull("selected_spending_id");
            } else {
                cv.put("selected_spending_id", accountId);
            }
            int rows = db.update("app_settings", cv,
                    "profile_id = ?",
                    new String[]{ String.valueOf(LocalProfile.PROFILE_ID) });
            if (rows == 0) {
                // app_settings row might not exist yet — insert it
                cv.put("profile_id", LocalProfile.PROFILE_ID);
                db.insertOrThrow("app_settings", null, cv);
            }
            return Result.ok();
        } catch (SQLiteException e) {
            Log.e(TAG, "selectSpendingAccount failed", e);
            return Result.error(ResultCode.STORAGE_ERROR, "error_storage");
        }
    }

    // -------------------------------------------------------------------------
    // getSelectedSpendingAccountId
    // -------------------------------------------------------------------------

    @Override
    public Result<String> getSelectedSpendingAccountId() {
        String sql =
            "SELECT selected_spending_id FROM app_settings WHERE profile_id = ?";

        try (SQLiteDatabase db = helper.getReadableDatabase();
             Cursor c = db.rawQuery(sql,
                     new String[]{ String.valueOf(LocalProfile.PROFILE_ID) })) {

            if (!c.moveToFirst()) {
                return Result.ok(null); // No settings row → no selection
            }
            // getIsNull check: selected_spending_id may be NULL
            String id = c.isNull(0) ? null : c.getString(0);
            return Result.ok(id);
        } catch (SQLiteException e) {
            Log.e(TAG, "getSelectedSpendingAccountId failed", e);
            return Result.error(ResultCode.STORAGE_ERROR, "error_storage");
        }
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private static VirtualAccount cursorToAccount(Cursor c) {
        return new VirtualAccount(
            c.getString(0),                         // account_id
            c.getLong(1),                            // profile_id
            c.getString(2),                          // account_name
            AccountType.fromString(c.getString(3)),  // account_type
            c.getLong(4)                             // created_at_ms
        );
    }
}
