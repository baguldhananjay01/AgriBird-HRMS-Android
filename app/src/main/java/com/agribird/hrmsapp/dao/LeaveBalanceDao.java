//package com.agribird.hrmsapp.dao;
//
//import androidx.lifecycle.LiveData;
//import androidx.room.Dao;
//import androidx.room.Insert;
//import androidx.room.Query;
//import androidx.room.Update;
//
//import com.agribird.hrmsapp.Model.LeaveBalance;
//
//import java.util.List;
//
//@Dao
//public interface LeaveBalanceDao {
//
//    @Insert
//    void insertLeaveBalance(LeaveBalance leaveBalance);
//
//    @Update
//    void updateLeaveBalance(LeaveBalance leaveBalance);
//
//    @Query("SELECT * FROM leaveBalance WHERE employeeId = :empId")
//    LiveData<List<LeaveBalance>> getLeaveBalance(String empId);
//
//    @Query("SELECT * FROM leaveBalance WHERE employeeId = :empId AND leaveType = :leaveType")
//    LeaveBalance getLeaveBalanceByType(String empId, String leaveType);
//
//    @Query("SELECT * FROM leaveBalance WHERE employeeId = :empId AND leaveType = :leaveType")
//    LiveData<LeaveBalance> getLeaveBalanceByTypeLiveData(String empId, String leaveType);
//
//    @Query("SELECT * FROM leaveBalance WHERE employeeId = :empId AND year = :year")
//    List<LeaveBalance> getLeaveBalanceForYear(String empId, String year);
//
//    @Query("UPDATE leaveBalance SET usedDays = usedDays + :days, remainingDays = remainingDays - :days " +
//            "WHERE employeeId = :empId AND leaveType = :leaveType")
//    void updateUsedLeaveDays(String empId, String leaveType, int days);
//
//    @Query("DELETE FROM leaveBalance WHERE employeeId = :empId")
//    void deleteLeaveBalance(String empId);
//
//    @Query("SELECT SUM(remainingDays) FROM leaveBalance WHERE employeeId = :empId")
//    LiveData<Integer> getTotalRemainingLeave(String empId);
//}