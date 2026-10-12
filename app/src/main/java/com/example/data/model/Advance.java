package com.example.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "advances")
public class Advance {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int workerId;
    private int siteId;
    private String date; // YYYY-MM-DD
    private double amount;
    private String supervisorName;
    private String remark;
    private long createdAt;

    public Advance(int workerId, int siteId, String date, double amount,
                   String supervisorName, String remark, long createdAt) {
        this.workerId = workerId;
        this.siteId = siteId;
        this.date = date;
        this.amount = amount;
        this.supervisorName = supervisorName;
        this.remark = remark;
        this.createdAt = createdAt;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getSupervisorName() {
        return supervisorName;
    }

    public void setSupervisorName(String supervisorName) {
        this.supervisorName = supervisorName;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
