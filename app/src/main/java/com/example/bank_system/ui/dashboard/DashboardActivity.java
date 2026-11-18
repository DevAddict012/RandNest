package com.example.bank_system.ui.dashboard;

import android.os.Bundle;
import android.widget.TextView;

import com.example.bank_system.R;
import com.example.bank_system.ui.base.BaseProtectedActivity;

/**
 * UI-04 — Dashboard (stub for M02).
 *
 * This is a placeholder so Gate/Setup/Unlock have a valid target to navigate to.
 * Full implementation is built in M09 (Safe-to-Spend + account integration).
 */
public final class DashboardActivity extends BaseProtectedActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
    }
}
