package com.example.ui.workermaster;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.data.AppDatabase;
import com.example.data.model.Site;
import com.example.data.model.Worker;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WorkerMasterActivity extends AppCompatActivity implements WorkerAdapter.OnWorkerActionListener {

    private AppDatabase db;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private EditText etSearchWorker;
    private ChipGroup chipGroupSiteFilter;
    private ChipGroup chipGroupTradeFilter;
    private RecyclerView rvWorkerList;
    private View layoutEmptyWorker;
    private TextView tvWorkerTotalBadge;
    private TextView tvWorkerMaleBadge;
    private TextView tvWorkerFemaleBadge;
    private ExtendedFloatingActionButton fabAddWorker;

    private WorkerAdapter adapter;
    private List<Worker> workerList = new ArrayList<>();
    private List<String> allWorkerCodes = new ArrayList<>();
    private List<Site> siteList = new ArrayList<>();
    private final Map<Integer, String> siteNameMap = new HashMap<>();

    private String currentSearchQuery = "";
    private String currentTradeFilter = "All";
    private int currentSiteFilterId = -1;

    private final String[] TRADES = {
            "Mason",
            "Carpenter",
            "Helper",
            "Coolie",
            "Bar Bender",
            "Electrician",
            "Plumber",
            "Painter",
            "Welder",
            "Foreman"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_master);

        db = AppDatabase.getDatabase(this);

        initViews();
        loadSitesAndWorkers();
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btnBackWorkerMaster);
        btnBack.setOnClickListener(v -> finish());

        etSearchWorker = findViewById(R.id.etSearchWorker);
        chipGroupSiteFilter = findViewById(R.id.chipGroupSiteFilter);
        chipGroupTradeFilter = findViewById(R.id.chipGroupTradeFilter);
        rvWorkerList = findViewById(R.id.rvWorkerList);
        layoutEmptyWorker = findViewById(R.id.layoutEmptyWorker);
        tvWorkerTotalBadge = findViewById(R.id.tvWorkerTotalBadge);
        tvWorkerMaleBadge = findViewById(R.id.tvWorkerMaleBadge);
        tvWorkerFemaleBadge = findViewById(R.id.tvWorkerFemaleBadge);
        fabAddWorker = findViewById(R.id.fabAddWorker);

        adapter = new WorkerAdapter(this, siteNameMap, this);
        rvWorkerList.setLayoutManager(new LinearLayoutManager(this));
        rvWorkerList.setAdapter(adapter);

        etSearchWorker.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString();
                applyCurrentFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        chipGroupTradeFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                currentTradeFilter = "All";
            } else {
                Chip chip = findViewById(checkedIds.get(0));
                currentTradeFilter = chip != null ? chip.getText().toString() : "All";
            }
            applyCurrentFilters();
        });

        fabAddWorker.setOnClickListener(v -> showAddEditWorkerDialog(null));
    }

    private void setupSiteFilterChips() {
        if (chipGroupSiteFilter == null) return;
        chipGroupSiteFilter.removeAllViews();

        Chip allChip = new Chip(this);
        allChip.setText("All Sites (ဆိုဒ်အားလုံး)");
        allChip.setCheckable(true);
        allChip.setChecked(currentSiteFilterId == -1);
        allChip.setOnClickListener(v -> {
            currentSiteFilterId = -1;
            applyCurrentFilters();
        });
        chipGroupSiteFilter.addView(allChip);

        for (Site site : siteList) {
            Chip siteChip = new Chip(this);
            siteChip.setText(site.getSiteName());
            siteChip.setCheckable(true);
            siteChip.setChecked(currentSiteFilterId == site.getId());
            final int sId = site.getId();
            siteChip.setOnClickListener(v -> {
                currentSiteFilterId = sId;
                applyCurrentFilters();
            });
            chipGroupSiteFilter.addView(siteChip);
        }
    }

    private void loadSitesAndWorkers() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            siteList = db.siteDao().getActiveSites();
            siteNameMap.clear();
            for (Site s : siteList) {
                siteNameMap.put(s.getId(), s.getSiteName());
            }

            workerList = db.workerDao().getActiveWorkers();
            allWorkerCodes = db.workerDao().getAllWorkerCodes();

            mainHandler.post(() -> {
                setupSiteFilterChips();
                adapter.setWorkers(workerList);
                applyCurrentFilters();
            });
        });
    }

    private String generateNextSequentialWorkerCode() {
        int maxNumber = 100; // Baseline so starting worker code is W-101 or sequentially after highest
        if (allWorkerCodes != null) {
            for (String code : allWorkerCodes) {
                if (code == null) continue;
                java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(\\d+)").matcher(code.trim());
                while (matcher.find()) {
                    try {
                        int num = Integer.parseInt(matcher.group(1));
                        if (num > maxNumber) {
                            maxNumber = num;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return String.format(Locale.US, "W-%d", maxNumber + 1);
    }

    private void applyCurrentFilters() {
        adapter.applyFilter(currentSearchQuery, currentTradeFilter, currentSiteFilterId);
        int count = adapter.getFilteredCount();
        int male = adapter.getFilteredMaleCount();
        int female = adapter.getFilteredFemaleCount();

        tvWorkerTotalBadge.setText(String.format(Locale.US, "စုစုပေါင်း: %d", count));
        if (tvWorkerMaleBadge != null) {
            tvWorkerMaleBadge.setText(String.format(Locale.US, "ကျား %d", male));
        }
        if (tvWorkerFemaleBadge != null) {
            tvWorkerFemaleBadge.setText(String.format(Locale.US, "မ %d", female));
        }

        if (count == 0) {
            layoutEmptyWorker.setVisibility(View.VISIBLE);
            rvWorkerList.setVisibility(View.GONE);
        } else {
            layoutEmptyWorker.setVisibility(View.GONE);
            rvWorkerList.setVisibility(View.VISIBLE);
        }
    }

    private void showAddEditWorkerDialog(Worker workerToEdit) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_worker);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView tvTitle = dialog.findViewById(R.id.tvDialogTitle);
        TextView tvWorkerIdStatusBadge = dialog.findViewById(R.id.tvWorkerIdStatusBadge);
        TextInputEditText etWorkerCode = dialog.findViewById(R.id.etWorkerCode);
        MaterialButton btnAutoWorkerCode = dialog.findViewById(R.id.btnAutoWorkerCode);
        TextInputEditText etWorkerName = dialog.findViewById(R.id.etWorkerName);
        Spinner spinnerTrade = dialog.findViewById(R.id.spinnerWorkerTrade);
        RadioGroup rgGender = dialog.findViewById(R.id.rgGender);
        RadioButton rbMale = dialog.findViewById(R.id.rbMale);
        RadioButton rbFemale = dialog.findViewById(R.id.rbFemale);
        TextInputEditText etDailyRate = dialog.findViewById(R.id.etDailyRate);
        TextInputEditText etOtRate = dialog.findViewById(R.id.etOtRate);
        TextInputEditText etPhoneNumber = dialog.findViewById(R.id.etPhoneNumber);
        Spinner spinnerSite = dialog.findViewById(R.id.spinnerWorkerSite);
        MaterialButton btnCancel = dialog.findViewById(R.id.btnCancelWorker);
        MaterialButton btnSave = dialog.findViewById(R.id.btnSaveWorker);

        final String autoSequentialCode = generateNextSequentialWorkerCode();

        // Trade Spinner
        ArrayAdapter<String> tradeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                TRADES
        );
        tradeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTrade.setAdapter(tradeAdapter);

        // Site Spinner
        ArrayAdapter<Site> siteAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                siteList
        );
        siteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSite.setAdapter(siteAdapter);

        if (workerToEdit != null) {
            tvTitle.setText("Edit Worker (လုပ်သား အချက်အလက် ပြင်ဆင်ရန်)");
            btnSave.setText("Update Worker (ပြင်ဆင်ချက် သိမ်းရန်)");
            etWorkerCode.setText(workerToEdit.getWorkerCode());
            etWorkerName.setText(workerToEdit.getName());

            if (tvWorkerIdStatusBadge != null) {
                tvWorkerIdStatusBadge.setText("✏️ Saved ID");
                tvWorkerIdStatusBadge.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.badge_custom_bg));
                tvWorkerIdStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.badge_custom_text));
            }

            if ("Female".equalsIgnoreCase(workerToEdit.getGender())) {
                rbFemale.setChecked(true);
            } else {
                rbMale.setChecked(true);
            }

            for (int i = 0; i < TRADES.length; i++) {
                if (TRADES[i].equalsIgnoreCase(workerToEdit.getWorkerType())) {
                    spinnerTrade.setSelection(i);
                    break;
                }
            }

            etDailyRate.setText(String.format(Locale.US, "%.0f", workerToEdit.getDailyRate()));
            etOtRate.setText(String.format(Locale.US, "%.0f", workerToEdit.getOtRate()));
            etPhoneNumber.setText(workerToEdit.getPhoneNumber());

            for (int i = 0; i < siteList.size(); i++) {
                if (siteList.get(i).getId() == workerToEdit.getAssignedSiteId()) {
                    spinnerSite.setSelection(i);
                    break;
                }
            }
        } else {
            tvTitle.setText("Add New Worker (လုပ်သားသစ် စာရင်းသွင်း)");
            btnSave.setText("Save Worker (သိမ်းဆည်းမည်)");
            // Auto sequential Worker ID generated
            etWorkerCode.setText(autoSequentialCode);
            if (tvWorkerIdStatusBadge != null) {
                tvWorkerIdStatusBadge.setText("⚡ Auto Sequence (" + autoSequentialCode + ")");
                tvWorkerIdStatusBadge.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.badge_auto_bg));
                tvWorkerIdStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.badge_auto_text));
            }
        }

        // Live Auto vs Custom ID Indicator
        etWorkerCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (tvWorkerIdStatusBadge == null) return;
                String current = s.toString().trim();
                if (current.equalsIgnoreCase(autoSequentialCode)) {
                    tvWorkerIdStatusBadge.setText("⚡ Auto Sequence (" + autoSequentialCode + ")");
                    tvWorkerIdStatusBadge.setBackgroundTintList(ContextCompat.getColorStateList(WorkerMasterActivity.this, R.color.badge_auto_bg));
                    tvWorkerIdStatusBadge.setTextColor(ContextCompat.getColor(WorkerMasterActivity.this, R.color.badge_auto_text));
                } else if (!current.isEmpty()) {
                    tvWorkerIdStatusBadge.setText("✏️ Custom ID (" + current + ")");
                    tvWorkerIdStatusBadge.setBackgroundTintList(ContextCompat.getColorStateList(WorkerMasterActivity.this, R.color.badge_custom_bg));
                    tvWorkerIdStatusBadge.setTextColor(ContextCompat.getColor(WorkerMasterActivity.this, R.color.badge_custom_text));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Fast Auto Button allows resetting to sequential Auto ID anytime
        if (btnAutoWorkerCode != null) {
            btnAutoWorkerCode.setOnClickListener(v -> {
                etWorkerCode.setText(autoSequentialCode);
                etWorkerCode.setSelection(autoSequentialCode.length());
                if (tvWorkerIdStatusBadge != null) {
                    tvWorkerIdStatusBadge.setText("⚡ Auto Sequence (" + autoSequentialCode + ")");
                    tvWorkerIdStatusBadge.setBackgroundTintList(ContextCompat.getColorStateList(WorkerMasterActivity.this, R.color.badge_auto_bg));
                    tvWorkerIdStatusBadge.setTextColor(ContextCompat.getColor(WorkerMasterActivity.this, R.color.badge_auto_text));
                }
                Snackbar.make(etWorkerCode, "Applied Auto ID: " + autoSequentialCode, Snackbar.LENGTH_SHORT).show();
            });
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String code = etWorkerCode.getText() != null ? etWorkerCode.getText().toString().trim() : "";
            String name = etWorkerName.getText() != null ? etWorkerName.getText().toString().trim() : "";
            String dailyRateStr = etDailyRate.getText() != null ? etDailyRate.getText().toString().trim() : "0";
            String otRateStr = etOtRate.getText() != null ? etOtRate.getText().toString().trim() : "0";
            String phone = etPhoneNumber.getText() != null ? etPhoneNumber.getText().toString().trim() : "";

            if (name.isEmpty()) {
                etWorkerName.setError("Name is required");
                return;
            }
            if (code.isEmpty()) {
                code = "W-" + System.currentTimeMillis() % 1000;
            }

            double dailyRate = 25000.0;
            try {
                dailyRate = Double.parseDouble(dailyRateStr);
            } catch (Exception ignored) {}

            double otRate = 3500.0;
            try {
                otRate = Double.parseDouble(otRateStr);
            } catch (Exception ignored) {}

            String trade = spinnerTrade.getSelectedItem() != null ? spinnerTrade.getSelectedItem().toString() : "Helper";
            String gender = rbFemale.isChecked() ? "Female" : "Male";

            int siteId = 0;
            if (spinnerSite.getSelectedItem() instanceof Site) {
                siteId = ((Site) spinnerSite.getSelectedItem()).getId();
            }

            final double fDaily = dailyRate;
            final double fOt = otRate;
            final String fCode = code;
            final String fName = name;
            final String fTrade = trade;
            final String fGender = gender;
            final String fPhone = phone;
            final int fSiteId = siteId;

            AppDatabase.databaseWriteExecutor.execute(() -> {
                if (workerToEdit != null) {
                    workerToEdit.setWorkerCode(fCode);
                    workerToEdit.setName(fName);
                    workerToEdit.setWorkerType(fTrade);
                    workerToEdit.setGender(fGender);
                    workerToEdit.setDailyRate(fDaily);
                    workerToEdit.setOtRate(fOt);
                    workerToEdit.setPhoneNumber(fPhone);
                    workerToEdit.setAssignedSiteId(fSiteId);
                    db.workerDao().update(workerToEdit);
                } else {
                    Worker newWorker = new Worker(
                            fCode,
                            fName,
                            fGender,
                            fTrade,
                            fDaily,
                            fOt,
                            fPhone,
                            fSiteId,
                            true,
                            System.currentTimeMillis()
                    );
                    db.workerDao().insert(newWorker);
                }

                mainHandler.post(() -> {
                    dialog.dismiss();
                    loadSitesAndWorkers();
                    Snackbar.make(
                            rvWorkerList,
                            workerToEdit != null ? "Worker updated successfully" : "Worker added successfully",
                            Snackbar.LENGTH_SHORT
                    ).show();
                });
            });
        });

        dialog.show();
    }

    @Override
    public void onEditWorker(Worker worker) {
        showAddEditWorkerDialog(worker);
    }

    @Override
    public void onDeleteWorker(Worker worker) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Worker")
                .setMessage("Are you sure you want to remove " + worker.getName() + " (" + worker.getWorkerCode() + ")? Existing attendance logs will be preserved.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        worker.setActive(false);
                        db.workerDao().update(worker);
                        mainHandler.post(() -> {
                            loadSitesAndWorkers();
                            Snackbar.make(rvWorkerList, worker.getName() + " removed", Snackbar.LENGTH_SHORT).show();
                        });
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
