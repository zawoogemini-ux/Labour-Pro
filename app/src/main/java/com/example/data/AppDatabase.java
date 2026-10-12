package com.example.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.data.dao.AdvanceDao;
import com.example.data.dao.AttendanceDao;
import com.example.data.dao.SiteDao;
import com.example.data.dao.WorkerDao;
import com.example.data.model.Advance;
import com.example.data.model.Attendance;
import com.example.data.model.Site;
import com.example.data.model.Worker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
    entities = {Site.class, Worker.class, Attendance.class, Advance.class},
    version = 1,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public abstract SiteDao siteDao();
    public abstract WorkerDao workerDao();
    public abstract AttendanceDao attendanceDao();
    public abstract AdvanceDao advanceDao();

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "construction_labour_pro_mmk.db"
                    )
                    .addCallback(new Callback() {
                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase db) {
                            super.onCreate(db);
                            databaseWriteExecutor.execute(() -> seedInitialData(INSTANCE));
                        }
                    })
                    .build();
                }
            }
        }
        return INSTANCE;
    }

    private static void seedInitialData(AppDatabase db) {
        long now = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String today = sdf.format(new Date(now));

        // Seed Sites
        Site site1 = new Site("B2 WCR Highway Project", "Outer Ring Road Sector-8", "U Kyaw Myint", now, true);
        Site site2 = new Site("Village-2 Residential Complex", "Phase 3 Green Valley", "U Aung San", now, true);
        Site site3 = new Site("Metro Station Pier-14", "Civil Lines Crossing", "U Myo Min", now, true);

        long s1Id = db.siteDao().insert(site1);
        long s2Id = db.siteDao().insert(site2);
        long s3Id = db.siteDao().insert(site3);

        int site1Id = (int) s1Id;
        int site2Id = (int) s2Id;

        // Seed Workers with Myanmar Kyats (Ks) rates
        Worker w1 = new Worker("W-101", "Ko Thant Zin", "Male", "Mason", 25000.0, 3500.0, "09765432101", site1Id, true, now);
        Worker w2 = new Worker("W-102", "U Zaw Lin", "Male", "Carpenter", 28000.0, 4000.0, "09765432102", site1Id, true, now);
        Worker w3 = new Worker("W-103", "Ko Aung Ko", "Male", "Helper", 15000.0, 2000.0, "09765432103", site1Id, true, now);
        Worker w4 = new Worker("W-104", "Ma Aye Thida", "Female", "Helper", 15000.0, 2000.0, "09765432104", site1Id, true, now);
        Worker w5 = new Worker("W-105", "Ko Kyaw Swar", "Male", "Bar Bender", 26000.0, 3500.0, "09765432105", site1Id, true, now);
        Worker w6 = new Worker("W-106", "Daw Mya Mya", "Female", "Coolie", 14000.0, 1800.0, "09765432106", site1Id, true, now);
        Worker w7 = new Worker("W-107", "Ko Tun Lin", "Male", "Mason", 25000.0, 3500.0, "09765432107", site2Id, true, now);
        Worker w8 = new Worker("W-108", "U Hlaing Win", "Male", "Electrician", 30000.0, 4500.0, "09765432108", site2Id, true, now);

        long w1Id = db.workerDao().insert(w1);
        long w2Id = db.workerDao().insert(w2);
        long w3Id = db.workerDao().insert(w3);
        long w4Id = db.workerDao().insert(w4);
        long w5Id = db.workerDao().insert(w5);
        long w6Id = db.workerDao().insert(w6);
        db.workerDao().insert(w7);
        db.workerDao().insert(w8);

        // Seed Sample Attendance for Today on Site 1
        // W1: Present (P, P), 2 hr OT -> 25000 + 7000 = 32000 Ks
        Attendance a1 = new Attendance((int) w1Id, site1Id, today, "P", "P", "P", 1.0, 2.0, 25000.0, 7000.0, 32000.0, "Foundation beam", now);
        // W2: Present (P, P), 0 OT -> 28000 Ks
        Attendance a2 = new Attendance((int) w2Id, site1Id, today, "P", "P", "P", 1.0, 0.0, 28000.0, 0.0, 28000.0, "Shuttering work", now);
        // W3: Half-Day (P, A) -> 7500 Ks
        Attendance a3 = new Attendance((int) w3Id, site1Id, today, "P", "A", "HD", 0.5, 0.0, 7500.0, 0.0, 7500.0, "Left post lunch", now);
        // W4: Present (P, P), 1 hr OT -> 15000 + 2000 = 17000 Ks
        Attendance a4 = new Attendance((int) w4Id, site1Id, today, "P", "P", "P", 1.0, 1.0, 15000.0, 2000.0, 17000.0, "Curing concrete", now);
        // W5: Absent (A, A) -> 0 Ks
        Attendance a5 = new Attendance((int) w5Id, site1Id, today, "A", "A", "A", 0.0, 0.0, 0.0, 0.0, 0.0, "Unwell", now);
        // W6: Present (P, P), 0 OT -> 14000 Ks
        Attendance a6 = new Attendance((int) w6Id, site1Id, today, "P", "P", "P", 1.0, 0.0, 14000.0, 0.0, 14000.0, "Material handling", now);

        db.attendanceDao().insertOrUpdate(a1);
        db.attendanceDao().insertOrUpdate(a2);
        db.attendanceDao().insertOrUpdate(a3);
        db.attendanceDao().insertOrUpdate(a4);
        db.attendanceDao().insertOrUpdate(a5);
        db.attendanceDao().insertOrUpdate(a6);

        // Seed Sample Advances in Myanmar Kyats
        Advance adv1 = new Advance((int) w1Id, site1Id, today, 10000.0, "U Kyaw Myint", "Weekly food ration", now);
        Advance adv2 = new Advance((int) w3Id, site1Id, today, 5000.0, "U Kyaw Myint", "Medicine advance", now);
        db.advanceDao().insert(adv1);
        db.advanceDao().insert(adv2);
    }
}
