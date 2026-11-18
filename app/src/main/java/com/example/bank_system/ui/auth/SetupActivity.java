package com.example.bank_system.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bank_system.R;
import com.example.bank_system.RandNestApp;
import com.example.bank_system.common.Result;
import com.example.bank_system.ui.dashboard.DashboardActivity;

/**
 * UI-02 — First-time profile and PIN setup.
 *
 * Displays:
 *  - Simulation disclosure (shown prominently before any input).
 *  - Display name field.
 *  - PIN entry and confirmation fields (numberPassword input type).
 *  - Data-loss warning.
 *  - "Create profile" button.
 *
 * Rules:
 *  - No email, ID, bank account number, or real credential field exists here.
 *  - PBKDF2 runs on the background executor — the UI shows a progress bar.
 *  - On success, routes to DashboardActivity (session already unlocked by AuthService).
 *  - Double-submit is safe: the DB profile_id = 1 CHECK prevents a second profile.
 */
public final class SetupActivity extends AppCompatActivity {

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private TextInputEditText etDisplayName;
    private TextInputEditText etPin;
    private TextInputEditText etPinConfirm;
    private Button      btnCreate;
    private ProgressBar progressBar;
    private TextView    tvError;

    private boolean submitting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        etDisplayName = findViewById(R.id.et_display_name);
        etPin         = findViewById(R.id.et_pin);
        etPinConfirm  = findViewById(R.id.et_pin_confirm);
        btnCreate     = findViewById(R.id.btn_create_profile);
        progressBar   = findViewById(R.id.progress_bar);
        tvError       = findViewById(R.id.tv_error);

        btnCreate.setOnClickListener(v -> attemptSetup());
    }

    private void attemptSetup() {
        if (submitting) return;

        tvError.setVisibility(View.GONE);

        String name        = etDisplayName.getText().toString();
        char[] pin         = getChars(etPin);
        char[] pinConfirm  = getChars(etPinConfirm);

        setLoading(true);

        RandNestApp app = RandNestApp.from(this);
        long nowMs      = System.currentTimeMillis();

        app.getExecutor().execute(() -> {
            Result<Void> result = app.getAuthService()
                                     .completeSetup(name, pin, pinConfirm, nowMs);

            mainHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                setLoading(false);

                if (result.isOk()) {
                    // Session is now unlocked — go straight to Dashboard
                    Intent intent = new Intent(this, DashboardActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
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

    private void showError(String messageKey) {
        String message = resolveMessage(messageKey);
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    private String resolveMessage(String key) {
        if (key == null) return getString(R.string.error_unknown);
        switch (key) {
            case "error_name_required":  return getString(R.string.error_name_required);
            case "error_name_too_long":  return getString(R.string.error_name_too_long);
            case "error_pin_invalid":    return getString(R.string.error_pin_invalid);
            case "error_pin_mismatch":   return getString(R.string.error_pin_mismatch);
            case "error_profile_exists": return getString(R.string.error_profile_exists);
            default:                     return getString(R.string.error_storage);
        }
    }

    /** Extracts char[] from an EditText without leaving a String copy on the heap. */
    private static char[] getChars(EditText et) {
        int len = et.length();
        char[] chars = new char[len];
        if (len > 0) {
            et.getText().getChars(0, len, chars, 0);
        }
        return chars;
    }
}
