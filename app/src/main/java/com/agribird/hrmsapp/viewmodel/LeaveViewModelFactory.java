package com.agribird.hrmsapp.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.agribird.hrmsapp.repository.LeaveRepository;

public class LeaveViewModelFactory implements ViewModelProvider.Factory {

    private final LeaveRepository repository;

    public LeaveViewModelFactory(LeaveRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(
            @NonNull Class<T> modelClass) {

        if (modelClass.isAssignableFrom(LeaveViewModel.class)) {

            return (T) new LeaveViewModel(repository);
        }

        throw new IllegalArgumentException(
                "Unknown ViewModel class: " + modelClass.getName()
        );
    }
}