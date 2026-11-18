package com.example.bank_system.ui.base;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bank_system.RandNestApp;
import com.example.bank_system.domain.service.SessionManager;
import com.example.bank_system.ui.auth.UnlockActivity;

/**
 * Base class for every Activity that requires an unlocked session.
 *
 * Rules (Phase 3 §2.3 and §9.3):
 *  - onStart checks the session; if locked, routes to UnlockActivity and
 *    finishes the protected Activity so it cannot be reached via Back.
 *  - Protected data must never appear in Recents previews or during lock
 *    transitions — handled by finishing before routing.
 *  - Subclasses call refreshFromDatabase() on onResume (after mutations)
 *    to ensure no stale balance/intent value is displayed.
 *  - Subclasses must NOT perform database reads/writes on the main thread;
 *    use RandNestApp.getExecutor() and post results back via mainHandler.
 */
public abstract class BaseProtectedActivity extends AppCompatActivity {

    protected final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onStart() {
        super.onStart();
        checkSession();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkSession();
    }

    // -------------------------------------------------------------------------
    // Session guard
    // -------------------------------------------------------------------------

    private void checkSession() {
        SessionManager sm = RandNestApp.from(this).getSessionManager();
        if (!sm.isUnlocked()) {
            routeToUnlock();
        }
    }

    private void routeToUnlock() {
        Intent intent = new Intent(this, UnlockActivity.class);
        // Clear the back stack so the locked user cannot press Back to reach
        // protected content.
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // -------------------------------------------------------------------------
    // Helpers for subclasses
    // -------------------------------------------------------------------------

    /** Convenience access to the shared single-thread executor. */
    protected java.util.concurrent.ExecutorService getExecutor() {
        return RandNestApp.from(this).getExecutor();
    }

    /**
     * Subclasses override this to reload their displayed data from SQLite
     * after a successful mutation or on onResume.
     * Default no-op.
     */
    protected void refreshFromDatabase() { }
}
