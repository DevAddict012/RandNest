package com.example.bank_system.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * RandNest SQLiteOpenHelper.
 *
 * Responsibilities:
 *  - Open / create "randnest.db" at schema version 1.
 *  - Enable foreign-key enforcement via onConfigure().
 *  - Delegate table/index creation to SchemaV1.createAll().
 *  - Reject any unsupported upgrade safely (no silent DROP TABLE).
 *
 * Usage: instantiate once in RandNestApp and share the single instance.
 * Never hold a reference to the SQLiteDatabase on the main/UI thread.
 */
public final class RandNestDbHelper extends SQLiteOpenHelper {

    public static final String DB_NAME    = "randnest.db";
    public static final int    DB_VERSION = 1;

    public RandNestDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // -------------------------------------------------------------------------
    // onConfigure — called before onCreate / onUpgrade
    // -------------------------------------------------------------------------

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // Foreign-key enforcement must be enabled before any schema or data work.
        db.setForeignKeyConstraintsEnabled(true);
    }

    // -------------------------------------------------------------------------
    // onCreate — called once when the database file does not yet exist
    // -------------------------------------------------------------------------

    @Override
    public void onCreate(SQLiteDatabase db) {
        SchemaV1.createAll(db);
    }

    // -------------------------------------------------------------------------
    // onUpgrade — no approved migration exists yet; fail safely
    // -------------------------------------------------------------------------

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // A future approved migration will add explicit v1→v2, v2→v3 steps here.
        // Never drop all tables unconditionally; that would destroy user data.
        throw new SQLiteException(
            "No approved schema migration from version " + oldVersion +
            " to version " + newVersion + ". Upgrade blocked."
        );
    }

    // -------------------------------------------------------------------------
    // onDowngrade — always unsafe for this app; fail explicitly
    // -------------------------------------------------------------------------

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        throw new SQLiteException(
            "Database downgrade from version " + oldVersion +
            " to " + newVersion + " is not supported."
        );
    }
}
