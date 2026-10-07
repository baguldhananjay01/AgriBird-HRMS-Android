package com.agribird.hrmsapp.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.agribird.hrmsapp.Model.BreakRecord;

import java.util.List;

@Dao
public interface BreakDao {

    @Insert
    void insertBreak(BreakRecord record);

    @Update
    void updateBreak(BreakRecord record);

    @Query("SELECT * FROM break_records WHERE employeeId = :empId AND date = :date AND type = :type AND endTime IS NULL ORDER BY id DESC LIMIT 1")
    BreakRecord getActiveBreak(String empId, String date, String type);

    @Query("SELECT * FROM break_records WHERE employeeId = :empId AND date = :date ORDER BY id DESC")
    List<BreakRecord> getBreaksForDay(String empId, String date);

    @Query("SELECT SUM(durationMinutes) FROM break_records WHERE employeeId = :empId AND date = :date AND endTime IS NOT NULL")
    Long getTotalBreakMinutesForDay(String empId, String date);

    @Query("SELECT COUNT(*) FROM break_records WHERE employeeId = :empId AND date = :date AND type = :type AND endTime IS NOT NULL")
    int getCompletedBreakCount(String empId, String date, String type);

    @Query("SELECT * FROM break_records WHERE employeeId = :empId AND date = :date AND endTime IS NOT NULL ORDER BY id DESC")
    List<BreakRecord> getCompletedBreaksForDay(String empId, String date);
}