package com.agribird.hrmsapp.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.agribird.hrmsapp.Model.LeaveApiModel;
import com.agribird.hrmsapp.api.LeaveApi;
import com.agribird.hrmsapp.dto.ApiResponse;
import com.agribird.hrmsapp.utils.ApiState;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LeaveRepository {

    private final LeaveApi leaveApi;

    public LeaveRepository(LeaveApi leaveApi) {
        this.leaveApi = leaveApi;
    }

    public LiveData<ApiState<List<LeaveApiModel>>> getEmployeeLeaves(String empID) {

        MutableLiveData<ApiState<List<LeaveApiModel>>> liveData =
                new MutableLiveData<>();

        liveData.setValue(ApiState.loading());

        leaveApi.getEmployeeLeaves(empID)
                .enqueue(new Callback<ApiResponse<List<LeaveApiModel>>>() {

                    @Override
                    public void onResponse(
                            Call<ApiResponse<List<LeaveApiModel>>> call,
                            Response<ApiResponse<List<LeaveApiModel>>> response) {

                        if (response.isSuccessful()
                                && response.body() != null) {

                            ApiResponse<List<LeaveApiModel>> body =
                                    response.body();

                            liveData.setValue(
                                    ApiState.success(
                                            body.getData(),
                                            body.getMessage()
                                    )
                            );

                        } else {

                            liveData.setValue(
                                    ApiState.error(
                                            "Unable to load leave data."
                                    )
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ApiResponse<List<LeaveApiModel>>> call,
                            Throwable t) {

                        String message = t.getMessage();

                        if (message == null || message.trim().isEmpty()) {
                            message = "Network error.";
                        }

                        liveData.setValue(
                                ApiState.error(message)
                        );
                    }
                });

        return liveData;
    }
}