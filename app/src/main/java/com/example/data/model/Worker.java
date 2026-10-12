package com.example.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "workers")
public class Worker {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String workerCode;
    private String name;
    private String gender;
    private String workerType;
    private double dailyRate;
    private double otRate;
    private String phoneNumber;
    private int assignedSiteId;
    private boolean isActive;
    private long createdAt;

    public Worker(String workerCode, String name, String gender, String workerType,
                  double dailyRate, double otRate, String phoneNumber, int assignedSiteId,
                  boolean isActive, long createdAt) {
        this.workerCode = workerCode;
        this.name = name;
        this.gender = gender;
        this.workerType = workerType;
        this.dailyRate = dailyRate;
        this.otRate = otRate;
        this.phoneNumber = phoneNumber;
        this.assignedSiteId = assignedSiteId;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWorkerCode() {
        return workerCode;
    }

    public void setWorkerCode(String workerCode) {
        this.workerCode = workerCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getWorkerType() {
        return workerType;
    }

    public void setWorkerType(String workerType) {
        this.workerType = workerType;
    }

    public double getDailyRate() {
        return dailyRate;
    }

    public void setDailyRate(double dailyRate) {
        this.dailyRate = dailyRate;
    }

    public double getOtRate() {
        return otRate;
    }

    public void setOtRate(double otRate) {
        this.otRate = otRate;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getAssignedSiteId() {
        return assignedSiteId;
    }

    public void setAssignedSiteId(int assignedSiteId) {
        this.assignedSiteId = assignedSiteId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
