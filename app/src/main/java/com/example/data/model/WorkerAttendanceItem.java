package com.example.data.model;

public class WorkerAttendanceItem {
    private Worker worker;
    private Attendance attendance;
    private double advanceDeduction;

    public WorkerAttendanceItem(Worker worker, Attendance attendance, double advanceDeduction) {
        this.worker = worker;
        this.attendance = attendance;
        this.advanceDeduction = advanceDeduction;
        recalculate();
    }

    public Worker getWorker() {
        return worker;
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
        recalculate();
    }

    public Attendance getAttendance() {
        return attendance;
    }

    public void setAttendance(Attendance attendance) {
        this.attendance = attendance;
        recalculate();
    }

    public double getAdvanceDeduction() {
        return advanceDeduction;
    }

    public void setAdvanceDeduction(double advanceDeduction) {
        this.advanceDeduction = advanceDeduction;
    }

    public void recalculate() {
        if (worker == null || attendance == null) return;

        String m = attendance.getMorningStatus();
        String e = attendance.getEveningStatus();
        if (m == null) m = "P";
        if (e == null) e = "P";

        double fraction = 0.0;
        String overall = "A";

        if ("P".equals(m) && "P".equals(e)) {
            fraction = 1.0;
            overall = "P";
        } else if ("A".equals(m) && "A".equals(e)) {
            fraction = 0.0;
            overall = "A";
        } else {
            // Half day cases: P+A, A+P, HD+anything, anything+HD
            fraction = 0.5;
            overall = "HD";
        }

        double dailyWage = worker.getDailyRate() * fraction;
        double otHours = Math.max(0.0, attendance.getOtHours());
        double otWage = otHours * worker.getOtRate();
        double total = dailyWage + otWage;

        attendance.setDayFraction(fraction);
        attendance.setOverallStatus(overall);
        attendance.setDailyWage(dailyWage);
        attendance.setOtWage(otWage);
        attendance.setTotalEarned(total);
    }

    public double getNetBalance() {
        if (attendance == null) return 0.0;
        return attendance.getTotalEarned() - advanceDeduction;
    }
}
