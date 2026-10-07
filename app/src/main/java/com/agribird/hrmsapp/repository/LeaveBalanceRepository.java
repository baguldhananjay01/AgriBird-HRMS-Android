package com.agribird.hrmsapp.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.agribird.hrmsapp.Model.LeaveBalance;
import com.agribird.hrmsapp.api.LeaveBalanceApi;
import com.agribird.hrmsapp.dto.ApiResponse;
import com.agribird.hrmsapp.utils.ApiState;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LeaveBalanceRepository {
    private final LeaveBalanceApi leaveBalanceApi;
    public LeaveBalanceRepository(LeaveBalanceApi leaveBalanceApi) {

        this.leaveBalanceApi = leaveBalanceApi;
    }

    public LiveData<ApiState<List<LeaveBalance>>>
    getEmployeeLeaveBalance(String empID, String year) {

        MutableLiveData<ApiState<List<LeaveBalance>>> liveData = new MutableLiveData<>();

        liveData.setValue(ApiState.loading());
        leaveBalanceApi.getEmployeeLeaveBalance(empID, year).enqueue(new Callback<ApiResponse<List<LeaveBalance>>>() {

                            @Override
                            public void onResponse(Call<ApiResponse<List<LeaveBalance>>> call, Response<ApiResponse<List<LeaveBalance>>> response) {

                                if (response.isSuccessful() && response.body() != null) {

                                    ApiResponse<List<LeaveBalance>> body = response.body();

                                    liveData.setValue(ApiState.success(body.getData(), body.getMessage()));

                                } else {

                                    liveData.setValue(ApiState.error("Unable to load leave balance."));
                                }
                            }

                            @Override
                            public void onFailure(Call<ApiResponse<List<LeaveBalance>>> call, Throwable t) {

                                String message = t.getMessage();

                                if (message == null || message.trim().isEmpty()) {

                                    message = "Network error.";
                                }

                                liveData.setValue(ApiState.error(message));
                            }
                        }
                );

        return liveData;
    }
}