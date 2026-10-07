package com.agribird.hrmsapp.network;

import android.content.Context;

import com.agribird.hrmsapp.api.AttendanceApi;
import com.agribird.hrmsapp.api.EmployeeApi;
import com.agribird.hrmsapp.api.LeaveApi;
import com.agribird.hrmsapp.api.LeaveBalanceApi;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL =
            "http://10.133.40.152:1010/";

    private static Retrofit retrofit;

    public static Retrofit getRetrofitInstance(Context context) {

        if (retrofit == null) {

            AuthInterceptor authInterceptor =
                    new AuthInterceptor(context);

            OkHttpClient client =
                    new OkHttpClient.Builder()
                            .addInterceptor(authInterceptor)
                            .connectTimeout(10, TimeUnit.SECONDS)
                            .readTimeout(10, TimeUnit.SECONDS)
                            .writeTimeout(10, TimeUnit.SECONDS)
                            .build();

            retrofit =
                    new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .client(client)
                            .addConverterFactory(
                                    GsonConverterFactory.create()
                            )
                            .build();
        }

        return retrofit;
    }

    public static EmployeeApi getEmployeeApi(Context context) {
        return getRetrofitInstance(context)
                .create(EmployeeApi.class);
    }

    public static AttendanceApi getAttendanceApi(Context context) {
        return getRetrofitInstance(context)
                .create(AttendanceApi.class);
    }

    public static LeaveApi getLeaveApi(Context context) {
        return getRetrofitInstance(context)
                .create(LeaveApi.class);
    }

    public static LeaveBalanceApi getLeaveBalanceApi(Context context) {
        return getRetrofitInstance(context)
                .create(LeaveBalanceApi.class);
    }
}
//package com.agribird.hrmsapp.network;
//
//import android.content.Context;
//
//import com.agribird.hrmsapp.api.AttendanceApi;
//import com.agribird.hrmsapp.api.EmployeeApi;
//import com.agribird.hrmsapp.api.LeaveApi;
//import com.agribird.hrmsapp.api.LeaveBalanceApi;
//
//import java.util.concurrent.TimeUnit;
//
//import okhttp3.OkHttpClient;
//import retrofit2.Retrofit;
//import retrofit2.converter.gson.GsonConverterFactory;
//
//public class RetrofitClient {
//
//    private static final String BASE_URL =
//            "http://10.235.122.152:1010/";
//
//    private static Retrofit retrofit;
//
//    public static Retrofit getRetrofitInstance(Context context) {
//
//        if (retrofit == null) {
//
//            AuthInterceptor authInterceptor =
//                    new AuthInterceptor(context);
//
//            OkHttpClient client =
//                    new OkHttpClient.Builder()
//                            .addInterceptor(authInterceptor)
//                            .connectTimeout(10, TimeUnit.SECONDS)
//                            .readTimeout(10, TimeUnit.SECONDS)
//                            .writeTimeout(10, TimeUnit.SECONDS)
//                            .build();
//
//            retrofit =
//                    new Retrofit.Builder()
//                            .baseUrl(BASE_URL)
//                            .client(client)
//                            .addConverterFactory(
//                                    GsonConverterFactory.create()
//                            )
//                            .build();
//        }
//
//        return retrofit;
//    }
//
//    public static EmployeeApi getEmployeeApi(Context context) {
//
//        return getRetrofitInstance(context)
//                .create(EmployeeApi.class);
//    }
//
//    public static AttendanceApi getAttendanceApi(Context context) {
//
//        return getRetrofitInstance(context)
//                .create(AttendanceApi.class);
//    }
//
//    public static LeaveApi getLeaveApi(Context context) {
//
//        return getRetrofitInstance(context)
//                .create(LeaveApi.class);
//    }
//
//    public static LeaveBalanceApi getLeaveBalanceApi(Context context) {
//
//        return getRetrofitInstance(context)
//                .create(LeaveBalanceApi.class);
//    }
//}