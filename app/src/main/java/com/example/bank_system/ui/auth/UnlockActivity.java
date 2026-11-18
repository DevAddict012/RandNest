package com.example.bank_system.ui.auth;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bank_system.R;
import com.example.bank_system.RandNestApp;
import com.example.bank_system.common.Result;
import com.example.bank_system.common.ResultCode;
import com.example.bank_system.ui.dashboard.DashboardActivity;

/**
 * UI-03 — PIN unlock screen.
 *
 * Rules:
 *  - No protected balances or account data are shown here.
 *  - Lockout message shows remaining seconds when blocked.
 *  - "Erase demo data" is offered for lost-PIN recovery with a typed
 *    confirmation dialog — it must NOT require the PIN to erase.
 *  - On success, routes to DashboardActivity (clears back stack).
 *  - This Activity is not exported; only GateActivity and BaseProtectedActivity
 *    can navigate here.
 */
public final class UnlockActivity extends AppCompatActivity {

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private TextInputEditText etPin;
    private Button      btnUnlock;
    private ProgressBar progressBar;
    private TextView    tvError;
    private Button      btnErase;

    private boolean submitting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlock);

        etPin       = findViewById(R.id.et_pin);
        btnUnlock   = findViewById(R.id.btn_unlock);
        progressBar = findViewById(R.id.progress_bar);
        tvError     = findViewById(R.id.tv_error);
        btnErase    = findViewById(R.id.btn_erase_data);

        btnUnlock.setOnClickListener(v -> attemptUnlock());
        btnErase.setOnClickListener(v -> showEraseConfirmDialog());
    }

    // -------------------------------------------------------------------------
    // Unlock
    // -------------------------------------------------------------------------

    private void attemptUnlock() {
        if (submitting) return;

        tvError.setVisibility(View.GONE);
        char[] pin = getChars(etPin);
        setLoading(true);

        RandNestApp app = RandNestApp.from(this);
        long nowMs = System.currentTimeMillis();

        app.getExecutor().execute(() -> {
            Result<Void> result = app.getAuthService().unlock(pin, nowMs);

            mainHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                setLoading(false);

                if (result.isOk()) {
                    etPin.getText().clear();
                    Intent intent = new Intent(this, DashboardActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    etPin.getText().clear();
                    showError(result.getCode(), result.getMessageKey());
                }
            });
        });
    }

    private void showError(ResultCode code, String messageKey) {
        String message;
        if (code == ResultCode.LOCKED && messageKey != null &&
                messageKey.startsWith("error_locked:")) {
            // Parse remaining seconds for the lockout countdown
            try {
                long seconds = Long.parseLong(messageKey.split(":")[1]);
                message = getString(R.string.error_locked_countdown, seconds);
            } catch (NumberFormatException e) {
                message = getString(R.string.error_locked);
            }
        } else if ("error_wrong_pin".equals(messageKey)) {
            message = getString(R.string.error_wrong_pin);
        } else {
            message = getString(R.string.error_storage);
        }
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    // -------------------------------------------------------------------------
    // Erase demo data (lost-PIN recovery)
    // -------------------------------------------------------------------------

    private void showEraseConfirmDialog() {
        // Step 1: warn the user
        new AlertDialog.Builder(this)
            .setTitle(R.string.dialog_erase_title)
            .setMessage(R.string.dialog_erase_message)
            .setPositiveButton(R.string.dialog_erase_continue, (d, w) -> showTypedConfirmDialog())
            .setNegativeButton(android.R.string.cancel, null)
            .show();
    }

    private void showTypedConfirmDialog() {
        // Step 2: require typed "ERASE" confirmation
        final EditText input = new EditText(this);
        input.setHint(R.string.dialog_erase_hint);

        new AlertDialog.Builder(this)
            .setTitle(R.string.dialog_erase_type_title)
            .setView(input)
            .setPositiveButton(R.string.dialog_erase_confirm, (d, w) -> {
                String typed = input.getText().toString();
                performErase(typed);
            })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
    }

    private void performErase(String typedConfirmation) {
        setLoading(true);
        RandNestApp app = RandNestApp.from(this);

        app.getExecutor().execute(() -> {
            Result<Void> result = app.getAuthService().resetAllDemoData(typedConfirmation);

            mainHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                setLoading(false);

                if (result.isOk()) {
                    // Data erased → go to Setup
                    Intent intent = new Intent(this, SetupActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    tvError.setText(R.string.error_reset_failed);
                    tvError.setVisibility(View.VISIBLE);
                }
            });
        });
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void setLoading(boolean loading) {
        submitting = loading;
        btnUnlock.setEnabled(!loading);
        btnErase.setEnabled(!loading);
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private static char[] getChars(TextInputEditText et) {
        int len = et.length();
        char[] chars = new char[len];
        if (len > 0) {
            et.getText().getChars(0, len, chars, 0);
        }
        return chars;
    }
}
