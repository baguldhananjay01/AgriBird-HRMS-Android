package com.agribird.hrmsapp.api;

import com.agribird.hrmsapp.Model.LeaveBalance;
import com.agribird.hrmsapp.dto.ApiResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface LeaveBalanceApi {

    @GET("api/leave-balance/employee/{empID}")
    Call<ApiResponse<List<LeaveBalance>>> getEmployeeLeaveBalance(
            @Path("empID") String empID,
            @Query("year") String year
    );
}