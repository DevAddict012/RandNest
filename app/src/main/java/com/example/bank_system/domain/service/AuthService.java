package com.example.bank_system.domain.service;

import com.example.bank_system.common.Result;
import com.example.bank_system.common.ResultCode;
import com.example.bank_system.data.repository.AuthRepository;
import com.example.bank_system.domain.util.PinKdf;

import java.util.Arrays;

/**
 * Coordinates profile setup, PIN unlock, lockout enforcement, and data reset.
 *
 * All public methods that touch the database must be called on a background
 * executor — never on the main/UI thread. The SessionManager is updated here
 * after a successful unlock, and locked on failure/reset.
 *
 * Security posture (from Phase 3 §9.2):
 *  - 5 consecutive wrong PINs → 30 s delay, escalating by 30 s per failure,
 *    capped at 5 minutes (300 s).
 *  - blocked_until_ms is wall-clock epoch-ms so an ordinary process restart
 *    does not reset the lockout.
 *  - PIN char[] arrays are zeroed by PinKdf after derivation.
 */
public final class AuthService {

    // Lockout policy
    private static final int  MAX_FREE_ATTEMPTS    = 5;
    private static final long BASE_DELAY_MS         = 30_000L;   // 30 s
    private static final long MAX_DELAY_MS          = 300_000L;  // 5 min

    private final AuthRepository authRepository;
    private final SessionManager sessionManager;

    public AuthService(AuthRepository authRepository, SessionManager sessionManager) {
        this.authRepository = authRepository;
        this.sessionManager  = sessionManager;
    }

    // -------------------------------------------------------------------------
    // isSetupCompleted — called by GateActivity routing
    // -------------------------------------------------------------------------

    public Result<Boolean> isSetupCompleted() {
        return authRepository.isSetupCompleted();
    }

    // -------------------------------------------------------------------------
    // completeSetup
    // -------------------------------------------------------------------------

    /**
     * Validates inputs, derives the PIN verifier, then atomically persists the
     * profile, auth_state and app_settings rows.
     *
     * @param displayName  User's display name.
     * @param pin          PIN entered by user; zeroed internally after derivation.
     * @param pinConfirm   Confirmation PIN; zeroed internally.
     * @param nowMs        Current epoch milliseconds.
     */
    public Result<Void> completeSetup(String displayName,
                                      char[] pin,
                                      char[] pinConfirm,
                                      long nowMs) {
        // --- input validation ---
        if (displayName == null || displayName.trim().isEmpty()) {
            return Result.error(ResultCode.VALIDATION_ERROR, "error_name_required");
        }
        if (displayName.trim().length() > 60) {
            return Result.error(ResultCode.VALIDATION_ERROR, "error_name_too_long");
        }
        if (!PinKdf.isValidPin(pin)) {
            return Result.error(ResultCode.VALIDATION_ERROR, "error_pin_invalid");
        }
        if (!Arrays.equals(pin, pinConfirm)) {
            return Result.error(ResultCode.VALIDATION_ERROR, "error_pin_mismatch");
        }

        // --- derive verifier (slow, do on background thread) ---
        byte[] salt;
        byte[] verifier;
        try {
            salt     = PinKdf.generateSalt();
            verifier = PinKdf.deriveVerifier(pin, salt);
        } finally {
            Arrays.fill(pin,        '\0');
            Arrays.fill(pinConfirm, '\0');
        }

        // --- persist ---
        Result<Void> result = authRepository.createProfile(
            displayName.trim(), salt, verifier, PinKdf.ITERATIONS, nowMs
        );

        // Zero verifier bytes from memory
        Arrays.fill(verifier, (byte) 0);
        Arrays.fill(salt,     (byte) 0);

        if (result.isOk()) {
            sessionManager.unlock();
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // unlock
    // -------------------------------------------------------------------------

    /**
     * Verifies the user's PIN against the stored verifier.
     * Enforces lockout on repeated failures.
     *
     * @param pin    PIN entered by user; zeroed internally.
     * @param nowMs  Current epoch milliseconds (used for lockout checks).
     */
    public Result<Void> unlock(char[] pin, long nowMs) {
        // Load auth state from DB
        Result<AuthRepository.AuthState> stateResult = authRepository.loadAuthState();
        if (!stateResult.isOk()) {
            Arrays.fill(pin, '\0');
            return Result.error(stateResult.getCode(), stateResult.getMessageKey());
        }

        AuthRepository.AuthState state = stateResult.getValue();

        // Check if currently blocked
        if (state.blockedUntilMs > nowMs) {
            Arrays.fill(pin, '\0');
            long remainingMs = state.blockedUntilMs - nowMs;
            // Store remaining seconds in messageKey for UI display
            return Result.error(ResultCode.LOCKED,
                "error_locked:" + (remainingMs / 1000));
        }

        // Verify PIN
        boolean correct;
        try {
            correct = PinKdf.verify(pin, state.pinSalt, state.pinVerifier);
        } finally {
            Arrays.fill(pin, '\0');
        }

        if (correct) {
            authRepository.clearFailedAttempts();
            sessionManager.unlock();
            return Result.ok();
        }

        // Wrong PIN — record failure and compute next lockout
        int newFailures = state.failedAttempts + 1;
        long blockedUntil = 0L;
        if (newFailures >= MAX_FREE_ATTEMPTS) {
            int extraFails = newFailures - MAX_FREE_ATTEMPTS;
            long delay = BASE_DELAY_MS + (long) extraFails * BASE_DELAY_MS;
            delay = Math.min(delay, MAX_DELAY_MS);
            blockedUntil = nowMs + delay;
        }
        authRepository.recordFailedAttempt(blockedUntil);

        return Result.error(ResultCode.VALIDATION_ERROR, "error_wrong_pin");
    }

    // -------------------------------------------------------------------------
    // lockNow
    // -------------------------------------------------------------------------

    /** Immediately locks the session. Called by manual "Lock app". */
    public void lockNow() {
        sessionManager.lock();
    }

    // -------------------------------------------------------------------------
    // hasValidSession
    // -------------------------------------------------------------------------

    public boolean hasValidSession() {
        return sessionManager.isUnlocked();
    }

    // -------------------------------------------------------------------------
    // resetAllDemoData
    // -------------------------------------------------------------------------

    /**
     * Clears all local data after the user types the required confirmation phrase.
     * The session is locked before deletion begins and not restored on failure.
     *
     * @param typedConfirmation  Must equal "ERASE" (case-insensitive) to proceed.
     */
    public Result<Void> resetAllDemoData(String typedConfirmation) {
        if (!"ERASE".equalsIgnoreCase(
                typedConfirmation == null ? "" : typedConfirmation.trim())) {
            return Result.error(ResultCode.VALIDATION_ERROR, "error_confirm_required");
        }

        // Lock session before wiping — never expose data during or after reset
        sessionManager.lock();

        return authRepository.deleteAllData();
    }
}
