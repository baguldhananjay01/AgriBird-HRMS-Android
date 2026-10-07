    package com.agribird.hrmsapp.api;

    import com.agribird.hrmsapp.Model.EmployeeApiModel;
    import com.agribird.hrmsapp.Model.LoginResponseModel;
    import com.agribird.hrmsapp.dto.ApiResponse;

    import java.util.List;

    import retrofit2.Call;
    import retrofit2.http.Body;
    import retrofit2.http.DELETE;
    import retrofit2.http.GET;
    import retrofit2.http.POST;
    import retrofit2.http.PUT;
    import retrofit2.http.Path;

    public interface EmployeeApi {

        // Get all employees
        @GET("api/employees")
        Call<ApiResponse<List<EmployeeApiModel>>> getAllEmployees();


        // Add employee
        @POST("api/employees")
        Call<ApiResponse<EmployeeApiModel>> addEmployee(
                @Body EmployeeApiModel employee
        );


        // Get employee using custom Employee ID
        // Example: EMP995
        @GET("api/employees/employee-id/{empID}")
        Call<ApiResponse<EmployeeApiModel>> getEmployeeByEmpID(
                @Path("empID") String empID
        );

        @PUT("api/employees/update/{empID}")
        Call<ApiResponse<EmployeeApiModel>> updateEmployee(
                @Path("empID") String empID,
                @Body EmployeeApiModel employee
        );

        @POST("api/employees/login")
        Call<ApiResponse<LoginResponseModel>> loginEmployee(
                @Body EmployeeApiModel employee);

        @DELETE("api/employees/emp/{empID}")
        Call<ApiResponse<Void>> deleteEmployee(
                @Path("empID") String empID
        );
    }