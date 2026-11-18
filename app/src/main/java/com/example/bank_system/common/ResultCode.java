package com.example.bank_system.common;

/**
 * Typed outcome codes returned by all service and repository operations.
 * UI layers map these to user-visible string resources; SQL internals
 * must never be surfaced directly to the user.
 */
public enum ResultCode {
    OK,
    VALIDATION_ERROR,
    INSUFFICIENT_FUNDS,
    NOT_FOUND,
    WRONG_ACCOUNT_TYPE,
    LOCKED,
    ALREADY_COMPLETED,
    ALREADY_CANCELLED,
    CONFLICT,
    OVERFLOW,
    STORAGE_ERROR,
    CORRUPT_DATA
}
