package com.example.bank_system.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.bank_system.R;
import com.example.bank_system.RandNestApp;
import com.example.bank_system.common.Result;
import com.example.bank_system.domain.model.VirtualAccount;
import com.example.bank_system.domain.service.AccountService;
import com.example.bank_system.domain.util.MoneyFormatter;
import com.example.bank_system.ui.accounts.AccountsActivity;
import com.example.bank_system.ui.base.BaseProtectedActivity;

/**
 * UI-04 — Dashboard.
 *
 * M03 implementation: shows selected Spending account name + posted balance.
 * Safe-to-Spend estimate, bills, reserve and quick-action buttons are wired
 * in M08–M09. Navigation bar links to Accounts (live) and stubs for the rest.
 */
public final class DashboardActivity extends BaseProtectedActivity {

    private TextView    tvAccountName;
    private TextView    tvPostedBalance;
    private TextView    tvSafeToSpend;
    private TextView    tvNoAccount;
    private ProgressBar progressBar;
    private Button      btnAccounts;
    private Button      btnBills;
    private Button      btnHistory;
    private Button      btnSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        tvAccountName   = findViewById(R.id.tv_account_name);
        tvPostedBalance = findViewById(R.id.tv_posted_balance);
        tvSafeToSpend   = findViewById(R.id.tv_safe_to_spend);
        tvNoAccount     = findViewById(R.id.tv_no_account);
        progressBar     = findViewById(R.id.progress_bar);
        btnAccounts     = findViewById(R.id.btn_nav_accounts);
        btnBills        = findViewById(R.id.btn_nav_bills);
        btnHistory      = findViewById(R.id.btn_nav_history);
        btnSettings     = findViewById(R.id.btn_nav_settings);

        btnAccounts.setOnClickListener(v ->
                startActivity(new Intent(this, AccountsActivity.class)));

        // Stubs for future milestones
        btnBills.setOnClickListener(v ->
                tvSafeToSpend.setText(getString(R.string.coming_soon)));
        btnHistory.setOnClickListener(v ->
                tvSafeToSpend.setText(getString(R.string.coming_soon)));
        btnSettings.setOnClickListener(v ->
                tvSafeToSpend.setText(getString(R.string.coming_soon)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshFromDatabase();
    }

    @Override
    protected void refreshFromDatabase() {
        showLoading(true);
        AccountService svc = RandNestApp.from(this).getAccountService();

        getExecutor().execute(() -> {
            Result<VirtualAccount> accountResult = svc.getSelectedSpendingAccount();
            Long balance = null;
            if (accountResult.isOk() && accountResult.getValue() != null) {
                Result<Long> balResult = svc.readPostedBalanceCents(
                        accountResult.getValue().getAccountId());
                balance = balResult.isOk() ? balResult.getValue() : 0L;
            }
            final VirtualAccount account     = accountResult.isOk()
                    ? accountResult.getValue() : null;
            final Long           finalBalance = balance;

            mainHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                showLoading(false);

                if (account == null) {
                    // No Spending account selected yet
                    tvNoAccount.setVisibility(View.VISIBLE);
                    tvAccountName.setVisibility(View.GONE);
                    tvPostedBalance.setVisibility(View.GONE);
                    tvSafeToSpend.setVisibility(View.GONE);
                } else {
                    tvNoAccount.setVisibility(View.GONE);
                    tvAccountName.setVisibility(View.VISIBLE);
                    tvPostedBalance.setVisibility(View.VISIBLE);
                    tvSafeToSpend.setVisibility(View.VISIBLE);

                    tvAccountName.setText(account.getAccountName());
                    tvPostedBalance.setText(MoneyFormatter.format(
                            finalBalance != null ? finalBalance : 0L));
                    // Safe-to-spend full calculation arrives in M09
                    tvSafeToSpend.setText(getString(R.string.safe_to_spend_pending));
                }
            });
        });
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
