package com.agribird.hrmsapp.Model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "break_records")
public class BreakRecord {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String employeeId;
    private String date;          // dd MMM yyyy
    private String type;          // LUNCH, TEA, BIO
    private String startTime;     // hh:mm a
    private String endTime;       // hh:mm a (null if break is active)
    private long durationMinutes; // total minutes (after end)

    public BreakRecord() {
    }

    public BreakRecord(String employeeId, String date, String type, String startTime, String endTime, long durationMinutes) {
        this.employeeId = employeeId;
        this.date = date;
        this.type = type;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationMinutes = durationMinutes;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public long getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(long durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
}