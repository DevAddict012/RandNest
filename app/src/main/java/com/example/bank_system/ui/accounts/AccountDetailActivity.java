package com.example.bank_system.ui.accounts;

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
import com.example.bank_system.ui.base.BaseProtectedActivity;

/**
 * UI-07 — Detail view for a single virtual account.
 *
 * Shows name, type, short account reference, live posted balance and
 * quick-action buttons. The money action buttons are stubs here — they
 * become live in M04 (Funding/Expense) and M05 (Transfer).
 *
 * Account ID is received via Intent extra EXTRA_ACCOUNT_ID.
 * Data is always re-queried from SQLite — never trusted from the Intent.
 */
public final class AccountDetailActivity extends BaseProtectedActivity {

    public static final String EXTRA_ACCOUNT_ID = "extra_account_id";

    private String      accountId;
    private TextView    tvName;
    private TextView    tvType;
    private TextView    tvRef;
    private TextView    tvBalance;
    private ProgressBar progressBar;
    private TextView    tvError;
    private Button      btnAddFunds;
    private Button      btnExpense;
    private Button      btnTransfer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_detail);

        accountId   = getIntent().getStringExtra(EXTRA_ACCOUNT_ID);

        tvName      = findViewById(R.id.tv_account_name);
        tvType      = findViewById(R.id.tv_account_type);
        tvRef       = findViewById(R.id.tv_account_ref);
        tvBalance   = findViewById(R.id.tv_balance);
        progressBar = findViewById(R.id.progress_bar);
        tvError     = findViewById(R.id.tv_error);
        btnAddFunds = findViewById(R.id.btn_add_funds);
        btnExpense  = findViewById(R.id.btn_expense);
        btnTransfer = findViewById(R.id.btn_transfer);

        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // Money action stubs — enabled in M04 / M05
        btnAddFunds.setOnClickListener(v -> showComingSoon("Add demo funds — available in M04"));
        btnExpense.setOnClickListener(v  -> showComingSoon("Record expense — available in M04"));
        btnTransfer.setOnClickListener(v -> showComingSoon("Transfer — available in M05"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshFromDatabase();
    }

    @Override
    protected void refreshFromDatabase() {
        if (accountId == null || accountId.isEmpty()) {
            finish();
            return;
        }
        showLoading(true);
        AccountService svc = RandNestApp.from(this).getAccountService();

        getExecutor().execute(() -> {
            Result<VirtualAccount> accountResult = svc.getAccount(accountId);
            Result<Long>           balanceResult = svc.readPostedBalanceCents(accountId);

            mainHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                showLoading(false);

                if (!accountResult.isOk()) {
                    tvError.setText(R.string.error_account_not_found);
                    tvError.setVisibility(View.VISIBLE);
                    return;
                }

                VirtualAccount account = accountResult.getValue();
                long balance = balanceResult.isOk() ? balanceResult.getValue() : 0L;

                tvName.setText(account.getAccountName());
                tvType.setText(account.getAccountType().name());
                // Show last 8 chars of UUID as a short reference
                String id = account.getAccountId();
                tvRef.setText("Ref: …" + id.substring(Math.max(0, id.length() - 8)));
                tvBalance.setText(MoneyFormatter.format(balance));
            });
        });
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showComingSoon(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
    }
}
