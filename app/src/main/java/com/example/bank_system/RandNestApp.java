package com.example.bank_system;

import android.app.Application;
import android.app.Activity;
import android.app.Application.ActivityLifecycleCallbacks;
import android.os.Bundle;

import com.example.bank_system.data.RandNestDbHelper;
import com.example.bank_system.data.repository.AccountRepository;
import com.example.bank_system.data.repository.AuthRepository;
import com.example.bank_system.data.sqlite.SqliteAccountRepository;
import com.example.bank_system.data.sqlite.SqliteAuthRepository;
import com.example.bank_system.domain.service.AccountService;
import com.example.bank_system.domain.service.AuthService;
import com.example.bank_system.domain.service.SessionManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Application entry point and composition root.
 *
 * Owns:
 *  - A single-threaded ExecutorService for all database and KDF operations.
 *    Single thread ensures serialized writes and deterministic ordering.
 *  - The shared RandNestDbHelper (one SQLite connection pool).
 *  - The RAM-only SessionManager.
 *  - Singleton service instances shared across Activities.
 *
 * Also tracks foreground/background transitions to enforce the 60-second
 * relock timeout via SessionManager.
 */
public final class RandNestApp extends Application {

    // -------------------------------------------------------------------------
    // Singletons
    // -------------------------------------------------------------------------

    private ExecutorService  executor;
    private RandNestDbHelper dbHelper;
    private SessionManager   sessionManager;
    private AuthService      authService;
    private AccountService   accountService;

    // Track how many activities are currently visible (started but not stopped)
    private int visibleActivityCount = 0;

    // -------------------------------------------------------------------------
    // onCreate
    // -------------------------------------------------------------------------

    @Override
    public void onCreate() {
        super.onCreate();

        executor       = Executors.newSingleThreadExecutor();
        dbHelper       = new RandNestDbHelper(this);
        sessionManager = new SessionManager();

        AuthRepository authRepo = new SqliteAuthRepository(dbHelper);
        authService             = new AuthService(authRepo, sessionManager);

        AccountRepository accountRepo = new SqliteAccountRepository(dbHelper);
        accountService              = new AccountService(accountRepo);

        // Register lifecycle callbacks to track background/foreground transitions
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override public void onActivityCreated(Activity a, Bundle b) { }
            @Override public void onActivitySaveInstanceState(Activity a, Bundle b) { }
            @Override public void onActivityDestroyed(Activity a) { }

            @Override
            public void onActivityStarted(Activity activity) {
                visibleActivityCount++;
                if (visibleActivityCount == 1) {
                    // App came to foreground
                    sessionManager.onAppForegrounded();
                }
            }

            @Override
            public void onActivityStopped(Activity activity) {
                visibleActivityCount--;
                if (visibleActivityCount == 0) {
                    // App went to background
                    sessionManager.onAppBackgrounded();
                }
            }

            @Override public void onActivityResumed(Activity a) { }
            @Override public void onActivityPaused(Activity a)  { }
        });
    }

    // -------------------------------------------------------------------------
    // Public accessors
    // -------------------------------------------------------------------------

    public static RandNestApp from(android.content.Context ctx) {
        return (RandNestApp) ctx.getApplicationContext();
    }

    public ExecutorService  getExecutor()        { return executor; }
    public RandNestDbHelper getDbHelper()         { return dbHelper; }
    public SessionManager   getSessionManager()   { return sessionManager; }
    public AuthService      getAuthService()      { return authService; }
    public AccountService   getAccountService()   { return accountService; }
}
