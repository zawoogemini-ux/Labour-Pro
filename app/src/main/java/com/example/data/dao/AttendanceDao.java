package com.example.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.data.model.Attendance;

import java.util.List;

@Dao
public interface AttendanceDao {
    @Query("SELECT * FROM attendance WHERE attendanceDate = :date AND siteId = :siteId")
    List<Attendance> getAttendanceForDateAndSite(String date, int siteId);

    @Query("SELECT * FROM attendance WHERE attendanceDate = :date")
    List<Attendance> getAttendanceForDate(String date);

    @Query("SELECT * FROM attendance WHERE workerId = :workerId AND attendanceDate = :date LIMIT 1")
    Attendance getAttendanceForWorkerAndDate(int workerId, String date);

    @Query("SELECT * FROM attendance WHERE attendanceDate BETWEEN :startDate AND :endDate ORDER BY attendanceDate ASC")
    List<Attendance> getAttendanceBetweenDates(String startDate, String endDate);

    @Query("SELECT * FROM attendance WHERE siteId = :siteId AND attendanceDate BETWEEN :startDate AND :endDate ORDER BY attendanceDate ASC")
    List<Attendance> getAttendanceForSiteBetweenDates(int siteId, String startDate, String endDate);

    @Query("SELECT * FROM attendance WHERE workerId = :workerId AND attendanceDate BETWEEN :startDate AND :endDate ORDER BY attendanceDate ASC")
    List<Attendance> getAttendanceForWorkerBetweenDates(int workerId, String startDate, String endDate);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertOrUpdate(Attendance attendance);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Attendance> attendances);

    @Update
    void update(Attendance attendance);

    @Delete
    void delete(Attendance attendance);
}
