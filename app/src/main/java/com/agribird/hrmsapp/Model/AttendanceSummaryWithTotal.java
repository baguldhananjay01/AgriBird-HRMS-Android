package com.agribird.hrmsapp.Model;

public class AttendanceSummaryWithTotal extends AttendanceSummary {
    private int totalCount;

    public AttendanceSummaryWithTotal() {}

    public AttendanceSummaryWithTotal(int totalCount, int presentCount, int absentCount,
                                      int leaveCount, int halfDayCount) {
        super(presentCount, absentCount, leaveCount, halfDayCount);
        this.totalCount = totalCount;
    }

    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
}