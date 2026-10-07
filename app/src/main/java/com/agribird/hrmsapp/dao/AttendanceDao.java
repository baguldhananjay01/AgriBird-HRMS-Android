package com.agribird.hrmsapp.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.agribird.hrmsapp.Model.Attendance;
import com.agribird.hrmsapp.Model.AttendanceSummary;
import com.agribird.hrmsapp.Model.AttendanceSummaryWithTotal;
import com.agribird.hrmsapp.Model.AttendanceStatusCount;
import com.agribird.hrmsapp.Model.Employee;

import java.util.List;

@Dao
public interface AttendanceDao {

    @Insert
    void insertAttendance(Attendance attendance);

    @Update
    void updateAttendance(Attendance attendance);

    @Query("SELECT * FROM attendance_table WHERE employeeId = :empId AND date = :currentDate LIMIT 1")
    Attendance getAttendanceByDate(String empId, String currentDate);

    @Query("SELECT * FROM attendance_table WHERE employeeId = :empId ORDER BY attendanceId DESC")
    List<Attendance> getAllAttendance(String empId);

    @Query("SELECT * FROM attendance_table ORDER BY attendanceId DESC")
    List<Attendance> getAllAttendance();

    @Delete
    void deleteAttendance(Attendance attendance);

    // =============== EXISTING METHODS ===============
    @Query("SELECT COUNT(*) FROM attendance_table WHERE date = :todayDate AND (status = 'Present' OR status = 'Late' OR status = 'Completed')")
    LiveData<Integer> getPresentCountByDate(String todayDate);

    @Query("SELECT e.* FROM Employee e INNER JOIN attendance_table a ON e.id = a.employeeId WHERE a.date = :currentDate AND a.status = 'PRESENT'")
    List<Employee> getTodayPresentEmployees(String currentDate);

    // =============== NEW METHODS FOR DASHBOARD ===============

    /**
     * Get today's attendance for a specific employee with LiveData
     */
    @Query("SELECT * FROM attendance_table WHERE employeeId = :empId AND date = :todayDate LIMIT 1")
    LiveData<Attendance> getTodayAttendance(String empId, String todayDate);

    /**
     * Get count of employees on leave today
     */
    @Query("SELECT COUNT(*) FROM attendance_table WHERE date = :todayDate AND status = 'Leave'")
    LiveData<Integer> getOnLeaveCount(String todayDate);

    /**
     * Get count of pending regularization requests
     */
    @Query("SELECT COUNT(*) FROM attendance_table WHERE status = 'Pending' OR status = 'PENDING_REGULARIZATION'")
    LiveData<Integer> getPendingRegularizationCount();

    /**
     * Get monthly attendance summary for a specific employee
     * Note: SQLite date functions - adjust based on your date format
     */
    @Query("SELECT " +
            "COUNT(CASE WHEN status = 'Present' OR status = 'Late' OR status = 'Completed' THEN 1 END) as presentCount, " +
            "COUNT(CASE WHEN status = 'Absent' THEN 1 END) as absentCount, " +
            "COUNT(CASE WHEN status = 'Leave' THEN 1 END) as leaveCount, " +
            "COUNT(CASE WHEN status = 'Half Day' THEN 1 END) as halfDayCount " +
            "FROM attendance_table " +
            "WHERE employeeId = :empId AND date LIKE :monthPattern")
    LiveData<AttendanceSummary> getMonthlyAttendanceSummary(String empId, String monthPattern);

    /**
     * Get today's attendance summary for donut chart (Admin Dashboard)
     */
    @Query("SELECT " +
            "COUNT(*) as totalCount, " +
            "COUNT(CASE WHEN status = 'Present' OR status = 'Late' OR status = 'Completed' THEN 1 END) as presentCount, " +
            "COUNT(CASE WHEN status = 'Absent' THEN 1 END) as absentCount, " +
            "COUNT(CASE WHEN status = 'Leave' THEN 1 END) as leaveCount, " +
            "COUNT(CASE WHEN status = 'Half Day' THEN 1 END) as halfDayCount " +
            "FROM attendance_table " +
            "WHERE date = :todayDate")
    LiveData<AttendanceSummaryWithTotal> getAttendanceSummaryWithTotal(String todayDate);

    /**
     * Check if employee has checked in today
     */
    @Query("SELECT COUNT(*) > 0 FROM attendance_table WHERE employeeId = :empId AND date = :todayDate AND checkInTime IS NOT NULL")
    LiveData<Boolean> hasCheckedInToday(String empId, String todayDate);

    /**
     * Get attendance count for today by status (for pie chart/donut chart)
     */
    @Query("SELECT status, COUNT(*) as count FROM attendance_table WHERE date = :todayDate GROUP BY status")
    LiveData<List<AttendanceStatusCount>> getAttendanceStatusCounts(String todayDate);

    /**
     * Get today's present employees count with LiveData
     */
    @Query("SELECT COUNT(*) FROM attendance_table WHERE date = :todayDate AND status = 'Present'")
    LiveData<Integer> getTodayPresentCount(String todayDate);

    /**
     * Get today's absent employees count
     */
    @Query("SELECT COUNT(*) FROM attendance_table WHERE date = :todayDate AND status = 'Absent'")
    LiveData<Integer> getTodayAbsentCount(String todayDate);

    /**
     * Get today's half day employees count
     */
    @Query("SELECT COUNT(*) FROM attendance_table WHERE date = :todayDate AND status = 'Half Day'")
    LiveData<Integer> getTodayHalfDayCount(String todayDate);
}



//package com.agribird.hrmsapp.dao;
//
//import androidx.lifecycle.LiveData;
//import androidx.room.Dao;
//import androidx.room.Delete;
//import androidx.room.Insert;
//import androidx.room.Query;
//import androidx.room.Update;
//
//
//import com.agribird.hrmsapp.Model.Attendance;
//import com.agribird.hrmsapp.Model.Employee;
//
//import java.util.List;
//
//@Dao
//public interface AttendanceDao {
//
//    @Insert
//    void insertAttendance(Attendance attendance);
//
//    @Update
//    void updateAttendance(Attendance attendance);
//
//    @Query("SELECT * FROM attendance_table WHERE employeeId = :empId AND date = :currentDate LIMIT 1")
//    Attendance getAttendanceByDate(String empId, String currentDate);
//
//    @Query("SELECT * FROM attendance_table WHERE employeeId = :empId ORDER BY attendanceId DESC")
//    List<Attendance> getAllAttendance(String empId);
//
//    @Query("SELECT * FROM attendance_table ORDER BY attendanceId DESC")
//    List<Attendance> getAllAttendance();
//    @Delete
//    void deleteAttendance(Attendance attendance);
//    @Query("SELECT COUNT(*) FROM attendance_table WHERE date = :todayDate AND (status = 'Present' OR status = 'Late' OR status = 'Completed')")
//    LiveData<Integer> getPresentCountByDate(String todayDate);
//
//    @Query("SELECT e.* FROM Employee e INNER JOIN attendance_table a ON e.id = a.employeeId WHERE a.date = :currentDate AND a.status = 'PRESENT'")
//    List<Employee> getTodayPresentEmployees(String currentDate);
//}
