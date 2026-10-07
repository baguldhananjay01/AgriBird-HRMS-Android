package com.agribird.hrmsapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.agribird.hrmsapp.Model.LeaveBalance;
import com.agribird.hrmsapp.repository.LeaveBalanceRepository;
import com.agribird.hrmsapp.utils.ApiState;

import java.util.List;

public class LeaveBalanceViewModel extends ViewModel {

    private final LeaveBalanceRepository repository;

    public LeaveBalanceViewModel(
            LeaveBalanceRepository repository) {

        this.repository = repository;
    }

    public LiveData<ApiState<List<LeaveBalance>>>
    getEmployeeLeaveBalance(String empID, String year) {

        return repository.getEmployeeLeaveBalance(
                empID,
                year);
    }
}