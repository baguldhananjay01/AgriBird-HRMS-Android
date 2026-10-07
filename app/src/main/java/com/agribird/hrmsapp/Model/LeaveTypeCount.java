package com.agribird.hrmsapp.Model;

public class LeaveTypeCount {
    private String leaveType;
    private int count;

    public LeaveTypeCount() {}

    public LeaveTypeCount(String leaveType, int count) {
        this.leaveType = leaveType;
        this.count = count;
    }

    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
}