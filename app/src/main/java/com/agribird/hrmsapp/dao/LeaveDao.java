package com.agribird.hrmsapp.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.agribird.hrmsapp.Model.LeaveBalance;
import com.agribird.hrmsapp.Model.LeaveEntity;
import com.agribird.hrmsapp.Model.LeaveTypeCount;
import com.agribird.hrmsapp.Model.LeaveWithEmployee;

import java.util.List;

@Dao
public interface LeaveDao {

    @Insert
    void applyLeave(LeaveEntity leave);

    @Query("SELECT * FROM LeaveEntity")
    List<LeaveEntity> getAllLeaves();

    @Query("SELECT * FROM LeaveEntity WHERE employeeId=:employeeId")
    List<LeaveEntity> getEmployeeLeaves(String employeeId);

    @Query("SELECT COUNT(*) FROM LeaveEntity WHERE employeeId = :empId AND status = 'Pending'")
    int getPendingLeaveCount(String empId);

    @Query("SELECT COUNT(*) FROM LeaveEntity WHERE employeeId = :empId")
    int getTotalLeaveCount(String empId);

    @Query("SELECT COUNT(*) FROM LeaveEntity WHERE status = 'Pending'")
    LiveData<Integer> getPendingLeavesCount();

    @Query("SELECT COUNT(*) FROM LeaveEntity WHERE employeeId=:employeeId AND status='Approved'")
    int getApprovedLeaveCount(String employeeId);

    @Query("UPDATE LeaveEntity SET status = :newStatus WHERE leaveId = :lId")
    void updateLeaveStatus(int lId, String newStatus);

    @Transaction
    @Query("SELECT * FROM LeaveEntity ORDER BY CASE WHEN status = 'Pending' THEN 1 ELSE 2 END, leaveId DESC")
    List<LeaveWithEmployee> getAllLeaveRequests();

    // =============== NEW METHODS FOR DASHBOARD ===============

    /**
     * Get pending leave count as LiveData
     */
    @Query("SELECT COUNT(*) FROM LeaveEntity WHERE status = 'Pending'")
    LiveData<Integer> getPendingLeaveCountLiveData();

    /**
     * Get employee's pending leave count
     */
    @Query("SELECT COUNT(*) FROM LeaveEntity WHERE employeeId = :empId AND status = 'Pending'")
    LiveData<Integer> getEmployeePendingLeaveCount(String empId);

    /**
     * Get approved leave count with LiveData
     */
    @Query("SELECT COUNT(*) FROM LeaveEntity WHERE employeeId=:employeeId AND status='Approved'")
    LiveData<Integer> getApprovedLeaveCountLiveData(String employeeId);

    /**
     * Get leave balance for employee
     * Note: This assumes you have leave_balance table
     */
//    @Query("SELECT * FROM leaveBalance WHERE employeeId = :empId")
//    LiveData<List<LeaveBalance>> getEmployeeLeaveBalance(String empId);

    /**
     * Get total leave days taken by employee (approved leaves)
     * FIXED: Using substr for date format "dd MMM yyyy"
     */
    @Query("SELECT COUNT(*) as totalDays " +
            "FROM LeaveEntity WHERE employeeId = :empId AND status = 'Approved' " +
            "AND substr(startDate, -4) = :year")
    LiveData<Integer> getTotalLeaveDaysInYear(String empId, String year);

    /**
     * Get leave count by type for employee
     */
    @Query("SELECT leaveType, COUNT(*) as count FROM LeaveEntity " +
            "WHERE employeeId = :empId AND status = 'Approved' GROUP BY leaveType")
    LiveData<List<LeaveTypeCount>> getLeaveCountByType(String empId);
}
//package com.agribird.hrmsapp.dao;
//
//import androidx.lifecycle.LiveData;
//import androidx.room.Dao;
//import androidx.room.Insert;
//import androidx.room.Query;
//import androidx.room.Transaction;
//
//import com.agribird.hrmsapp.Model.LeaveEntity;
//import com.agribird.hrmsapp.Model.LeaveWithEmployee;
//
//import java.util.List;
//
//@Dao
//public interface LeaveDao {
//
//    @Insert
//    void applyLeave(LeaveEntity leave);
//    @Query("SELECT * FROM LeaveEntity")
//    List<LeaveEntity> getAllLeaves();
//    @Query("SELECT * FROM LeaveEntity WHERE employeeId=:employeeId")
//    List<LeaveEntity> getEmployeeLeaves(String employeeId);
//
//    @Query("SELECT COUNT(*) FROM LeaveEntity WHERE employeeId = :empId AND status = 'Pending'")
//    int getPendingLeaveCount(String empId);
//
//    @Query("SELECT COUNT(*) FROM LeaveEntity WHERE employeeId = :empId")
//    int getTotalLeaveCount(String empId);
//
//    @Query("SELECT COUNT(*) FROM LeaveEntity WHERE status = 'Pending'")
//    LiveData<Integer> getPendingLeavesCount();
//
//    @Query("SELECT COUNT(*) FROM LeaveEntity WHERE employeeId=:employeeId AND status='Approved'")
//    int getApprovedLeaveCount(String employeeId);
//
//    @Query("UPDATE LeaveEntity SET status = :newStatus WHERE leaveId = :lId")
//    void updateLeaveStatus(int lId, String newStatus);
//
//    @Transaction
//    @Query("SELECT * FROM LeaveEntity ORDER BY CASE WHEN status = 'Pending' THEN 1 ELSE 2 END, leaveId DESC")
//    List<LeaveWithEmployee> getAllLeaveRequests();
//
//
//}
