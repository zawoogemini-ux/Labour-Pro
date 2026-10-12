package com.example.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sites")
public class Site {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String siteName;
    private String location;
    private String supervisorName;
    private long createdAt;
    private boolean isActive;

    public Site(String siteName, String location, String supervisorName, long createdAt, boolean isActive) {
        this.siteName = siteName;
        this.location = location;
        this.supervisorName = supervisorName;
        this.createdAt = createdAt;
        this.isActive = isActive;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSupervisorName() {
        return supervisorName;
    }

    public void setSupervisorName(String supervisorName) {
        this.supervisorName = supervisorName;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return siteName;
    }
}
