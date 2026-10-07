package com.agribird.hrmsapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.agribird.hrmsapp.Model.AttendanceApiModel;
import com.agribird.hrmsapp.repository.AttendanceRepository;
import com.agribird.hrmsapp.utils.ApiState;

import java.util.List;

public class AttendanceViewModel extends ViewModel {

    private final AttendanceRepository repository;

    public AttendanceViewModel(AttendanceRepository repository) {
        this.repository = repository;
    }

    public LiveData<ApiState<AttendanceApiModel>> checkIn(
            String employeeId,
            double latitude,
            double longitude,
            String wifiSsid,
            boolean mockLocation) {

        return repository.checkIn(
                employeeId,
                latitude,
                longitude,
                wifiSsid,
                mockLocation
        );
    }

    public LiveData<ApiState<AttendanceApiModel>> checkOut(long employeeId) {
        return repository.checkOut(employeeId);
    }

    public LiveData<ApiState<List<AttendanceApiModel>>> getEmployeeAttendance(
            long employeeId) {

        return repository.getEmployeeAttendance(String.valueOf(employeeId));
    }

    public LiveData<ApiState<List<AttendanceApiModel>>> getTodayAttendance() {

        return repository.getTodayAttendance();
    }

    public LiveData<ApiState<Long>> getTodayPresentCount() {

        return repository.getTodayPresentCount();
    }

    public LiveData<ApiState<Long>> getTodayAttendanceCount() {

        return repository.getTodayAttendanceCount();
    }
}