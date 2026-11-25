package com.example.bank_system.ui.accounts;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.bank_system.R;
import com.example.bank_system.RandNestApp;
import com.example.bank_system.common.Result;
import com.example.bank_system.domain.model.AccountType;
import com.example.bank_system.ui.base.BaseProtectedActivity;
import com.google.android.material.textfield.TextInputEditText;

/**
 * UI-06 — Create a new virtual Spending or Savings account.
 *
 * Rules:
 *  - Name is 1–50 characters; names are NOT unique.
 *  - Type (Spending / Savings) is chosen via radio group and is immutable after creation.
 *  - Opening balance is always R0.00; any initial funds must be posted as a
 *    DEMO_FUNDING operation (enforced by BR-19).
 */
public final class CreateAccountActivity extends BaseProtectedActivity {

    private TextInputEditText etName;
    private RadioGroup        rgType;
    private RadioButton       rbSpending;
    private RadioButton       rbSavings;
    private Button            btnCreate;
    private ProgressBar       progressBar;
    private TextView          tvError;

    private boolean submitting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        etName      = findViewById(R.id.et_account_name);
        rgType      = findViewById(R.id.rg_account_type);
        rbSpending  = findViewById(R.id.rb_spending);
        rbSavings   = findViewById(R.id.rb_savings);
        btnCreate   = findViewById(R.id.btn_create_account);
        progressBar = findViewById(R.id.progress_bar);
        tvError     = findViewById(R.id.tv_error);

        // Default to Spending
        rbSpending.setChecked(true);

        btnCreate.setOnClickListener(v -> attemptCreate());

        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
    }

    private void attemptCreate() {
        if (submitting) return;
        tvError.setVisibility(View.GONE);

        String name = etName.getText() != null ? etName.getText().toString() : "";
        AccountType type = rbSavings.isChecked() ? AccountType.SAVINGS : AccountType.SPENDING;

        setLoading(true);
        long nowMs = System.currentTimeMillis();

        RandNestApp.from(this).getExecutor().execute(() -> {
            Result<String> result = RandNestApp.from(this)
                    .getAccountService()
                    .createAccount(name, type, nowMs);

            mainHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                setLoading(false);
                if (result.isOk()) {
                    // Return to AccountsActivity — it reloads on onResume
                    finish();
                } else {
                    showError(result.getMessageKey());
                }
            });
        });
    }

    private void setLoading(boolean loading) {
        submitting = loading;
        btnCreate.setEnabled(!loading);
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showError(String key) {
        String msg;
        if ("error_account_name_required".equals(key)) {
            msg = getString(R.string.error_account_name_required);
        } else if ("error_account_name_too_long".equals(key)) {
            msg = getString(R.string.error_account_name_too_long);
        } else {
            msg = getString(R.string.error_storage);
        }
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
    }
}
