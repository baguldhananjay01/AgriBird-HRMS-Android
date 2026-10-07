package com.agribird.hrmsapp.Model;

public class AttendanceStatusCount {
    private String status;
    private int count;

    public AttendanceStatusCount() {}

    public AttendanceStatusCount(String status, int count) {
        this.status = status;
        this.count = count;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
}