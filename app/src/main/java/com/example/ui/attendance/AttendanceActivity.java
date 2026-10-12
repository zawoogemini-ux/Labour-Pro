package com.example.ui.attendance;

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

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.data.AppDatabase;
import com.example.data.model.Advance;
import com.example.data.model.Attendance;
import com.example.data.model.Site;
import com.example.data.model.Worker;
import com.example.data.model.WorkerAttendanceItem;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AttendanceActivity extends AppCompatActivity implements AttendanceAdapter.OnAttendanceChangeListener {

    private AppDatabase db;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private final SimpleDateFormat displayFormat = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.US);

    private Calendar selectedCalendar = Calendar.getInstance();
    private List<Site> siteList = new ArrayList<>();
    private Site selectedSite;

    private Spinner spinnerAttendanceSite;
    private TextView tvHeaderSiteName;
    private MaterialButton btnDatePicker;
    private MaterialButton btnToday;
    private ImageButton btnPrevDay;
    private ImageButton btnNextDay;
    private MaterialButton btnMarkAllPresent;
    private MaterialButton btnMarkAllAbsent;
    private TextView tvWorkerCountHeader;
    private RecyclerView rvAttendanceList;

    private TextView tvBottomPresentCount;
    private TextView tvBottomHdCount;
    private TextView tvBottomAbsentCount;
    private TextView tvBottomOtCount;
    private TextView tvBottomTotalWages;
    private MaterialButton btnSaveAttendance;

    private AttendanceAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        db = AppDatabase.getDatabase(this);

        initViews();
        setupRecyclerView();
        loadSites();
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        tvHeaderSiteName = findViewById(R.id.tvHeaderSiteName);
        spinnerAttendanceSite = findViewById(R.id.spinnerAttendanceSite);
        btnDatePicker = findViewById(R.id.btnDatePicker);
        btnToday = findViewById(R.id.btnToday);
        btnPrevDay = findViewById(R.id.btnPrevDay);
        btnNextDay = findViewById(R.id.btnNextDay);
        btnMarkAllPresent = findViewById(R.id.btnMarkAllPresent);
        btnMarkAllAbsent = findViewById(R.id.btnMarkAllAbsent);
        tvWorkerCountHeader = findViewById(R.id.tvWorkerCountHeader);
        rvAttendanceList = findViewById(R.id.rvAttendanceList);

        tvBottomPresentCount = findViewById(R.id.tvBottomPresentCount);
        tvBottomHdCount = findViewById(R.id.tvBottomHdCount);
        tvBottomAbsentCount = findViewById(R.id.tvBottomAbsentCount);
        tvBottomOtCount = findViewById(R.id.tvBottomOtCount);
        tvBottomTotalWages = findViewById(R.id.tvBottomTotalWages);
        btnSaveAttendance = findViewById(R.id.btnSaveAttendance);

        updateDateButtonText();

        btnDatePicker.setOnClickListener(v -> showDatePicker());

        btnToday.setOnClickListener(v -> {
            selectedCalendar = Calendar.getInstance();
            updateDateButtonText();
            loadAttendanceForSelectedSiteAndDate();
        });

        btnPrevDay.setOnClickListener(v -> {
            selectedCalendar.add(Calendar.DAY_OF_MONTH, -1);
            updateDateButtonText();
            loadAttendanceForSelectedSiteAndDate();
        });

        btnNextDay.setOnClickListener(v -> {
            selectedCalendar.add(Calendar.DAY_OF_MONTH, 1);
            updateDateButtonText();
            loadAttendanceForSelectedSiteAndDate();
        });

        btnMarkAllPresent.setOnClickListener(v -> {
            adapter.markAll("P");
            Snackbar.make(rvAttendanceList, "All workers marked Present", Snackbar.LENGTH_SHORT).show();
        });

        btnMarkAllAbsent.setOnClickListener(v -> {
            adapter.markAll("A");
            Snackbar.make(rvAttendanceList, "All workers marked Absent", Snackbar.LENGTH_SHORT).show();
        });

        btnSaveAttendance.setOnClickListener(v -> saveAttendanceRecords());
    }

    private void setupRecyclerView() {
        adapter = new AttendanceAdapter(this, this);
        rvAttendanceList.setLayoutManager(new LinearLayoutManager(this));
        rvAttendanceList.setAdapter(adapter);
    }

    private void updateDateButtonText() {
        btnDatePicker.setText(displayFormat.format(selectedCalendar.getTime()));
    }

    private void showDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedCalendar.set(Calendar.YEAR, year);
                    selectedCalendar.set(Calendar.MONTH, month);
                    selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateButtonText();
                    loadAttendanceForSelectedSiteAndDate();
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
                siteList = sites;
                if (siteList.isEmpty()) {
                    // Fallback create site if none
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        Site defaultSite = new Site("Main Construction Site", "Central Zone", "Site Engineer", System.currentTimeMillis(), true);
                        db.siteDao().insert(defaultSite);
                        loadSites();
                    });
                    return;
                }

                ArrayAdapter<Site> spinnerAdapter = new ArrayAdapter<>(
                        AttendanceActivity.this,
                        android.R.layout.simple_spinner_item,
                        siteList
                );
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerAttendanceSite.setAdapter(spinnerAdapter);

                spinnerAttendanceSite.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedSite = siteList.get(position);
                        tvHeaderSiteName.setText(selectedSite.getSiteName());
                        loadAttendanceForSelectedSiteAndDate();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
            });
        });
    }

    private void loadAttendanceForSelectedSiteAndDate() {
        if (selectedSite == null) return;

        final int siteId = selectedSite.getId();
        final String dateStr = dateFormat.format(selectedCalendar.getTime());

        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Worker> workers = db.workerDao().getActiveWorkersBySite(siteId);
            if (workers.isEmpty()) {
                // If site has no workers assigned yet, show all active workers as fallback
                workers = db.workerDao().getActiveWorkers();
            }

            List<Attendance> existingAttendance = db.attendanceDao().getAttendanceForDateAndSite(dateStr, siteId);
            Map<Integer, Attendance> attMap = new HashMap<>();
            for (Attendance a : existingAttendance) {
                attMap.put(a.getWorkerId(), a);
            }

            // Get advances for this date/site to display deductions
            List<Advance> advances = db.advanceDao().getAdvancesBetweenDates(dateStr, dateStr);
            Map<Integer, Double> advMap = new HashMap<>();
            for (Advance adv : advances) {
                double current = advMap.containsKey(adv.getWorkerId()) ? advMap.get(adv.getWorkerId()) : 0.0;
                advMap.put(adv.getWorkerId(), current + adv.getAmount());
            }

            List<WorkerAttendanceItem> items = new ArrayList<>();
            long now = System.currentTimeMillis();

            for (Worker w : workers) {
                Attendance att = attMap.get(w.getId());
                if (att == null) {
                    // Create default record: Present (P, P), 0 OT for speed
                    att = new Attendance(
                            w.getId(),
                            siteId,
                            dateStr,
                            "P",
                            "P",
                            "P",
                            1.0,
                            0.0,
                            w.getDailyRate(),
                            0.0,
                            w.getDailyRate(),
                            "",
                            now
                    );
                }
                double advDeduction = advMap.containsKey(w.getId()) ? advMap.get(w.getId()) : 0.0;
                items.add(new WorkerAttendanceItem(w, att, advDeduction));
            }

            final List<WorkerAttendanceItem> finalItems = items;
            mainHandler.post(() -> {
                adapter.setItems(finalItems);
                tvWorkerCountHeader.setText(String.format(Locale.US, "Site Workers (%d)", finalItems.size()));
                onAttendanceChanged();
            });
        });
    }

    @Override
    public void onAttendanceChanged() {
        List<WorkerAttendanceItem> items = adapter.getItems();
        int presentCount = 0;
        int halfDayCount = 0;
        int absentCount = 0;
        double totalOtHours = 0.0;
        double totalWages = 0.0;

        for (WorkerAttendanceItem item : items) {
            Attendance att = item.getAttendance();
            if (att != null) {
                if ("P".equalsIgnoreCase(att.getOverallStatus())) {
                    presentCount++;
                } else if ("HD".equalsIgnoreCase(att.getOverallStatus())) {
                    halfDayCount++;
                } else {
                    absentCount++;
                }
                totalOtHours += att.getOtHours();
                totalWages += att.getTotalEarned();
            }
        }

        tvBottomPresentCount.setText(String.format(Locale.US, "P: %d", presentCount));
        tvBottomHdCount.setText(String.format(Locale.US, "HD: %d", halfDayCount));
        tvBottomAbsentCount.setText(String.format(Locale.US, "A: %d", absentCount));
        tvBottomOtCount.setText(String.format(Locale.US, "OT: %.1fh", totalOtHours));
        tvBottomTotalWages.setText(String.format(Locale.US, "%,.0f Ks", totalWages));
    }

    private void saveAttendanceRecords() {
        List<WorkerAttendanceItem> items = adapter.getItems();
        if (items.isEmpty()) {
            Snackbar.make(rvAttendanceList, "No workers to save attendance for", Snackbar.LENGTH_SHORT).show();
            return;
        }

        List<Attendance> toSave = new ArrayList<>();
        long now = System.currentTimeMillis();
        for (WorkerAttendanceItem item : items) {
            item.recalculate();
            Attendance att = item.getAttendance();
            att.setUpdatedAt(now);
            toSave.add(att);
        }

        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.attendanceDao().insertAll(toSave);
            mainHandler.post(() -> {
                Snackbar.make(
                        rvAttendanceList,
                        String.format(Locale.US, "Attendance saved for %d workers successfully!", toSave.size()),
                        Snackbar.LENGTH_LONG
                ).show();
            });
        });
    }
}
