package com.example.bank_system.ui.accounts;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.bank_system.R;
import com.example.bank_system.RandNestApp;
import com.example.bank_system.common.Result;
import com.example.bank_system.domain.model.VirtualAccount;
import com.example.bank_system.domain.service.AccountService;
import com.example.bank_system.domain.util.MoneyFormatter;
import com.example.bank_system.ui.base.BaseProtectedActivity;

import java.util.List;

/**
 * UI-05 — All virtual accounts with their live posted balances.
 *
 * Shows a card per account with name, type badge, balance and a
 * "Use for planning" button that selects it as the active Spending account.
 * Reloads from SQLite on every onResume so balance changes are always current.
 */
public final class AccountsActivity extends BaseProtectedActivity {

    private LinearLayout  accountsContainer;
    private ProgressBar   progressBar;
    private TextView      tvEmpty;
    private View          fabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounts);

        accountsContainer = findViewById(R.id.accounts_container);
        progressBar       = findViewById(R.id.progress_bar);
        tvEmpty           = findViewById(R.id.tv_empty);
        fabAdd            = findViewById(R.id.fab_add_account);

        fabAdd.setOnClickListener(v ->
            startActivity(new Intent(this, CreateAccountActivity.class)));

        // Back arrow
        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
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
            Result<List<VirtualAccount>> accountsResult = svc.getAccounts();
            Result<String>               selectedResult = svc.getSelectedSpendingAccountId();

            List<VirtualAccount> accounts  = accountsResult.isOk()
                    ? accountsResult.getValue() : null;
            String               selectedId = selectedResult.isOk()
                    ? selectedResult.getValue() : null;

            // Load balances for each account
            long[] balances = new long[accounts == null ? 0 : accounts.size()];
            if (accounts != null) {
                for (int i = 0; i < accounts.size(); i++) {
                    Result<Long> b = svc.readPostedBalanceCents(
                            accounts.get(i).getAccountId());
                    balances[i] = b.isOk() ? b.getValue() : 0L;
                }
            }

            final List<VirtualAccount> finalAccounts  = accounts;
            final String               finalSelectedId = selectedId;
            final long[]               finalBalances   = balances;

            mainHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                showLoading(false);
                renderAccounts(finalAccounts, finalSelectedId, finalBalances);
            });
        });
    }

    private void renderAccounts(List<VirtualAccount> accounts,
                                String selectedId,
                                long[] balances) {
        accountsContainer.removeAllViews();

        if (accounts == null || accounts.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }
        tvEmpty.setVisibility(View.GONE);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < accounts.size(); i++) {
            VirtualAccount account = accounts.get(i);
            long           balance = balances[i];
            boolean        isSelected = account.getAccountId().equals(selectedId);

            View card = inflater.inflate(
                    R.layout.item_account_card, accountsContainer, false);

            TextView tvName    = card.findViewById(R.id.tv_account_name);
            TextView tvType    = card.findViewById(R.id.tv_account_type);
            TextView tvBalance = card.findViewById(R.id.tv_balance);
            TextView tvSelect  = card.findViewById(R.id.tv_select_badge);
            View     btnDetail = card.findViewById(R.id.btn_view_detail);
            View     btnSelect = card.findViewById(R.id.btn_select_spending);

            tvName.setText(account.getAccountName());
            tvType.setText(account.getAccountType().name());
            tvBalance.setText(MoneyFormatter.format(balance));

            // "Planning account" badge — only on SPENDING + selected
            if (isSelected && account.isSpending()) {
                tvSelect.setVisibility(View.VISIBLE);
            } else {
                tvSelect.setVisibility(View.GONE);
            }

            // Show "Use for planning" only on unselected SPENDING accounts
            if (account.isSpending() && !isSelected) {
                btnSelect.setVisibility(View.VISIBLE);
                btnSelect.setOnClickListener(v ->
                        selectForPlanning(account.getAccountId()));
            } else {
                btnSelect.setVisibility(View.GONE);
            }

            btnDetail.setOnClickListener(v -> {
                Intent intent = new Intent(this, AccountDetailActivity.class);
                intent.putExtra(AccountDetailActivity.EXTRA_ACCOUNT_ID,
                        account.getAccountId());
                startActivity(intent);
            });

            accountsContainer.addView(card);
        }
    }

    private void selectForPlanning(String accountId) {
        showLoading(true);
        AccountService svc = RandNestApp.from(this).getAccountService();
        getExecutor().execute(() -> {
            svc.selectSpendingAccount(accountId);
            mainHandler.post(() -> {
                if (!isFinishing() && !isDestroyed()) refreshFromDatabase();
            });
        });
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
