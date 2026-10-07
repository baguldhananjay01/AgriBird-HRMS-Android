package com.agribird.hrmsapp.Model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "attendance_table",
        foreignKeys = @ForeignKey(
                entity = Employee.class,
                parentColumns = "id",
                childColumns = "employeeId",
                onDelete = ForeignKey.CASCADE
        )
)
public class Attendance {
    @PrimaryKey(autoGenerate = true)
    private int attendanceId;
    private String employeeId;
    private String employeeName;
    private String date;
    private String checkInTime;
    private String checkOutTime;
    private String status;
    private String selfiePath;
    private String totalHours;

    // ===== MAIN CONSTRUCTOR (Room हा वापरेल) =====
    public Attendance(String employeeId, String employeeName, String date,
                      String checkInTime, String checkOutTime, String status,
                      String selfiePath, String totalHours) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.date = date;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.status = status;
        this.selfiePath = selfiePath;
        this.totalHours = totalHours;
    }

    // ===== SECONDARY CONSTRUCTOR (Room हा ignore करेल) =====
    @Ignore  // <--- हे IMPORTANT आहे!
    public Attendance(String employeeId, String employeeName, String date,
                      String checkInTime, String checkOutTime, String status,
                      String selfiePath) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.date = date;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.status = status;
        this.selfiePath = selfiePath;
        this.totalHours = "00h 00m";
    }

    // ===== GETTERS AND SETTERS =====
    public int getAttendanceId() { return attendanceId; }
    public void setAttendanceId(int attendanceId) { this.attendanceId = attendanceId; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getCheckInTime() { return checkInTime; }
    public void setCheckInTime(String checkInTime) { this.checkInTime = checkInTime; }

    public String getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(String checkOutTime) { this.checkOutTime = checkOutTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSelfiePath() { return selfiePath; }
    public void setSelfiePath(String selfiePath) { this.selfiePath = selfiePath; }

    public String getTotalHours() { return totalHours; }
    public void setTotalHours(String totalHours) { this.totalHours = totalHours; }
}

//package com.agribird.hrmsapp.Model;
//
//import androidx.room.Entity;
//import androidx.room.ForeignKey;
//import androidx.room.PrimaryKey;
//
//@Entity(
//        tableName = "attendance_table",
//        foreignKeys = @ForeignKey(
//                entity = Employee.class,
//                parentColumns = "id",
//                childColumns = "employeeId",
//                onDelete = ForeignKey.CASCADE
//        )
//)
//public class Attendance {
//    @PrimaryKey(autoGenerate = true)
//    private int attendanceId;
//    private String employeeId;
//    private String employeeName;
//    private String date;
//    private String checkInTime;
//    private String checkOutTime;
//    private String status;
//    private String selfiePath;
//
//    public Attendance(String employeeId,String employeeName,String date,String checkInTime,String checkOutTime,String status,String selfiePath){
//        this.employeeId=employeeId;
//        this.employeeName=employeeName;
//        this.date=date;
//        this.checkInTime=checkInTime;
//        this.checkOutTime=checkOutTime;
//        this.status=status;
//        this.selfiePath=selfiePath;
//    }
//
//    public int getAttendanceId() {
//        return attendanceId;
//    }
//
//    public void setAttendanceId(int attendanceId) {
//        this.attendanceId = attendanceId;
//    }
//
//    public String getEmployeeId() {
//        return employeeId;
//    }
//
//    public void setEmployeeId(String employeeId) {
//        this.employeeId = employeeId;
//    }
//
//    public String getEmployeeName() {
//        return employeeName;
//    }
//
//    public void setEmployeeName(String employeeName)
//    {
//        this.employeeName = employeeName;
//    }
//
//    public String getDate() {
//        return date;
//    }
//
//    public void setDate(String date) {
//        this.date = date;
//    }
//
//    public String getCheckInTime() {
//        return checkInTime;
//    }
//
//    public void setCheckInTime(String checkInTime) {
//        this.checkInTime = checkInTime;
//    }
//
//    public String getCheckOutTime() {
//        return checkOutTime;
//    }
//
//    public void setCheckOutTime(String checkOutTime) {
//        this.checkOutTime = checkOutTime;
//    }
//
//    public String getStatus() {
//        return status;
//    }
//
//    public void setStatus(String status) {
//        this.status = status;
//    }
//
//    public String getSelfiePath() {
//        return selfiePath;
//    }
//
//    public void setSelfiePath(String selfiePath) {
//        this.selfiePath = selfiePath;
//    }
//}
