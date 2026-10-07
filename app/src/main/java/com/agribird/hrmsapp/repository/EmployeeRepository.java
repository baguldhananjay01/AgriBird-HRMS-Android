package com.agribird.hrmsapp.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.agribird.hrmsapp.Model.EmployeeApiModel;
import com.agribird.hrmsapp.api.EmployeeApi;
import com.agribird.hrmsapp.dto.ApiResponse;
import com.agribird.hrmsapp.network.RetrofitClient;
import com.agribird.hrmsapp.utils.ApiState;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeeRepository {

    private final EmployeeApi employeeApi;

    public EmployeeRepository(Context context) {
        employeeApi = RetrofitClient.getEmployeeApi(context);
    }

    public LiveData<ApiState<EmployeeApiModel>> addEmployee(EmployeeApiModel employee){
        MutableLiveData<ApiState<EmployeeApiModel>> result= new MutableLiveData<>();

        result.setValue(ApiState.loading());

        employeeApi.addEmployee(employee).enqueue(new Callback<ApiResponse<EmployeeApiModel>>() {
            @Override
            public void onResponse(Call<ApiResponse<EmployeeApiModel>> call, Response<ApiResponse<EmployeeApiModel>> response) {
                if(response.isSuccessful()
                    && response.body() !=null
                    && response.body().getData() !=null){

                    result.setValue(ApiState.success(response.body().getData(), response.body().getMessage()));
                }else{
                    String message = "Failed to add employee";

                    if(response.body() !=null
                        && response.body().getMessage() !=null){
                        message = response.body().getMessage();
                    }
                    result.setValue(ApiState.error(message));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<EmployeeApiModel>> call, Throwable throwable) {

                result.setValue(ApiState.error("Sever unavailable. Please check your internet connection"));

            }
        });
        return result;
    }
    public LiveData<ApiState<List<EmployeeApiModel>>> getAllEmployees() {
        MutableLiveData<ApiState<List<EmployeeApiModel>>> result = new MutableLiveData<>();

        // 1. LOADING
        result.setValue(ApiState.loading());

        employeeApi.getAllEmployees().enqueue(new Callback<ApiResponse<List<EmployeeApiModel>>>() {

                    @Override
                    public void onResponse(Call<ApiResponse<List<EmployeeApiModel>>> call, Response<ApiResponse<List<EmployeeApiModel>>> response) {

                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            // 2. SUCCESS
                            result.setValue(ApiState.success(response.body().getData(), response.body().getMessage()));

                        } else {

                            String message = "Failed to load employees";

                            if (response.body() != null
                                    && response.body().getMessage() != null
                                    && !response.body().getMessage().isEmpty()) {

                                message = response.body().getMessage();

                            } else if (response.code() >= 500) {
                                message = "Server error. Please try again later.";
                            } else if (response.code() >= 400) {
                                message = "Request failed. Please check your request.";
                            }

                            // 3. ERROR
                            result.setValue(ApiState.error(message)
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ApiResponse<List<EmployeeApiModel>>> call, Throwable t) {

                        String message;

                        if (t instanceof java.net.UnknownHostException) {

                            message = "No internet connection.";

                        } else if (t instanceof java.net.SocketTimeoutException) {

                            message = "Server connection timed out.";

                        } else if (t instanceof java.io.IOException) {

                            message = "Network error. Please check your internet connection.";

                        } else {

                            message = "Unable to connect to server.";

                        }
                        result.setValue(ApiState.error(message));
                    }
                });

        return result;
    }

    public LiveData<ApiState<EmployeeApiModel>> getEmployeeByEmpId(String employeeId){
        MutableLiveData<ApiState<EmployeeApiModel>> result=new MutableLiveData<>();
        result.setValue(ApiState.loading());

        employeeApi.getEmployeeByEmpID(employeeId).enqueue(new Callback<ApiResponse<EmployeeApiModel>>() {
            @Override
            public void onResponse(Call<ApiResponse<EmployeeApiModel>> call, Response<ApiResponse<EmployeeApiModel>> response) {

                if(response.isSuccessful() && response.body() !=null
                    && response.body().getData() !=null ){

                    result.setValue(ApiState.success(response.body().getData(), response.body().getMessage()));
                }else{
                    String massage = "Employee not found";

                    if(response.body() !=null && response.body().getMessage() !=null
                            && !response.body().getMessage().isEmpty()){

                        massage= response.body().getMessage();
                    }
                    result.setValue(ApiState.error(massage));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<EmployeeApiModel>> call, Throwable throwable) {

                String message;

                if(throwable instanceof java.net.UnknownHostException){
                    message= "No internet connection. ";
                } else if (throwable instanceof java.net.SocketTimeoutException) {
                    message = "Server connection timed out. ";
                } else if (throwable instanceof java.io.IOException) {
                    message = "Network error. Please check your connection. ";
                }else{
                    message = "Unable to connect to sever.";
                }
                result.setValue(ApiState.error(message));
            }
        });

        return  result;
    }

    public LiveData<ApiState<Void>> deleteEmployee(String empID){

        MutableLiveData<ApiState<Void>> result = new MutableLiveData<>();
        result.setValue(ApiState.loading());

        employeeApi.deleteEmployee(empID).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {

                if (response.isSuccessful()
                    && response.body() != null){
                    result.setValue(ApiState.success(null, response.body().getMessage()));
                }else{
                    String message;
                    if(response.code()==404){
                        message = "Employee not found";
                    } else if (response.code()==500) {
                        message = "Server error. please try again later.";
                    }else{
                        message = "Delete failed. HTTP"+response.code();
                    }
                    result.setValue(ApiState.error(message));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable throwable) {

                result.setValue(ApiState.error("Server.unavailable. Please check your internet connection."));
            }
        });

        return result;
    }

    public LiveData<ApiState<EmployeeApiModel>> updateEmployee(String empID,EmployeeApiModel employee){

        MutableLiveData<ApiState<EmployeeApiModel>> result=new MutableLiveData<>();

        result.setValue(ApiState.loading());

        employeeApi.updateEmployee(empID,employee).enqueue(new Callback<ApiResponse<EmployeeApiModel>>() {
            @Override
            public void onResponse(Call<ApiResponse<EmployeeApiModel>> call, Response<ApiResponse<EmployeeApiModel>> response) {

                if(response.isSuccessful()
                    && response.body() !=null
                    && response.body().getData() !=null){
                    result.setValue(ApiState.success(response.body().getData(), response.body().getMessage()));
                }else{
                    String message="Update failed";
                    if(response.body() !=null
                        && response.body().getMessage() !=null){
                        message = response.body().getMessage();
                    }
                    result.setValue(ApiState.error(message));
                }

            }

            @Override
            public void onFailure(Call<ApiResponse<EmployeeApiModel>> call, Throwable throwable) {

                result.setValue(ApiState.error("Server unavailable. Please check your internet connection"));

            }
        });

        return result;
    }
}