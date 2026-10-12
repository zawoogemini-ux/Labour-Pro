package com.example.data.model;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "attendance",
    indices = {@Index(value = {"workerId", "attendanceDate"}, unique = true)}
)
public class Attendance {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int workerId;
    private int siteId;
    private String attendanceDate; // YYYY-MM-DD
    private String morningStatus;  // "P", "HD", "A"
    private String eveningStatus;  // "P", "HD", "A"
    private String overallStatus;  // "P", "HD", "A"
    private double dayFraction;    // 1.0, 0.5, 0.0
    private double otHours;        // e.g. 0.0, 1.5, 2.0
    private double dailyWage;      // based on dayFraction * dailyRate
    private double otWage;         // based on otHours * otRate
    private double totalEarned;    // dailyWage + otWage
    private String remark;
    private long updatedAt;

    public Attendance(int workerId, int siteId, String attendanceDate,
                      String morningStatus, String eveningStatus, String overallStatus,
                      double dayFraction, double otHours, double dailyWage,
                      double otWage, double totalEarned, String remark, long updatedAt) {
        this.workerId = workerId;
        this.siteId = siteId;
        this.attendanceDate = attendanceDate;
        this.morningStatus = morningStatus;
        this.eveningStatus = eveningStatus;
        this.overallStatus = overallStatus;
        this.dayFraction = dayFraction;
        this.otHours = otHours;
        this.dailyWage = dailyWage;
        this.otWage = otWage;
        this.totalEarned = totalEarned;
        this.remark = remark;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getWorkerId() {
        return workerId;
    }

    public void setWorkerId(int workerId) {
        this.workerId = workerId;
    }

    public int getSiteId() {
        return siteId;
    }

    public void setSiteId(int siteId) {
        this.siteId = siteId;
    }

    public String getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(String attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public String getMorningStatus() {
        return morningStatus;
    }

    public void setMorningStatus(String morningStatus) {
        this.morningStatus = morningStatus;
    }

    public String getEveningStatus() {
        return eveningStatus;
    }

    public void setEveningStatus(String eveningStatus) {
        this.eveningStatus = eveningStatus;
    }

    public String getOverallStatus() {
        return overallStatus;
    }

    public void setOverallStatus(String overallStatus) {
        this.overallStatus = overallStatus;
    }

    public double getDayFraction() {
        return dayFraction;
    }

    public void setDayFraction(double dayFraction) {
        this.dayFraction = dayFraction;
    }

    public double getOtHours() {
        return otHours;
    }

    public void setOtHours(double otHours) {
        this.otHours = otHours;
    }

    public double getDailyWage() {
        return dailyWage;
    }

    public void setDailyWage(double dailyWage) {
        this.dailyWage = dailyWage;
    }

    public double getOtWage() {
        return otWage;
    }

    public void setOtWage(double otWage) {
        this.otWage = otWage;
    }

    public double getTotalEarned() {
        return totalEarned;
    }

    public void setTotalEarned(double totalEarned) {
        this.totalEarned = totalEarned;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
