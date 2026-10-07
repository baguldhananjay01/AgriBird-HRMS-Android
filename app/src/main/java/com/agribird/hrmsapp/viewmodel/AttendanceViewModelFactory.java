package com.agribird.hrmsapp.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.agribird.hrmsapp.repository.AttendanceRepository;

public class AttendanceViewModelFactory
        implements ViewModelProvider.Factory {

    private final AttendanceRepository repository;

    public AttendanceViewModelFactory(
            AttendanceRepository repository) {

        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(
            @NonNull Class<T> modelClass) {

        if (modelClass.isAssignableFrom(AttendanceViewModel.class)) {
            return (T) new AttendanceViewModel(repository);
        }

        throw new IllegalArgumentException(
                "Unknown ViewModel class"
        );
    }
}