package com.agribird.hrmsapp.Model;

public class LeaveBalance {

    private Long balanceId;
    private EmployeeApiModel employee;
    private String leaveType;
    private int allocatedDays;
    private int usedDays;
    private int remainingDays;
    private String year;

    public LeaveBalance() {
    }

    public Long getBalanceId() {
        return balanceId;
    }

    public void setBalanceId(Long balanceId) {
        this.balanceId = balanceId;
    }

    public EmployeeApiModel getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeApiModel employee) {
        this.employee = employee;
    }

    public String getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(String leaveType) {
        this.leaveType = leaveType;
    }

    public int getAllocatedDays() {
        return allocatedDays;
    }

    public void setAllocatedDays(int allocatedDays) {
        this.allocatedDays = allocatedDays;
    }

    public int getUsedDays() {
        return usedDays;
    }

    public void setUsedDays(int usedDays) {
        this.usedDays = usedDays;
    }

    public int getRemainingDays() {
        return remainingDays;
    }

    public void setRemainingDays(int remainingDays) {
        this.remainingDays = remainingDays;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }
}