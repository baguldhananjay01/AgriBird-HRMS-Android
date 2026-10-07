package com.agribird.hrmsapp.api;

import com.agribird.hrmsapp.Model.AttendanceApiModel;
import com.agribird.hrmsapp.Model.AttendanceSummary;
import com.agribird.hrmsapp.Model.AttendanceSummaryWithTotal;
import com.agribird.hrmsapp.Model.EmployeeApiModel;
import com.agribird.hrmsapp.dto.ApiResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AttendanceApi {

    @POST("api/attendance/check-in")
    Call<ApiResponse<AttendanceApiModel>> checkIn(
            @Query("employeeId") long employeeId,
            @Query("latitude") double latitude,
            @Query("longitude") double longitude,
            @Query("wifiSsid") String wifiSsid,
            @Query("mockLocation") boolean mockLocation
    );

    @POST("api/attendance/check-out/{employeeId}")
    Call<ApiResponse<AttendanceApiModel>> checkOut(@Path("employeeId") long employeeId);

    @GET("api/attendance/employee/{employeeId}")
    Call<ApiResponse<List<AttendanceApiModel>>> getEmployeeAttendance(@Path("employeeId") String employeeId);

    @GET("attendance/today")
    Call<ApiResponse<List<AttendanceApiModel>>> getTodayAttendance();

    @GET("attendance/today/count")
    Call<ApiResponse<Long>> getTodayPresentCount();

    @GET("attendance/today/total")
    Call<ApiResponse<Long>> getTodayAttendanceCount();

    @GET("api/attendance/present-count")
    Call<ApiResponse<Long>> getPresentCountByDate(
            @Query("date") String date
    );

    @GET("api/attendance/on-leave-count")
    Call<ApiResponse<Long>> getOnLeaveCount(
            @Query("date") String date
    );

    @GET("api/attendance/today/summary")
    Call<ApiResponse<AttendanceSummaryWithTotal>> getTodayAttendanceSummary();
}