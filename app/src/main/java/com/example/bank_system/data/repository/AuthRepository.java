package com.example.bank_system.data.repository;

import com.example.bank_system.common.Result;
import com.example.bank_system.domain.model.LocalProfile;

/**
 * Data contract for profile and auth_state persistence.
 *
 * All implementations must:
 *  - Run on a background executor (never the main/UI thread).
 *  - Use parameterized queries (no string concatenation with user input).
 *  - Never return raw Cursor objects outside the data layer.
 *  - Never log the PIN, salt, or verifier together.
 */
public interface AuthRepository {

    /**
     * Returns true if a profile with setup_completed = 1 already exists.
     * Used by GateActivity to decide Setup vs Unlock routing.
     */
    Result<Boolean> isSetupCompleted();

    /**
     * Atomically creates the profile, auth_state, and app_settings rows in
     * one SQL transaction. If a profile already exists, returns CONFLICT.
     *
     * @param displayName  Trimmed display name (1–60 chars).
     * @param salt         Fresh salt bytes from PinKdf.generateSalt().
     * @param verifier     Derived verifier bytes from PinKdf.deriveVerifier().
     * @param iterations   KDF iteration count stored for future migration.
     * @param nowMs        Current epoch milliseconds (created_at_ms).
     */
    Result<Void> createProfile(String displayName, byte[] salt,
                               byte[] verifier, int iterations, long nowMs);

    /**
     * Loads the auth_state row for the single profile.
     * Returns NOT_FOUND if no profile exists.
     */
    Result<AuthState> loadAuthState();

    /**
     * Increments failed_attempts and optionally sets blocked_until_ms.
     *
     * @param blockedUntilMs  0 if no block; epoch-ms of when retries are allowed again.
     */
    Result<Void> recordFailedAttempt(long blockedUntilMs);

    /** Clears failed_attempts and blocked_until_ms after a successful unlock. */
    Result<Void> clearFailedAttempts();

    /**
     * Deletes all data in child-to-parent order inside one SQL transaction.
     * Used by the deliberate user reset flow only.
     */
    Result<Void> deleteAllData();

    // -------------------------------------------------------------------------
    // Value object returned by loadAuthState()
    // -------------------------------------------------------------------------

    /** Immutable snapshot of the auth_state row. */
    final class AuthState {
        public final String kdfAlgorithm;
        public final int    kdfIterations;
        public final byte[] pinSalt;
        public final byte[] pinVerifier;
        public final int    failedAttempts;
        public final long   blockedUntilMs;

        public AuthState(String kdfAlgorithm, int kdfIterations,
                         byte[] pinSalt, byte[] pinVerifier,
                         int failedAttempts, long blockedUntilMs) {
            this.kdfAlgorithm   = kdfAlgorithm;
            this.kdfIterations  = kdfIterations;
            this.pinSalt        = pinSalt;
            this.pinVerifier    = pinVerifier;
            this.failedAttempts = failedAttempts;
            this.blockedUntilMs = blockedUntilMs;
        }
    }
}
