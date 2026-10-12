package com.example;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.data.AppDatabase;
import com.example.data.model.Advance;
import com.example.data.model.Attendance;
import com.example.data.model.Site;
import com.example.data.model.Worker;
import com.example.ui.advance.AdvanceActivity;
import com.example.ui.attendance.AttendanceActivity;
import com.example.ui.report.ReportActivity;
import com.example.ui.workermaster.WorkerMasterActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private AppDatabase db;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private final SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM yyyy", Locale.US);

    private Spinner spinnerSites;
    private MaterialButton btnAddSite;
    private MaterialButton btnDashboardDate;
    private TextView tvStatsDateLabel;

    private TextView tvTotalWorkers;
    private TextView tvMaleCount;
    private TextView tvFemaleCount;
    private TextView tvPresentCount;
    private TextView tvHalfDayCount;
    private TextView tvAbsentCount;
    private TextView tvDailyWageExpense;
    private TextView tvTotalOtHours;
    private TextView tvAdvancesToday;
    private LinearLayout llSiteGenderList;
    private TextView tvSiteBreakdownTotalBadge;

    private MaterialCardView cardNavAttendance;
    private MaterialCardView cardNavWorkerMaster;
    private MaterialCardView cardNavAdvanceLedger;
    private MaterialCardView cardNavReports;

    private List<Site> siteList = new ArrayList<>();
    private Site selectedSite;
    private final Calendar selectedCalendar = Calendar.getInstance();

    public static class SiteGenderStat {
        public final Site site;
        public final int total;
        public final int male;
        public final int female;

        public SiteGenderStat(Site site, int total, int male, int female) {
            this.site = site;
            this.total = total;
            this.male = male;
            this.female = female;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = AppDatabase.getDatabase(this);

        initViews();
        setupNavigation();
        loadSites();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardStats();
    }

    private void initViews() {
        spinnerSites = findViewById(R.id.spinnerSites);
        btnAddSite = findViewById(R.id.btnAddSite);
        btnDashboardDate = findViewById(R.id.btnDashboardDate);
        tvStatsDateLabel = findViewById(R.id.tvStatsDateLabel);

        tvTotalWorkers = findViewById(R.id.tvTotalWorkers);
        tvMaleCount = findViewById(R.id.tvMaleCount);
        tvFemaleCount = findViewById(R.id.tvFemaleCount);
        tvPresentCount = findViewById(R.id.tvPresentCount);
        tvHalfDayCount = findViewById(R.id.tvHalfDayCount);
        tvAbsentCount = findViewById(R.id.tvAbsentCount);
        tvDailyWageExpense = findViewById(R.id.tvDailyWageExpense);
        tvTotalOtHours = findViewById(R.id.tvTotalOtHours);
        tvAdvancesToday = findViewById(R.id.tvAdvancesToday);
        llSiteGenderList = findViewById(R.id.llSiteGenderList);
        tvSiteBreakdownTotalBadge = findViewById(R.id.tvSiteBreakdownTotalBadge);

        cardNavAttendance = findViewById(R.id.cardNavAttendance);
        cardNavWorkerMaster = findViewById(R.id.cardNavWorkerMaster);
        cardNavAdvanceLedger = findViewById(R.id.cardNavAdvanceLedger);
        cardNavReports = findViewById(R.id.cardNavReports);

        updateDateButtonText();

        btnDashboardDate.setOnClickListener(v -> showDatePicker());
        btnAddSite.setOnClickListener(v -> showAddSiteDialog());
    }

    private void setupNavigation() {
        cardNavAttendance.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AttendanceActivity.class);
            startActivity(intent);
        });

        cardNavWorkerMaster.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, WorkerMasterActivity.class);
            startActivity(intent);
        });

        cardNavAdvanceLedger.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AdvanceActivity.class);
            startActivity(intent);
        });

        cardNavReports.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ReportActivity.class);
            startActivity(intent);
        });
    }

    private void updateDateButtonText() {
        Calendar today = Calendar.getInstance();
        boolean isToday = today.get(Calendar.YEAR) == selectedCalendar.get(Calendar.YEAR)
                && today.get(Calendar.DAY_OF_YEAR) == selectedCalendar.get(Calendar.DAY_OF_YEAR);

        if (isToday) {
            btnDashboardDate.setText("Today");
            tvStatsDateLabel.setText("Today's Site Summary");
        } else {
            btnDashboardDate.setText(displayFormat.format(selectedCalendar.getTime()));
            tvStatsDateLabel.setText(displayFormat.format(selectedCalendar.getTime()) + " Summary");
        }
    }

    private void showDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedCalendar.set(Calendar.YEAR, year);
                    selectedCalendar.set(Calendar.MONTH, month);
                    selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateButtonText();
                    loadDashboardStats();
                },
                selectedCalendar.get(Calendar.YEAR),
                selectedCalendar.get(Calendar.MONTH),
                selectedCalendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void loadSites() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Site> sites = db.siteDao().getActiveSites();
            mainHandler.post(() -> {
                List<Site> displaySites = new ArrayList<>();
                Site allSitesOption = new Site("All Sites (ဆိုဒ်အားလုံး)", "All Construction Projects", "Operations", System.currentTimeMillis(), true);
                allSitesOption.setId(-1);
                displaySites.add(allSitesOption);
                displaySites.addAll(sites);
                siteList = displaySites;

                ArrayAdapter<Site> adapter = new ArrayAdapter<>(
                        MainActivity.this,
                        android.R.layout.simple_spinner_item,
                        siteList
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerSites.setAdapter(adapter);

                spinnerSites.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedSite = siteList.get(position);
                        loadDashboardStats();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
            });
        });
    }

    private void loadDashboardStats() {
        if (selectedSite == null && !siteList.isEmpty()) {
            selectedSite = siteList.get(0);
        }

        final int siteId = selectedSite != null ? selectedSite.getId() : -1;
        final String dateStr = dateFormat.format(selectedCalendar.getTime());

        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Worker> allActiveWorkers = db.workerDao().getActiveWorkers();

            // Count for selected site (or all sites if siteId == -1)
            int totalWorkers = 0;
            int maleCount = 0;
            int femaleCount = 0;

            for (Worker w : allActiveWorkers) {
                if (siteId == -1 || w.getAssignedSiteId() == siteId) {
                    totalWorkers++;
                    if ("Female".equalsIgnoreCase(w.getGender())) {
                        femaleCount++;
                    } else {
                        maleCount++;
                    }
                }
            }

            // Site-wise breakdown for each active site
            List<SiteGenderStat> siteStats = new ArrayList<>();
            for (Site s : siteList) {
                if (s.getId() == -1) continue;
                int sTotal = 0;
                int sMale = 0;
                int sFemale = 0;
                for (Worker w : allActiveWorkers) {
                    if (w.getAssignedSiteId() == s.getId()) {
                        sTotal++;
                        if ("Female".equalsIgnoreCase(w.getGender())) {
                            sFemale++;
                        } else {
                            sMale++;
                        }
                    }
                }
                siteStats.add(new SiteGenderStat(s, sTotal, sMale, sFemale));
            }

            List<Attendance> attendanceList;
            if (siteId != -1) {
                attendanceList = db.attendanceDao().getAttendanceForDateAndSite(dateStr, siteId);
            } else {
                attendanceList = db.attendanceDao().getAttendanceForDate(dateStr);
            }

            int pCount = 0;
            int hdCount = 0;
            int aCount = 0;
            double otHours = 0.0;
            double wageExpense = 0.0;

            for (Attendance a : attendanceList) {
                if ("P".equalsIgnoreCase(a.getOverallStatus())) {
                    pCount++;
                } else if ("HD".equalsIgnoreCase(a.getOverallStatus())) {
                    hdCount++;
                } else {
                    aCount++;
                }
                otHours += a.getOtHours();
                wageExpense += a.getTotalEarned();
            }

            // Get Advances given today
            List<Advance> advancesToday = db.advanceDao().getAdvancesBetweenDates(dateStr, dateStr);
            double advancesSum = 0.0;
            for (Advance adv : advancesToday) {
                if (siteId == -1 || adv.getSiteId() == siteId) {
                    advancesSum += adv.getAmount();
                }
            }

            final int fTotalWorkers = totalWorkers;
            final int fMale = maleCount;
            final int fFemale = femaleCount;
            final List<SiteGenderStat> fSiteStats = siteStats;
            final int fP = pCount;
            final int fHd = hdCount;
            final int fA = aCount;
            final double fOt = otHours;
            final double fWage = wageExpense;
            final double fAdv = advancesSum;

            mainHandler.post(() -> {
                tvTotalWorkers.setText(String.valueOf(fTotalWorkers));
                if (tvMaleCount != null) {
                    tvMaleCount.setText(String.format(Locale.US, "ကျား %d", fMale));
                }
                if (tvFemaleCount != null) {
                    tvFemaleCount.setText(String.format(Locale.US, "မ %d", fFemale));
                }
                tvPresentCount.setText(String.valueOf(fP));
                tvHalfDayCount.setText(String.valueOf(fHd));
                tvAbsentCount.setText(String.valueOf(fA));
                tvDailyWageExpense.setText(String.format(Locale.US, "%,.0f Ks", fWage));
                tvTotalOtHours.setText(String.format(Locale.US, "OT: %.1fh", fOt));
                tvAdvancesToday.setText(String.format(Locale.US, "Adv: %,.0f Ks", fAdv));

                if (tvSiteBreakdownTotalBadge != null) {
                    if (siteId == -1) {
                        tvSiteBreakdownTotalBadge.setText(String.format(Locale.US, "All Sites (%d ဦး)", fTotalWorkers));
                    } else {
                        tvSiteBreakdownTotalBadge.setText(String.format(Locale.US, "%s (%d ဦး)", selectedSite.getSiteName(), fTotalWorkers));
                    }
                }

                // Populate Site-wise Gender Breakdown Cards
                if (llSiteGenderList != null) {
                    llSiteGenderList.removeAllViews();
                    for (SiteGenderStat stat : fSiteStats) {
                        View row = getLayoutInflater().inflate(R.layout.item_site_gender_breakdown, llSiteGenderList, false);
                        TextView tvName = row.findViewById(R.id.tvSiteRowName);
                        TextView tvTotalBadge = row.findViewById(R.id.tvSiteRowTotalBadge);
                        TextView tvLocation = row.findViewById(R.id.tvSiteRowLocation);
                        TextView tvMaleBadge = row.findViewById(R.id.tvSiteRowMaleBadge);
                        TextView tvFemaleBadge = row.findViewById(R.id.tvSiteRowFemaleBadge);
                        MaterialCardView card = row.findViewById(R.id.cardSiteRow);

                        tvName.setText(stat.site.getSiteName());
                        tvTotalBadge.setText(String.format(Locale.US, "%d ဦး", stat.total));
                        String locStr = (stat.site.getLocation() != null ? stat.site.getLocation() : "")
                                + (stat.site.getSupervisorName() != null && !stat.site.getSupervisorName().isEmpty() ? " • " + stat.site.getSupervisorName() : "");
                        tvLocation.setText(locStr);
                        tvMaleBadge.setText(String.format(Locale.US, "ကျား (Male): %d", stat.male));
                        tvFemaleBadge.setText(String.format(Locale.US, "မ (Female): %d", stat.female));

                        if (stat.site.getId() == siteId) {
                            card.setStrokeColor(0xFF2563EB);
                            card.setStrokeWidth(2);
                            card.setCardBackgroundColor(0xFFF1F5F9);
                        } else {
                            card.setStrokeColor(0xFFE2E8F0);
                            card.setStrokeWidth(1);
                            card.setCardBackgroundColor(0xFFFFFFFF);
                        }

                        final int clickSiteId = stat.site.getId();
                        row.setOnClickListener(v -> {
                            for (int i = 0; i < siteList.size(); i++) {
                                if (siteList.get(i).getId() == clickSiteId) {
                                    spinnerSites.setSelection(i);
                                    break;
                                }
                            }
                        });

                        llSiteGenderList.addView(row);
                    }
                }
            });
        });
    }

    private void showAddSiteDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_site);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextInputEditText etSiteName = dialog.findViewById(R.id.etSiteName);
        TextInputEditText etSiteLocation = dialog.findViewById(R.id.etSiteLocation);
        TextInputEditText etSiteSupervisor = dialog.findViewById(R.id.etSiteSupervisor);
        MaterialButton btnCancel = dialog.findViewById(R.id.btnCancelSite);
        MaterialButton btnSave = dialog.findViewById(R.id.btnSaveSite);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String name = etSiteName.getText() != null ? etSiteName.getText().toString().trim() : "";
            String location = etSiteLocation.getText() != null ? etSiteLocation.getText().toString().trim() : "";
            String supervisor = etSiteSupervisor.getText() != null ? etSiteSupervisor.getText().toString().trim() : "";

            if (name.isEmpty()) {
                etSiteName.setError("Site name is required");
                return;
            }

            AppDatabase.databaseWriteExecutor.execute(() -> {
                Site newSite = new Site(name, location, supervisor, System.currentTimeMillis(), true);
                long newId = db.siteDao().insert(newSite);
                newSite.setId((int) newId);

                mainHandler.post(() -> {
                    dialog.dismiss();
                    loadSites();
                    Snackbar.make(
                            spinnerSites,
                            "Construction site created successfully!",
                            Snackbar.LENGTH_SHORT
                    ).show();
                });
            });
        });

        dialog.show();
    }
}
