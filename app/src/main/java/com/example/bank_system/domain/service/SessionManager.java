package com.example.bank_system.domain.service;

import android.os.SystemClock;

/**
 * RAM-only unlock session.
 *
 * Rules (Phase 3 §9.3):
 *  - Default LOCKED on every cold process start — never persisted.
 *  - Unlocked after correct PIN verification.
 *  - Auto-locks when the app has been in background for >= BACKGROUND_TIMEOUT_MS.
 *  - Manual Lock clears the session immediately.
 *  - Configuration changes (rotation) are NOT treated as backgrounding.
 *
 * This class is intentionally simple and thread-safe by synchronization.
 * It must never store the PIN, verifier, or any financial data.
 */
public final class SessionManager {

    /** 60 seconds background timeout, as approved in Phase 3 §2.3. */
    public static final long BACKGROUND_TIMEOUT_MS = 60_000L;

    private volatile boolean unlocked          = false;
    private volatile long    backgroundedAtMs   = -1L; // -1 = not backgrounded

    // -------------------------------------------------------------------------
    // Unlock / lock
    // -------------------------------------------------------------------------

    /** Call after a successful PIN verification. */
    public synchronized void unlock() {
        unlocked        = true;
        backgroundedAtMs = -1L;
    }

    /** Immediate lock — called by manual "Lock app" and security guards. */
    public synchronized void lock() {
        unlocked         = false;
        backgroundedAtMs = -1L;
    }

    // -------------------------------------------------------------------------
    // Background tracking
    // -------------------------------------------------------------------------

    /**
     * Call from Application.onTrimMemory or a lifecycle observer when the
     * app goes into the background (last visible Activity paused).
     */
    public synchronized void onAppBackgrounded() {
        if (unlocked) {
            backgroundedAtMs = SystemClock.elapsedRealtime();
        }
    }

    /**
     * Call when the app returns to the foreground (first visible Activity resumed).
     * If more than BACKGROUND_TIMEOUT_MS have passed, the session is locked.
     */
    public synchronized void onAppForegrounded() {
        if (backgroundedAtMs >= 0) {
            long elapsed = SystemClock.elapsedRealtime() - backgroundedAtMs;
            if (elapsed >= BACKGROUND_TIMEOUT_MS) {
                lock();
                return;
            }
        }
        backgroundedAtMs = -1L;
    }

    // -------------------------------------------------------------------------
    // Query
    // -------------------------------------------------------------------------

    /**
     * Returns true only if the session is currently unlocked.
     * Every protected Activity checks this on onStart/onResume.
     */
    public synchronized boolean isUnlocked() {
        return unlocked;
    }
}
