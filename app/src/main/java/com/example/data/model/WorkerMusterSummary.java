package com.example.data.model;

public class WorkerMusterSummary {
    private Worker worker;
    private int presentDays;
    private int halfDays;
    private int absentDays;
    private double totalOtHours;
    private double grossWage;
    private double advanceDeductions;

    public WorkerMusterSummary(Worker worker) {
        this.worker = worker;
        this.presentDays = 0;
        this.halfDays = 0;
        this.absentDays = 0;
        this.totalOtHours = 0.0;
        this.grossWage = 0.0;
        this.advanceDeductions = 0.0;
    }

    public Worker getWorker() {
        return worker;
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
    }

    public int getPresentDays() {
        return presentDays;
    }

    public void setPresentDays(int presentDays) {
        this.presentDays = presentDays;
    }

    public int getHalfDays() {
        return halfDays;
    }

    public void setHalfDays(int halfDays) {
        this.halfDays = halfDays;
    }

    public int getAbsentDays() {
        return absentDays;
    }

    public void setAbsentDays(int absentDays) {
        this.absentDays = absentDays;
    }

    public double getTotalOtHours() {
        return totalOtHours;
    }

    public void setTotalOtHours(double totalOtHours) {
        this.totalOtHours = totalOtHours;
    }

    public double getGrossWage() {
        return grossWage;
    }

    public void setGrossWage(double grossWage) {
        this.grossWage = grossWage;
    }

    public double getAdvanceDeductions() {
        return advanceDeductions;
    }

    public void setAdvanceDeductions(double advanceDeductions) {
        this.advanceDeductions = advanceDeductions;
    }

    public double getNetPayable() {
        return Math.max(0.0, grossWage - advanceDeductions);
    }

    public double getTotalManDays() {
        return presentDays + (halfDays * 0.5);
    }
}
