package com.example.bank_system.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.bank_system.common.Result;
import com.example.bank_system.common.ResultCode;
import com.example.bank_system.data.repository.AccountRepository;
import com.example.bank_system.data.sqlite.SqliteAccountRepository;
import com.example.bank_system.domain.model.AccountType;
import com.example.bank_system.domain.model.VirtualAccount;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.*;

/**
 * M03 account repository tests.
 * Run with: .\gradlew.bat connectedDebugAndroidTest
 */
@RunWith(AndroidJUnit4.class)
public class AccountRepositoryTest {

    private RandNestDbHelper    helper;
    private AccountRepository   repo;
    private static final long   NOW = 1_700_000_000_000L;

    @Before
    public void setUp() {
        Context ctx = ApplicationProvider.getApplicationContext();
        ctx.deleteDatabase(RandNestDbHelper.DB_NAME);
        helper = new RandNestDbHelper(ctx);

        // Insert a minimal profile so FK constraints pass
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL(
            "INSERT INTO profiles (profile_id, display_name, setup_completed, created_at_ms)" +
            " VALUES (1, 'Tester', 1, " + NOW + ")"
        );
        db.execSQL(
            "INSERT INTO app_settings (profile_id, selected_spending_id)" +
            " VALUES (1, NULL)"
        );

        repo = new SqliteAccountRepository(helper);
    }

    @After
    public void tearDown() {
        helper.close();
        ApplicationProvider.getApplicationContext()
                           .deleteDatabase(RandNestDbHelper.DB_NAME);
    }

    // -------------------------------------------------------------------------
    // AC-M03-01: New account starts with a zero posted balance
    // -------------------------------------------------------------------------
    @Test
    public void newAccount_hasZeroBalance() {
        Result<String> created = repo.createAccount("Spending", AccountType.SPENDING, NOW);
        assertTrue(created.isOk());

        Result<Long> balance = repo.readPostedBalanceCents(created.getValue());
        assertTrue(balance.isOk());
        assertEquals("Opening balance must be R0.00", 0L, (long) balance.getValue());
    }

    // -------------------------------------------------------------------------
    // AC-M03-02: Duplicate account names are allowed (names are not unique IDs)
    // -------------------------------------------------------------------------
    @Test
    public void duplicateNames_bothCreated() {
        Result<String> r1 = repo.createAccount("My Account", AccountType.SPENDING, NOW);
        Result<String> r2 = repo.createAccount("My Account", AccountType.SAVINGS,  NOW + 1);
        assertTrue(r1.isOk());
        assertTrue(r2.isOk());
        assertNotEquals("UUIDs must differ", r1.getValue(), r2.getValue());
    }

    // -------------------------------------------------------------------------
    // AC-M03-03: getAccounts returns all accounts ordered by creation time
    // -------------------------------------------------------------------------
    @Test
    public void getAccounts_returnsAll_orderedByCreatedAt() {
        repo.createAccount("Spending", AccountType.SPENDING, NOW);
        repo.createAccount("Savings",  AccountType.SAVINGS,  NOW + 1000);

        Result<List<VirtualAccount>> result = repo.getAccounts();
        assertTrue(result.isOk());
        assertEquals(2, result.getValue().size());
        assertEquals(AccountType.SPENDING, result.getValue().get(0).getAccountType());
        assertEquals(AccountType.SAVINGS,  result.getValue().get(1).getAccountType());
    }

    // -------------------------------------------------------------------------
    // AC-M03-04: getAccount on a nonexistent ID returns NOT_FOUND
    // -------------------------------------------------------------------------
    @Test
    public void getAccount_unknownId_returnsNotFound() {
        Result<VirtualAccount> result = repo.getAccount("nonexistent-uuid");
        assertFalse(result.isOk());
        assertEquals(ResultCode.NOT_FOUND, result.getCode());
    }

    // -------------------------------------------------------------------------
    // AC-M03-05: Balance of nonexistent account returns NOT_FOUND
    //            (not zero — distinguishes "no account" from "empty ledger")
    // -------------------------------------------------------------------------
    @Test
    public void balanceOfNonexistentAccount_returnsNotFound() {
        Result<Long> result = repo.readPostedBalanceCents("does-not-exist");
        assertFalse(result.isOk());
        assertEquals(ResultCode.NOT_FOUND, result.getCode());
    }

    // -------------------------------------------------------------------------
    // AC-M03-06: Selecting a Spending account persists and is retrieved
    // -------------------------------------------------------------------------
    @Test
    public void selectSpendingAccount_persistsAndReturns() {
        Result<String> created = repo.createAccount("Main Spending", AccountType.SPENDING, NOW);
        assertTrue(created.isOk());
        String id = created.getValue();

        Result<Void> select = repo.selectSpendingAccount(id);
        assertTrue(select.isOk());

        Result<String> retrieved = repo.getSelectedSpendingAccountId();
        assertTrue(retrieved.isOk());
        assertEquals(id, retrieved.getValue());
    }

    // -------------------------------------------------------------------------
    // AC-M03-07: AccountService rejects SAVINGS selection for planning
    // -------------------------------------------------------------------------
    @Test
    public void accountService_rejectsSavingsForPlanning() {
        com.example.bank_system.domain.service.AccountService svc =
            new com.example.bank_system.domain.service.AccountService(repo);

        Result<String> created = repo.createAccount("My Savings", AccountType.SAVINGS, NOW);
        assertTrue(created.isOk());

        Result<Void> select = svc.selectSpendingAccount(created.getValue());
        assertFalse(select.isOk());
        assertEquals(ResultCode.WRONG_ACCOUNT_TYPE, select.getCode());
    }

    // -------------------------------------------------------------------------
    // AC-M03-08: Account data survives a database close and reopen
    // -------------------------------------------------------------------------
    @Test
    public void accounts_persistAfterReopen() {
        Result<String> created = repo.createAccount("Persistent", AccountType.SPENDING, NOW);
        assertTrue(created.isOk());
        String id = created.getValue();
        helper.close();

        Context ctx = ApplicationProvider.getApplicationContext();
        RandNestDbHelper helper2 = new RandNestDbHelper(ctx);
        AccountRepository repo2  = new SqliteAccountRepository(helper2);

        Result<VirtualAccount> found = repo2.getAccount(id);
        assertTrue("Account must exist after reopen", found.isOk());
        assertEquals("Persistent", found.getValue().getAccountName());
        helper2.close();
    }

    // -------------------------------------------------------------------------
    // AC-M03-09: AccountService rejects blank account name
    // -------------------------------------------------------------------------
    @Test
    public void accountService_rejectsBlankName() {
        com.example.bank_system.domain.service.AccountService svc =
            new com.example.bank_system.domain.service.AccountService(repo);

        Result<String> result = svc.createAccount("   ", AccountType.SPENDING, NOW);
        assertFalse(result.isOk());
        assertEquals(ResultCode.VALIDATION_ERROR, result.getCode());
    }

    // -------------------------------------------------------------------------
    // AC-M03-10: AccountService rejects name longer than 50 characters
    // -------------------------------------------------------------------------
    @Test
    public void accountService_rejectsNameTooLong() {
        com.example.bank_system.domain.service.AccountService svc =
            new com.example.bank_system.domain.service.AccountService(repo);

        String longName = "A".repeat(51);
        Result<String> result = svc.createAccount(longName, AccountType.SPENDING, NOW);
        assertFalse(result.isOk());
        assertEquals(ResultCode.VALIDATION_ERROR, result.getCode());
    }
}
