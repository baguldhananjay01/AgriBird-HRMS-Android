package com.agribird.hrmsapp.Model;

public class AttendanceSummary {

    private int presentCount;
    private int absentCount;
    private int leaveCount;
    private int halfDayCount;

    public AttendanceSummary() {}

    public AttendanceSummary(int presentCount, int absentCount, int leaveCount, int halfDayCount) {
        this.presentCount = presentCount;
        this.absentCount = absentCount;
        this.leaveCount = leaveCount;
        this.halfDayCount = halfDayCount;
    }


    public int getPresentCount() { return presentCount; }
    public void setPresentCount(int presentCount) { this.presentCount = presentCount; }

    public int getAbsentCount() { return absentCount; }
    public void setAbsentCount(int absentCount) { this.absentCount = absentCount; }

    public int getLeaveCount() { return leaveCount; }
    public void setLeaveCount(int leaveCount) { this.leaveCount = leaveCount; }

    public int getHalfDayCount() { return halfDayCount; }
    public void setHalfDayCount(int halfDayCount) { this.halfDayCount = halfDayCount; }

    public int getTotalDays() {
        return presentCount + absentCount + leaveCount + halfDayCount;
    }
}