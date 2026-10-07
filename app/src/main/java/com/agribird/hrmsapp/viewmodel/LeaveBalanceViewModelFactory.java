package com.agribird.hrmsapp.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.agribird.hrmsapp.repository.LeaveBalanceRepository;

public class LeaveBalanceViewModelFactory implements ViewModelProvider.Factory {

    private final LeaveBalanceRepository repository;

    public LeaveBalanceViewModelFactory(LeaveBalanceRepository repository) {

        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {

        if (modelClass.isAssignableFrom(LeaveBalanceViewModel.class)) {

            return (T) new LeaveBalanceViewModel(repository);
        }

        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}