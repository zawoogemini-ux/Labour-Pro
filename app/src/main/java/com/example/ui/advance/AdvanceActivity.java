package com.example.ui.advance;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.data.AppDatabase;
import com.example.data.model.Advance;
import com.example.data.model.Site;
import com.example.data.model.Worker;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdvanceActivity extends AppCompatActivity implements AdvanceAdapter.OnAdvanceActionListener {

    private AppDatabase db;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private Spinner spinnerAdvanceSite;
    private TextView tvTotalAdvanceAmount;
    private RecyclerView rvAdvanceList;
    private View layoutEmptyAdvance;
    private ExtendedFloatingActionButton fabRecordAdvance;

    private AdvanceAdapter adapter;
    private List<Site> siteList = new ArrayList<>();
    private List<Worker> workerList = new ArrayList<>();
    private final Map<Integer, Worker> workerMap = new HashMap<>();
    private Site selectedSite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advance);

        db = AppDatabase.getDatabase(this);

        initViews();
        loadSitesAndWorkers();
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btnBackAdvance);
        btnBack.setOnClickListener(v -> finish());

        spinnerAdvanceSite = findViewById(R.id.spinnerAdvanceSite);
        tvTotalAdvanceAmount = findViewById(R.id.tvTotalAdvanceAmount);
        rvAdvanceList = findViewById(R.id.rvAdvanceList);
        layoutEmptyAdvance = findViewById(R.id.layoutEmptyAdvance);
        fabRecordAdvance = findViewById(R.id.fabRecordAdvance);

        adapter = new AdvanceAdapter(this, workerMap, this);
        rvAdvanceList.setLayoutManager(new LinearLayoutManager(this));
        rvAdvanceList.setAdapter(adapter);

        fabRecordAdvance.setOnClickListener(v -> showRecordAdvanceDialog());
    }

    private void loadSitesAndWorkers() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            siteList = db.siteDao().getActiveSites();
            workerList = db.workerDao().getActiveWorkers();

            workerMap.clear();
            for (Worker w : workerList) {
                workerMap.put(w.getId(), w);
            }

            mainHandler.post(() -> {
                if (siteList.isEmpty()) {
                    return;
                }

                // Create display list with "All Sites" as first item
                List<Site> displaySites = new ArrayList<>();
                Site allSite = new Site("All Construction Sites", "", "", 0, true);
                allSite.setId(-1);
                displaySites.add(allSite);
                displaySites.addAll(siteList);

                ArrayAdapter<Site> siteAdapter = new ArrayAdapter<>(
                        AdvanceActivity.this,
                        android.R.layout.simple_spinner_item,
                        displaySites
                );
                siteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerAdvanceSite.setAdapter(siteAdapter);

                spinnerAdvanceSite.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedSite = displaySites.get(position);
                        loadAdvances();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
            });
        });
    }

    private void loadAdvances() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Advance> advances;
            double total = 0.0;

            if (selectedSite != null && selectedSite.getId() != -1) {
                advances = db.advanceDao().getAdvancesForSite(selectedSite.getId());
                Double sum = db.advanceDao().getTotalAdvancesForSite(selectedSite.getId());
                total = sum != null ? sum : 0.0;
            } else {
                advances = db.advanceDao().getAllAdvances();
                Double sum = db.advanceDao().getTotalAdvancesAll();
                total = sum != null ? sum : 0.0;
            }

            final List<Advance> finalList = advances;
            final double finalTotal = total;

            mainHandler.post(() -> {
                adapter.setAdvances(finalList);
                tvTotalAdvanceAmount.setText(String.format(Locale.US, "%,.0f Ks", finalTotal));

                if (finalList.isEmpty()) {
                    layoutEmptyAdvance.setVisibility(View.VISIBLE);
                    rvAdvanceList.setVisibility(View.GONE);
                } else {
                    layoutEmptyAdvance.setVisibility(View.GONE);
                    rvAdvanceList.setVisibility(View.VISIBLE);
                }
            });
        });
    }

    private void showRecordAdvanceDialog() {
        if (workerList.isEmpty()) {
            Snackbar.make(rvAdvanceList, "Please add workers in Worker Master first.", Snackbar.LENGTH_SHORT).show();
            return;
        }

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_advance);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        Spinner spinnerWorker = dialog.findViewById(R.id.spinnerAdvanceWorker);
        TextInputEditText etAmount = dialog.findViewById(R.id.etAdvanceAmount);
        MaterialButton btnDatePicker = dialog.findViewById(R.id.btnAdvanceDatePicker);
        TextInputEditText etSupervisor = dialog.findViewById(R.id.etAdvanceSupervisor);
        TextInputEditText etRemark = dialog.findViewById(R.id.etAdvanceRemark);
        MaterialButton btnCancel = dialog.findViewById(R.id.btnCancelAdvance);
        MaterialButton btnSave = dialog.findViewById(R.id.btnSaveAdvance);

        // Workers for current site or all
        List<Worker> candidateWorkers = new ArrayList<>();
        if (selectedSite != null && selectedSite.getId() != -1) {
            for (Worker w : workerList) {
                if (w.getAssignedSiteId() == selectedSite.getId()) {
                    candidateWorkers.add(w);
                }
            }
        }
        if (candidateWorkers.isEmpty()) {
            candidateWorkers.addAll(workerList);
        }

        List<String> workerDisplayList = new ArrayList<>();
        for (Worker w : candidateWorkers) {
            workerDisplayList.add(w.getName() + " (" + w.getWorkerCode() + " - " + w.getWorkerType() + ")");
        }

        ArrayAdapter<String> workerSpinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                workerDisplayList
        );
        workerSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWorker.setAdapter(workerSpinnerAdapter);

        final Calendar dateCal = Calendar.getInstance();
        btnDatePicker.setText(dateFormat.format(dateCal.getTime()));

        btnDatePicker.setOnClickListener(v -> {
            DatePickerDialog dpd = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        dateCal.set(Calendar.YEAR, year);
                        dateCal.set(Calendar.MONTH, month);
                        dateCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        btnDatePicker.setText(dateFormat.format(dateCal.getTime()));
                    },
                    dateCal.get(Calendar.YEAR),
                    dateCal.get(Calendar.MONTH),
                    dateCal.get(Calendar.DAY_OF_MONTH)
            );
            dpd.show();
        });

        // Set default supervisor
        if (selectedSite != null && selectedSite.getSupervisorName() != null && !selectedSite.getSupervisorName().isEmpty()) {
            etSupervisor.setText(selectedSite.getSupervisorName());
        } else {
            etSupervisor.setText("Site Supervisor");
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        final List<Worker> finalCandidateWorkers = candidateWorkers;
        btnSave.setOnClickListener(v -> {
            String amountStr = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";
            if (amountStr.isEmpty()) {
                etAmount.setError("Amount is required");
                return;
            }

            double amount = 0.0;
            try {
                amount = Double.parseDouble(amountStr);
            } catch (Exception e) {
                etAmount.setError("Invalid number");
                return;
            }

            if (amount <= 0) {
                etAmount.setError("Must be greater than 0");
                return;
            }

            int selectedWorkerPos = spinnerWorker.getSelectedItemPosition();
            if (selectedWorkerPos < 0 || selectedWorkerPos >= finalCandidateWorkers.size()) {
                return;
            }
            Worker chosenWorker = finalCandidateWorkers.get(selectedWorkerPos);

            String supervisor = etSupervisor.getText() != null ? etSupervisor.getText().toString().trim() : "Supervisor";
            String remark = etRemark.getText() != null ? etRemark.getText().toString().trim() : "";
            String date = dateFormat.format(dateCal.getTime());
            int siteId = chosenWorker.getAssignedSiteId();

            final double fAmount = amount;
            final String fDate = date;
            final String fSupervisor = supervisor;
            final String fRemark = remark;

            AppDatabase.databaseWriteExecutor.execute(() -> {
                Advance newAdv = new Advance(
                        chosenWorker.getId(),
                        siteId,
                        fDate,
                        fAmount,
                        fSupervisor,
                        fRemark,
                        System.currentTimeMillis()
                );
                db.advanceDao().insert(newAdv);

                mainHandler.post(() -> {
                    dialog.dismiss();
                    loadAdvances();
                    Snackbar.make(
                            rvAdvanceList,
                            String.format(Locale.US, "Advance of %,.0f Ks recorded for %s", fAmount, chosenWorker.getName()),
                            Snackbar.LENGTH_SHORT
                    ).show();
                });
            });
        });

        dialog.show();
    }

    @Override
    public void onDeleteAdvance(Advance advance) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Advance Record")
                .setMessage("Are you sure you want to delete this advance record of " + String.format(Locale.US, "%,.0f Ks", advance.getAmount()) + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        db.advanceDao().delete(advance);
                        mainHandler.post(() -> {
                            loadAdvances();
                            Snackbar.make(rvAdvanceList, "Advance deleted", Snackbar.LENGTH_SHORT).show();
                        });
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
