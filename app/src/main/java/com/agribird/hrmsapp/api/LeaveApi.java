package com.agribird.hrmsapp.api;

import com.agribird.hrmsapp.Model.LeaveApiModel;
import com.agribird.hrmsapp.dto.ApiResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface LeaveApi {
    @POST("api/leaves/apply/{empID}")
    Call<ApiResponse<LeaveApiModel>> applyLeave(
            @Path("empID") String empID,
            @Body LeaveApiModel leave
    );
    @GET("api/leaves")
    Call<ApiResponse<List<LeaveApiModel>>> getAllLeave();
    @GET("api/leaves/employee/{empID}")
    Call<ApiResponse<List<LeaveApiModel>>> getEmployeeLeaves(
            @Path("empID") String empID
    );
    @GET("api/leaves/status/{status}")
    Call<ApiResponse<List<LeaveApiModel>>> getLeavesByStatus(
            @Path("status") String status
    );
    @GET("api/leaves/{id}")
    Call<ApiResponse<LeaveApiModel>> getLeaveById(
            @Path("id") long id
    );

    @PUT("api/leaves/{id}/status")
    Call<ApiResponse<LeaveApiModel>> updateLeaveStatus(
            @Path("id") long id,
            @Query("status") String status
    );

    @GET("api/leaves/count/pending")
    Call<ApiResponse<Long>> getPendingLeaveCount();

    @GET("api/leaves/employee/{empID}/count/pending")
    Call<ApiResponse<Long>> getEmployeePendingLeaveCount(
            @Path("empID") String empID
    );
}
