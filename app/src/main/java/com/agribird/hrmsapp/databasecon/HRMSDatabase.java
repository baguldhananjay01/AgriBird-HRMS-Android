package com.agribird.hrmsapp.databasecon;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.agribird.hrmsapp.Model.Attendance;
import com.agribird.hrmsapp.Model.BreakRecord;
import com.agribird.hrmsapp.Model.Employee;
import com.agribird.hrmsapp.Model.LeaveEntity;
import com.agribird.hrmsapp.Model.LeaveBalance;
import com.agribird.hrmsapp.dao.AttendanceDao;
import com.agribird.hrmsapp.dao.BreakDao;
import com.agribird.hrmsapp.dao.EmployeeDao;
import com.agribird.hrmsapp.dao.LeaveDao;
//import com.agribird.hrmsapp.dao.LeaveBalanceDao;

@Database(
        entities = {
                Employee.class,
                Attendance.class,
                LeaveEntity.class,
                BreakRecord.class
        },
        version = 10
)
public abstract class HRMSDatabase extends RoomDatabase {

    private static volatile HRMSDatabase INSTANCE;

    public abstract EmployeeDao employeeDao();
    public abstract AttendanceDao attendanceDao();
    public abstract LeaveDao leaveDao();
    public abstract BreakDao breakDao();
    //public abstract LeaveBalanceDao leaveBalanceDao();

    public static HRMSDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (HRMSDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    HRMSDatabase.class,
                                    "hrms_database"
                            )
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}



//package com.agribird.hrmsapp.databasecon;
//
//import android.content.Context;
//
//import androidx.room.Database;
//import androidx.room.Room;
//import androidx.room.RoomDatabase;
//
//import com.agribird.hrmsapp.Model.Attendance;
//import com.agribird.hrmsapp.Model.Employee;
//import com.agribird.hrmsapp.Model.LeaveEntity;
//import com.agribird.hrmsapp.dao.AttendanceDao;
//import com.agribird.hrmsapp.dao.EmployeeDao;
//import com.agribird.hrmsapp.dao.LeaveDao;
//
//
//@Database(entities = {Employee.class, Attendance.class, LeaveEntity.class}, version = 5)
//public abstract class HRMSDatabase extends RoomDatabase {
//
//    private static volatile HRMSDatabase INSTANCE;
//    public abstract EmployeeDao employeeDao();
//    public abstract AttendanceDao attendanceDao();
//
//    public abstract LeaveDao leaveDao();
//
//    public static HRMSDatabase getInstance(final Context context){
//        if(INSTANCE==null){
//            synchronized (HRMSDatabase.class){
//                if(INSTANCE==null){
//                    INSTANCE = Room.databaseBuilder(
//                                    context.getApplicationContext(),
//                                    HRMSDatabase.class,
//                                    "hrms_database"
//                            )
//                            .allowMainThreadQueries()
//                            .fallbackToDestructiveMigration()
//                            .build();
//                }
//            }
//        }
//        return INSTANCE;
//    }
//}
