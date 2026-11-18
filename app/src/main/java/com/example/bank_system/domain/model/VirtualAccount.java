package com.example.bank_system.domain.model;

/**
 * Immutable snapshot of one virtual account row.
 *
 * Rules:
 *  - accountId is a UUID string (never displayed as-is to users).
 *  - accountName is a display label — it is NOT unique. Two accounts may share a name.
 *  - accountType is fixed at creation; it cannot change after the row is inserted.
 *  - There is no balanceCents field here. Balances are always derived at read time
 *    from SUM(ledger_entries.delta_cents) — never cached in the accounts table.
 */
public final class VirtualAccount {

    private final String      accountId;
    private final long        profileId;
    private final String      accountName;
    private final AccountType accountType;
    private final long        createdAtMs;

    public VirtualAccount(String accountId, long profileId,
                          String accountName, AccountType accountType,
                          long createdAtMs) {
        this.accountId   = accountId;
        this.profileId   = profileId;
        this.accountName = accountName;
        this.accountType = accountType;
        this.createdAtMs = createdAtMs;
    }

    public String      getAccountId()   { return accountId; }
    public long        getProfileId()   { return profileId; }
    public String      getAccountName() { return accountName; }
    public AccountType getAccountType() { return accountType; }
    public long        getCreatedAtMs() { return createdAtMs; }

    public boolean isSpending() { return accountType == AccountType.SPENDING; }
    public boolean isSavings()  { return accountType == AccountType.SAVINGS;  }
}
