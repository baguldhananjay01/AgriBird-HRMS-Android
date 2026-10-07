package com.agribird.hrmsapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.agribird.hrmsapp.Model.LeaveApiModel;
import com.agribird.hrmsapp.repository.LeaveRepository;
import com.agribird.hrmsapp.utils.ApiState;

import java.util.List;

public class LeaveViewModel extends ViewModel {

    private final LeaveRepository repository;

    public LeaveViewModel(LeaveRepository repository) {
        this.repository = repository;
    }

    public LiveData<ApiState<List<LeaveApiModel>>> getEmployeeLeaves(
            String empID) {

        return repository.getEmployeeLeaves(empID);
    }
}