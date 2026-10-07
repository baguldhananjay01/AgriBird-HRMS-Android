package com.agribird.hrmsapp.viewmodel;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.agribird.hrmsapp.Model.EmployeeApiModel;
import com.agribird.hrmsapp.repository.EmployeeRepository;
import com.agribird.hrmsapp.utils.ApiState;

import java.util.List;

public class EmployeeViewModel extends AndroidViewModel {

    private final EmployeeRepository repository;

    public EmployeeViewModel(@NonNull Application application) {
        super(application);
        repository = new EmployeeRepository(application);
    }

    public LiveData<ApiState<EmployeeApiModel>> addEmployee(
            EmployeeApiModel employee) {

        return repository.addEmployee(employee);
    }

    public LiveData<ApiState<List<EmployeeApiModel>>> getAllEmployees() {

        return repository.getAllEmployees();
    }

    public LiveData<ApiState<EmployeeApiModel>> getEmployeeByEmpId(String empID){
        return  repository.getEmployeeByEmpId(empID);
    }

    public LiveData<ApiState<EmployeeApiModel>> updateEmployee(
            String employeeId,
            EmployeeApiModel employee
    ) {

        return repository.updateEmployee(employeeId, employee);
    }

    public LiveData<ApiState<Void>> deleteEmployee(String empID){
        return repository.deleteEmployee(empID);
    }
}
//package com.agribird.hrmsapp.viewmodel;
//
//import androidx.lifecycle.LiveData;
//import androidx.lifecycle.MutableLiveData;
//import androidx.lifecycle.ViewModel;
//
//import com.agribird.hrmsapp.Model.EmployeeApiModel;
//import com.agribird.hrmsapp.repository.EmployeeRepository;
//import com.agribird.hrmsapp.utils.ApiState;
//
//import java.util.List;
//
//public class EmployeeViewModel extends ViewModel {
//
//
//    private MutableLiveData<ApiState<List<EmployeeApiModel>>> employeeLiveData;
//
//    private final EmployeeRepository repository;
//
//    public EmployeeViewModel() {
//        repository = new EmployeeRepository();
//    }
//
//    public LiveData<List<EmployeeApiModel>> getAllEmployees() {
//
//        return repository.getAllEmployees();
//    }
//
//    public MutableLiveData<ApiState<List<EmployeeApiModel>>> getEmployeeLiveData() {
//        return employeeLiveData;
//    }
//}