package com.example.bank_system.domain.service;

import com.example.bank_system.common.Result;
import com.example.bank_system.common.ResultCode;
import com.example.bank_system.data.repository.AccountRepository;
import com.example.bank_system.domain.model.AccountType;
import com.example.bank_system.domain.model.VirtualAccount;

import java.util.List;

/**
 * Business logic for virtual account management.
 *
 * Rules enforced here (not just in SQL):
 *  - Account name: 1–50 non-blank characters.
 *  - Only SPENDING accounts may be selected for the Safe-to-Spend calculation.
 *  - Account type is immutable; this service never changes it.
 *  - Balance reads always go through the ledger (no cached column).
 *
 * All public methods must be called on a background executor.
 */
public final class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    // -------------------------------------------------------------------------
    // createAccount
    // -------------------------------------------------------------------------

    /**
     * Validates inputs and creates a new virtual account.
     *
     * @param name  Display name entered by the user.
     * @param type  SPENDING or SAVINGS.
     * @param nowMs Current epoch milliseconds.
     * @return OK with the new account ID, or a typed validation/storage error.
     */
    public Result<String> createAccount(String name, AccountType type, long nowMs) {
        // Validate name
        if (name == null || name.trim().isEmpty()) {
            return Result.error(ResultCode.VALIDATION_ERROR, "error_account_name_required");
        }
        if (name.trim().length() > 50) {
            return Result.error(ResultCode.VALIDATION_ERROR, "error_account_name_too_long");
        }
        if (type == null) {
            return Result.error(ResultCode.VALIDATION_ERROR, "error_account_type_required");
        }
        return accountRepository.createAccount(name.trim(), type, nowMs);
    }

    // -------------------------------------------------------------------------
    // getAccounts
    // -------------------------------------------------------------------------

    public Result<List<VirtualAccount>> getAccounts() {
        return accountRepository.getAccounts();
    }

    // -------------------------------------------------------------------------
    // getAccount
    // -------------------------------------------------------------------------

    public Result<VirtualAccount> getAccount(String accountId) {
        if (accountId == null || accountId.isEmpty()) {
            return Result.error(ResultCode.VALIDATION_ERROR, "error_account_not_found");
        }
        return accountRepository.getAccount(accountId);
    }

    // -------------------------------------------------------------------------
    // readPostedBalanceCents
    // -------------------------------------------------------------------------

    public Result<Long> readPostedBalanceCents(String accountId) {
        if (accountId == null || accountId.isEmpty()) {
            return Result.error(ResultCode.NOT_FOUND, "error_account_not_found");
        }
        return accountRepository.readPostedBalanceCents(accountId);
    }

    // -------------------------------------------------------------------------
    // selectSpendingAccount
    // -------------------------------------------------------------------------

    /**
     * Selects an account as the active Spending account for planning.
     * Rejects SAVINGS accounts — only SPENDING may be selected.
     *
     * @param accountId UUID of the account to select.
     */
    public Result<Void> selectSpendingAccount(String accountId) {
        if (accountId == null || accountId.isEmpty()) {
            return Result.error(ResultCode.VALIDATION_ERROR, "error_account_not_found");
        }

        // Load account to verify type
        Result<VirtualAccount> accountResult = accountRepository.getAccount(accountId);
        if (!accountResult.isOk()) {
            return Result.error(accountResult.getCode(), accountResult.getMessageKey());
        }

        VirtualAccount account = accountResult.getValue();
        if (account.getAccountType() != AccountType.SPENDING) {
            return Result.error(ResultCode.WRONG_ACCOUNT_TYPE, "error_must_be_spending");
        }

        return accountRepository.selectSpendingAccount(accountId);
    }

    // -------------------------------------------------------------------------
    // getSelectedSpendingAccountId
    // -------------------------------------------------------------------------

    public Result<String> getSelectedSpendingAccountId() {
        return accountRepository.getSelectedSpendingAccountId();
    }

    // -------------------------------------------------------------------------
    // getSelectedSpendingAccount  (convenience — loads the full object)
    // -------------------------------------------------------------------------

    public Result<VirtualAccount> getSelectedSpendingAccount() {
        Result<String> idResult = accountRepository.getSelectedSpendingAccountId();
        if (!idResult.isOk()) {
            return Result.error(idResult.getCode(), idResult.getMessageKey());
        }
        String id = idResult.getValue();
        if (id == null) {
            return Result.ok(null); // No account selected yet
        }
        return accountRepository.getAccount(id);
    }
}
