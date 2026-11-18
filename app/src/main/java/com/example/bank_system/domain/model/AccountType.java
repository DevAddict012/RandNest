package com.example.bank_system.domain.model;

/**
 * The two virtual account types supported in the MVP.
 * account_type is immutable after creation — enforced by the DB CHECK constraint.
 */
public enum AccountType {
    SPENDING,
    SAVINGS;

    /** Returns the enum constant matching the DB-stored string, or throws. */
    public static AccountType fromString(String value) {
        for (AccountType t : values()) {
            if (t.name().equals(value)) return t;
        }
        throw new IllegalArgumentException("Unknown account type: " + value);
    }
}
