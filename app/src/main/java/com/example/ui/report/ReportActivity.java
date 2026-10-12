package com.example.ui.report;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.data.AppDatabase;
import com.example.data.model.Advance;
import com.example.data.model.Attendance;
import com.example.data.model.Site;
import com.example.data.model.Worker;
import com.example.data.model.WorkerMusterSummary;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportActivity extends AppCompatActivity {

    private AppDatabase db;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final SimpleDateFormat monthFormat = new SimpleDateFormat("MMM yyyy", Locale.US);
    private final SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private Spinner spinnerReportSite;
    private MaterialButton btnReportMonthPicker;
    private MaterialButton btnExportCsv;
    private MaterialButton btnPrintSheet;

    private TextView tvReportGrossWages;
    private TextView tvReportAdvancesDeduct;
    private TextView tvReportNetPayable;
    private TextView tvReportTotalDays;
    private TextView tvReportTotalOt;
    private TextView tvReportActiveWorkers;
    private RecyclerView rvReportMusterList;

    private ReportMusterAdapter adapter;
    private List<Site> siteList = new ArrayList<>();
    private Site selectedSite;
    private final Calendar currentMonthCal = Calendar.getInstance();

    private final List<WorkerMusterSummary> currentSummaries = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        db = AppDatabase.getDatabase(this);

        initViews();
        loadSites();
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btnBackReport);
        btnBack.setOnClickListener(v -> finish());

        spinnerReportSite = findViewById(R.id.spinnerReportSite);
        btnReportMonthPicker = findViewById(R.id.btnReportMonthPicker);
        btnExportCsv = findViewById(R.id.btnExportCsv);
        btnPrintSheet = findViewById(R.id.btnPrintSheet);

        tvReportGrossWages = findViewById(R.id.tvReportGrossWages);
        tvReportAdvancesDeduct = findViewById(R.id.tvReportAdvancesDeduct);
        tvReportNetPayable = findViewById(R.id.tvReportNetPayable);
        tvReportTotalDays = findViewById(R.id.tvReportTotalDays);
        tvReportTotalOt = findViewById(R.id.tvReportTotalOt);
        tvReportActiveWorkers = findViewById(R.id.tvReportActiveWorkers);
        rvReportMusterList = findViewById(R.id.rvReportMusterList);

        adapter = new ReportMusterAdapter(this);
        rvReportMusterList.setLayoutManager(new LinearLayoutManager(this));
        rvReportMusterList.setAdapter(adapter);

        updateMonthButtonText();

        btnReportMonthPicker.setOnClickListener(v -> showMonthPicker());

        btnExportCsv.setOnClickListener(v -> exportCsv());

        btnPrintSheet.setOnClickListener(v -> printSheet());
    }

    private void updateMonthButtonText() {
        btnReportMonthPicker.setText(monthFormat.format(currentMonthCal.getTime()));
    }

    private void showMonthPicker() {
        DatePickerDialog dpd = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    currentMonthCal.set(Calendar.YEAR, year);
                    currentMonthCal.set(Calendar.MONTH, month);
                    currentMonthCal.set(Calendar.DAY_OF_MONTH, 1);
                    updateMonthButtonText();
                    loadReportData();
                },
                currentMonthCal.get(Calendar.YEAR),
                currentMonthCal.get(Calendar.MONTH),
                1
        );
        dpd.show();
    }

    private void loadSites() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            siteList = db.siteDao().getActiveSites();
            mainHandler.post(() -> {
                List<Site> displaySites = new ArrayList<>();
                Site allSite = new Site("All Construction Sites", "", "", 0, true);
                allSite.setId(-1);
                displaySites.add(allSite);
                displaySites.addAll(siteList);

                ArrayAdapter<Site> siteAdapter = new ArrayAdapter<>(
                        ReportActivity.this,
                        android.R.layout.simple_spinner_item,
                        displaySites
                );
                siteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerReportSite.setAdapter(siteAdapter);

                spinnerReportSite.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedSite = displaySites.get(position);
                        loadReportData();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
            });
        });
    }

    private void loadReportData() {
        // Calculate start and end date for selected month
        Calendar startCal = (Calendar) currentMonthCal.clone();
        startCal.set(Calendar.DAY_OF_MONTH, 1);
        String startDate = dbDateFormat.format(startCal.getTime());

        Calendar endCal = (Calendar) currentMonthCal.clone();
        endCal.set(Calendar.DAY_OF_MONTH, endCal.getActualMaximum(Calendar.DAY_OF_MONTH));
        String endDate = dbDateFormat.format(endCal.getTime());

        final int siteId = selectedSite != null ? selectedSite.getId() : -1;

        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Worker> workers;
            if (siteId != -1) {
                workers = db.workerDao().getActiveWorkersBySite(siteId);
                if (workers.isEmpty()) {
                    workers = db.workerDao().getActiveWorkers();
                }
            } else {
                workers = db.workerDao().getActiveWorkers();
            }

            List<Attendance> attendanceList;
            if (siteId != -1) {
                attendanceList = db.attendanceDao().getAttendanceForSiteBetweenDates(siteId, startDate, endDate);
            } else {
                attendanceList = db.attendanceDao().getAttendanceBetweenDates(startDate, endDate);
            }

            List<Advance> advanceList;
            if (siteId != -1) {
                advanceList = db.advanceDao().getAdvancesBetweenDates(startDate, endDate);
            } else {
                advanceList = db.advanceDao().getAdvancesBetweenDates(startDate, endDate);
            }

            Map<Integer, WorkerMusterSummary> summaryMap = new HashMap<>();
            for (Worker w : workers) {
                summaryMap.put(w.getId(), new WorkerMusterSummary(w));
            }

            // Aggregate Attendance
            for (Attendance att : attendanceList) {
                WorkerMusterSummary s = summaryMap.get(att.getWorkerId());
                if (s != null) {
                    if ("P".equalsIgnoreCase(att.getOverallStatus())) {
                        s.setPresentDays(s.getPresentDays() + 1);
                    } else if ("HD".equalsIgnoreCase(att.getOverallStatus())) {
                        s.setHalfDays(s.getHalfDays() + 1);
                    } else {
                        s.setAbsentDays(s.getAbsentDays() + 1);
                    }
                    s.setTotalOtHours(s.getTotalOtHours() + att.getOtHours());
                    s.setGrossWage(s.getGrossWage() + att.getTotalEarned());
                }
            }

            // Aggregate Advances
            for (Advance adv : advanceList) {
                WorkerMusterSummary s = summaryMap.get(adv.getWorkerId());
                if (s != null) {
                    s.setAdvanceDeductions(s.getAdvanceDeductions() + adv.getAmount());
                }
            }

            List<WorkerMusterSummary> resultList = new ArrayList<>(summaryMap.values());

            // Compute KPIs
            double totalGross = 0;
            double totalAdv = 0;
            double totalNet = 0;
            double totalDays = 0;
            double totalOt = 0;

            for (WorkerMusterSummary s : resultList) {
                totalGross += s.getGrossWage();
                totalAdv += s.getAdvanceDeductions();
                totalNet += s.getNetPayable();
                totalDays += s.getTotalManDays();
                totalOt += s.getTotalOtHours();
            }

            int maleWorkers = 0;
            int femaleWorkers = 0;
            for (WorkerMusterSummary s : resultList) {
                if (s.getWorker() != null && "Female".equalsIgnoreCase(s.getWorker().getGender())) {
                    femaleWorkers++;
                } else {
                    maleWorkers++;
                }
            }

            final double fGross = totalGross;
            final double fAdv = totalAdv;
            final double fNet = totalNet;
            final double fDays = totalDays;
            final double fOt = totalOt;
            final int fWorkers = resultList.size();
            final int fMale = maleWorkers;
            final int fFemale = femaleWorkers;

            mainHandler.post(() -> {
                currentSummaries.clear();
                currentSummaries.addAll(resultList);
                adapter.setSummaries(resultList);

                tvReportGrossWages.setText(String.format(Locale.US, "%,.0f Ks", fGross));
                tvReportAdvancesDeduct.setText(String.format(Locale.US, "-%,.0f Ks", fAdv));
                tvReportNetPayable.setText(String.format(Locale.US, "%,.0f Ks", fNet));
                tvReportTotalDays.setText(String.format(Locale.US, "Man-Days: %.1f", fDays));
                tvReportTotalOt.setText(String.format(Locale.US, "Total OT: %.1fh", fOt));
                tvReportActiveWorkers.setText(String.format(Locale.US, "Workers: %d (ကျား %d, မ %d)", fWorkers, fMale, fFemale));
            });
        });
    }

    private void exportCsv() {
        if (currentSummaries.isEmpty()) {
            Toast.makeText(this, "No data available to export", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String periodLabel = monthFormat.format(currentMonthCal.getTime());
            File csvFile = CsvExportHelper.exportMusterToCsv(this, selectedSite, periodLabel, currentSummaries);
            CsvExportHelper.shareCsvFile(this, csvFile);
        } catch (Exception e) {
            Toast.makeText(this, "Error exporting CSV: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void printSheet() {
        if (currentSummaries.isEmpty()) {
            Toast.makeText(this, "No data available to print", Toast.LENGTH_SHORT).show();
            return;
        }

        String periodLabel = monthFormat.format(currentMonthCal.getTime());
        PrintHelper.printMusterSheet(this, selectedSite, periodLabel, currentSummaries);
    }
}
