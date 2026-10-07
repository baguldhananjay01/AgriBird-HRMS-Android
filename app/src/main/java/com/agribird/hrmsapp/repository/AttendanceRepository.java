package com.agribird.hrmsapp.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.agribird.hrmsapp.Model.AttendanceApiModel;
import com.agribird.hrmsapp.api.AttendanceApi;
import com.agribird.hrmsapp.dto.ApiResponse;
import com.agribird.hrmsapp.utils.ApiState;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AttendanceRepository {

    private final AttendanceApi attendanceApi;

    public AttendanceRepository(AttendanceApi attendanceApi) {
        this.attendanceApi = attendanceApi;
    }

    public LiveData<ApiState<AttendanceApiModel>> checkIn(
            String employeeId,
            double latitude,
            double longitude,
            String wifiSsid,
            boolean mockLocation) {

        MutableLiveData<ApiState<AttendanceApiModel>> result = new MutableLiveData<>();

        result.setValue(ApiState.loading());

        long id;

        try {
            id = Long.parseLong(employeeId);
        } catch (NumberFormatException e) {
            result.setValue(ApiState.error("Invalid Employee ID"));
            return result;
        }

        attendanceApi.checkIn(
                id,
                latitude,
                longitude,
                wifiSsid,
                mockLocation
        ).enqueue(new Callback<ApiResponse<AttendanceApiModel>>() {

            @Override
            public void onResponse(Call<ApiResponse<AttendanceApiModel>> call, Response<ApiResponse<AttendanceApiModel>> response) {

                if (response.isSuccessful() && response.body() != null) {

                    ApiResponse<AttendanceApiModel> apiResponse = response.body();

                    if ("SUCCESS".equalsIgnoreCase(apiResponse.getStatus())) {

                        result.setValue(ApiState.success(apiResponse.getData(), response.body().getMessage()));

                    } else {

                        result.setValue(ApiState.error(apiResponse.getMessage()));
                    }

                } else {
                    result.setValue(ApiState.error("Server Error: HTTP " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AttendanceApiModel>> call, Throwable t) {

                result.setValue(ApiState.error(t.getMessage() != null ? t.getMessage() : "Network Error"));
            }
        });

        return result;
    }

    public LiveData<ApiState<AttendanceApiModel>> checkOut(long employeeId) {
        MutableLiveData<ApiState<AttendanceApiModel>> result = new MutableLiveData<>();

        result.setValue(ApiState.loading());

        attendanceApi.checkOut(employeeId).enqueue(new Callback<ApiResponse<AttendanceApiModel>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<AttendanceApiModel>> call,
                            Response<ApiResponse<AttendanceApiModel>> response) {

                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            result.setValue(ApiState.success(response.body().getData(), response.body().getMessage()));

                        } else {

                            String message = "Check-out failed";
                            if (response.body() != null && response.body().getMessage() != null) {
                                message = response.body().getMessage();
                            }
                            result.setValue(ApiState.error(message));
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ApiResponse<AttendanceApiModel>> call,
                            Throwable t) {
                        result.setValue(ApiState.error(t.getMessage() != null ? t.getMessage() : "Network error"));
                    }
                });

        return result;
    }

    public LiveData<ApiState<List<AttendanceApiModel>>> getEmployeeAttendance(String employeeId) {

        MutableLiveData<ApiState<List<AttendanceApiModel>>> result = new MutableLiveData<>();
        result.setValue(ApiState.loading());

        attendanceApi.getEmployeeAttendance(employeeId).enqueue(new Callback<ApiResponse<List<AttendanceApiModel>>>() {

                    @Override
                    public void onResponse(Call<ApiResponse<List<AttendanceApiModel>>> call, Response<ApiResponse<List<AttendanceApiModel>>> response) {

                        if (response.isSuccessful() && response.body() != null && "SUCCESS".equalsIgnoreCase(response.body().getStatus())) {
                            result.setValue(ApiState.success(response.body().getData(), response.body().getMessage()));

                        } else {
                            result.setValue(ApiState.error("Server Error: HTTP " + response.code()));
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<AttendanceApiModel>>> call, Throwable t) {

                        result.setValue(ApiState.error(t.getMessage() != null ? t.getMessage() : "Network Error"));
                    }
                });

        return result;
    }

    public LiveData<ApiState<List<AttendanceApiModel>>> getTodayAttendance() {

        MutableLiveData<ApiState<List<AttendanceApiModel>>> result = new MutableLiveData<>();

        result.setValue(ApiState.loading());

        attendanceApi.getTodayAttendance().enqueue(new Callback<ApiResponse<List<AttendanceApiModel>>>() {

                    @Override
                    public void onResponse(Call<ApiResponse<List<AttendanceApiModel>>> call, Response<ApiResponse<List<AttendanceApiModel>>> response) {

                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {

                            result.setValue(ApiState.success(response.body().getData(), response.body().getMessage()));

                        } else {

                            String message = "Unable to load today's attendance";

                            if (response.body() != null && response.body().getMessage() != null) {
                                message = response.body().getMessage();
                            }

                            result.setValue(ApiState.error(message));
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<AttendanceApiModel>>> call, Throwable t) {
                        result.setValue(ApiState.error(t.getMessage() != null ? t.getMessage() : "Network error"));
                    }
                });

        return result;
    }

    public LiveData<ApiState<Long>> getTodayPresentCount() {
        MutableLiveData<ApiState<Long>> result = new MutableLiveData<>();
        result.setValue(ApiState.loading());

        attendanceApi.getTodayPresentCount().enqueue(new Callback<ApiResponse<Long>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Long>> call, Response<ApiResponse<Long>> response) {

                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {

                            result.setValue(ApiState.success(response.body().getData(), response.body().getMessage()));

                        } else {
                            String message = "Unable to load present count";

                            if (response.body() != null && response.body().getMessage() != null) {
                                message = response.body().getMessage();
                            }
                            result.setValue(ApiState.error(message));
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Long>> call, Throwable t) {
                        result.setValue(ApiState.error(t.getMessage() != null ? t.getMessage() : "Network error"));
                    }
                });

        return result;
    }
    public LiveData<ApiState<Long>> getTodayAttendanceCount() {

        MutableLiveData<ApiState<Long>> result = new MutableLiveData<>();
        result.setValue(ApiState.loading());
        attendanceApi.getTodayAttendanceCount().enqueue(new Callback<ApiResponse<Long>>() {

                    @Override
                    public void onResponse(Call<ApiResponse<Long>> call, Response<ApiResponse<Long>> response) {

                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            result.setValue(ApiState.success(response.body().getData(), response.body().getMessage()));

                        } else {

                            String message = "Unable to load attendance count";

                            if (response.body() != null && response.body().getMessage() != null) {
                                message = response.body().getMessage();
                            }

                            result.setValue(ApiState.error(message));
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Long>> call, Throwable t) {

                        result.setValue(ApiState.error(t.getMessage() != null ? t.getMessage() : "Network error"));
                    }
                });

        return result;
    }
}