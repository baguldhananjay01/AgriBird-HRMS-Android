package com.agribird.hrmsapp.Model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class LeaveEntity {
    @PrimaryKey(autoGenerate = true)
    private int leaveId;
    private String employeeId;
    private String leaveType;
    private String startDate;
    private String endDate;
    private String reason;
    private String status="Pending";

    public LeaveEntity(String employeeId,String leaveType,String startDate,String endDate,String reason){
        this.employeeId=employeeId;
        this.leaveType=leaveType;
        this.startDate=startDate;
        this.endDate=endDate;
        this.reason=reason;
        this.status="Pending";
    }

    public int getLeaveId() {
        return leaveId;
    }

    public void setLeaveId(int leaveId) {
        this.leaveId = leaveId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(String leaveType) {
        this.leaveType = leaveType;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
