package com.example.bank_system.data.repository;

import com.example.bank_system.common.Result;
import com.example.bank_system.domain.model.AccountType;
import com.example.bank_system.domain.model.VirtualAccount;

import java.util.List;

/**
 * Data contract for virtual account persistence.
 *
 * All implementations must:
 *  - Run on a background executor (never the main/UI thread).
 *  - Use parameterized queries; never concatenate user input into SQL.
 *  - Never return live Cursor objects outside the data layer.
 *  - Distinguish NOT_FOUND (account does not exist) from a zero balance
 *    (account exists but has no ledger entries yet).
 */
public interface AccountRepository {

    /**
     * Creates a new virtual account owned by profile 1.
     *
     * @param name    Trimmed display name (1–50 chars). Names need not be unique.
     * @param type    SPENDING or SAVINGS — immutable after creation.
     * @param nowMs   Epoch milliseconds for created_at_ms.
     * @return OK with the new UUID account ID, or a typed error.
     */
    Result<String> createAccount(String name, AccountType type, long nowMs);

    /**
     * Returns all accounts owned by profile 1, ordered by created_at_ms ASC.
     */
    Result<List<VirtualAccount>> getAccounts();

    /**
     * Returns a single account by ID, or NOT_FOUND if it does not exist
     * or does not belong to profile 1.
     */
    Result<VirtualAccount> getAccount(String accountId);

    /**
     * Returns the posted balance in cents for the given account.
     * This is always SUM(delta_cents) from ledger_entries — never a cached value.
     *
     * Returns NOT_FOUND if the account does not exist.
     * Returns OK(0) if the account exists but has no ledger entries yet.
     */
    Result<Long> readPostedBalanceCents(String accountId);

    /**
     * Sets the selected Spending account in app_settings.
     * The service layer must verify accountId refers to a SPENDING account
     * owned by profile 1 before calling this method.
     *
     * @param accountId UUID of the account to select, or null to clear selection.
     */
    Result<Void> selectSpendingAccount(String accountId);

    /**
     * Returns the currently selected Spending account ID from app_settings,
     * or null inside the Result if none is selected.
     */
    Result<String> getSelectedSpendingAccountId();
}
