package com.example.bank_system.domain.model;

/**
 * Immutable snapshot of the local user profile.
 * profile_id is always 1 — there is exactly one profile per installation.
 */
public final class LocalProfile {

    public static final long PROFILE_ID = 1L;

    private final long    profileId;
    private final String  displayName;
    private final boolean setupCompleted;
    private final long    createdAtMs;

    public LocalProfile(long profileId, String displayName,
                        boolean setupCompleted, long createdAtMs) {
        this.profileId      = profileId;
        this.displayName    = displayName;
        this.setupCompleted = setupCompleted;
        this.createdAtMs    = createdAtMs;
    }

    public long    getProfileId()      { return profileId; }
    public String  getDisplayName()    { return displayName; }
    public boolean isSetupCompleted()  { return setupCompleted; }
    public long    getCreatedAtMs()    { return createdAtMs; }
}
