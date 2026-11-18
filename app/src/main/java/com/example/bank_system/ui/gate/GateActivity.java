package com.example.bank_system.ui.gate;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bank_system.R;
import com.example.bank_system.RandNestApp;
import com.example.bank_system.common.Result;
import com.example.bank_system.ui.auth.SetupActivity;
import com.example.bank_system.ui.auth.UnlockActivity;

/**
 * UI-01 — The only exported, launcher Activity.
 *
 * Checks setup state asynchronously, then routes to:
 *   - SetupActivity   if no completed profile exists, or
 *   - UnlockActivity  if a profile already exists (always starts locked).
 *
 * This Activity removes itself from the back stack once routing is done,
 * so it never appears behind Setup or Unlock.
 */
public final class GateActivity extends AppCompatActivity {

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gate);
        checkSetupState();
    }

    private void checkSetupState() {
        RandNestApp app = RandNestApp.from(this);

        app.getExecutor().execute(() -> {
            Result<Boolean> result = app.getAuthService().isSetupCompleted();

            mainHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;

                if (result.isOk() && Boolean.TRUE.equals(result.getValue())) {
                    // Profile exists → go to Unlock (always starts locked)
                    startActivity(new Intent(this, UnlockActivity.class));
                } else {
                    // No profile yet → go to Setup
                    startActivity(new Intent(this, SetupActivity.class));
                }
                // Remove GateActivity from the back stack
                finish();
            });
        });
    }
}
