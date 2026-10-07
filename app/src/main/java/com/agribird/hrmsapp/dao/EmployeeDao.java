package com.agribird.hrmsapp.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.agribird.hrmsapp.Model.Employee;

import java.util.List;

@Dao
public interface EmployeeDao {

    @Insert
    void addEmployee(Employee emp);

    @Update
    void updateEmployee(Employee emp);

    @Delete
    void deleteEmployee(Employee emp);

    @Query("SELECT * FROM Employee WHERE id = :id")
    Employee getEmployeeById(String id);

    @Query("SELECT * FROM Employee")
    List<Employee> getAllEmployee();

    @Query("SELECT * FROM employee WHERE email = :email AND password = :password LIMIT 1")
    Employee loginEmployee(String email, String password);

    @Query("SELECT COUNT(*) FROM Employee")
    LiveData<Integer> getTotalEmployeesCount();

    // =============== NEW METHODS FOR DASHBOARD ===============

    /**
     * Get employees who haven't checked in today (for admin dashboard)
     * Note: This requires attendance_table join
     */
    @Query("SELECT e.* FROM Employee e LEFT JOIN attendance_table a ON e.id = a.employeeId AND a.date = :todayDate WHERE a.attendanceId IS NULL")
    LiveData<List<Employee>> getEmployeesNotCheckedIn(String todayDate);

    /**
     * Get employee by ID with LiveData (for profile updates)
     */
    @Query("SELECT * FROM Employee WHERE id = :empId LIMIT 1")
    LiveData<Employee> getEmployeeByIdLiveData(String empId);

    /**
     * Get total employees count (without LiveData)
     */
    @Query("SELECT COUNT(*) FROM Employee")
    int getTotalEmployeesCountSync();

    /**
     * Get employees by department (for department-wise reports)
     */
    @Query("SELECT * FROM Employee WHERE department = :department")
    List<Employee> getEmployeesByDepartment(String department);

    /**
     * Get all employees with LiveData (for real-time updates)
     */
    @Query("SELECT * FROM Employee")
    LiveData<List<Employee>> getAllEmployeesLiveData();

    /**
     * Search employees by name or ID (for search functionality)
     */
    @Query("SELECT * FROM Employee WHERE name LIKE '%' || :searchQuery || '%' OR id LIKE '%' || :searchQuery || '%'")
    List<Employee> searchEmployees(String searchQuery);

    /**
     * Get active employees count (excluding terminated/resigned)
     */
    @Query("SELECT COUNT(*) FROM Employee WHERE status = 'Active' OR status = 'Working'")
    LiveData<Integer> getActiveEmployeesCount();

    /**
     * Get employees who are on leave today (join with attendance)
     */
    @Query("SELECT e.* FROM Employee e INNER JOIN attendance_table a ON e.id = a.employeeId WHERE a.date = :todayDate AND a.status = 'Leave'")
    LiveData<List<Employee>> getEmployeesOnLeaveToday(String todayDate);
    @Query("UPDATE employee SET name = :newName, email = :newEmail, phone = :newPhone, emergencyPhone = :newEmergencyPhone, address = :newAddress WHERE id = :employeeId")
    void updateEmployeeProfile(String employeeId, String newName, String newEmail, String newPhone, String newEmergencyPhone, String newAddress);
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

//import com.agribird.hrmsapp.Model.Employee;
//
//import java.util.List;
//@Dao
//public interface EmployeeDao {
//
//    @Insert
//    void addEmployee(Employee emp);
//    @Update
//    void updateEmployee(Employee emp);
//    @Delete
//    void deleteEmployee(Employee emp);
//    @Query("SELECT * FROM Employee WHERE id = :id")
//     Employee getEmployeeById(String id);
//    @Query("SELECT * FROM Employee")
//    List<Employee> getAllEmployee();
//
//    @Query("SELECT * FROM employee WHERE email = :email AND password = :password LIMIT 1")
//    Employee loginEmployee(String email, String password);
//
//    @Query("SELECT COUNT(*) FROM Employee")
//    LiveData<Integer> getTotalEmployeesCount();
//
//}
