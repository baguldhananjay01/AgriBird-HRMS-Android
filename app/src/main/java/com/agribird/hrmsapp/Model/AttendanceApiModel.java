package com.agribird.hrmsapp.Model;

public class AttendanceApiModel {

    private long attendanceId;
    private String employeeId;
    private String employeeName;
    private String date;
    private String checkInTime;
    private String checkOutTime;
    private String status;
    private String selfiePath;
    private String totalHours;

    public long getAttendanceId() {
        return attendanceId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public String getDate() {
        return date;
    }

    public String getCheckInTime() {
        return checkInTime;
    }

    public String getCheckOutTime() {
        return checkOutTime;
    }

    public String getStatus() {
        return status;
    }

    public String getSelfiePath() {
        return selfiePath;
    }

    public String getTotalHours() {
        return totalHours;
    }
}