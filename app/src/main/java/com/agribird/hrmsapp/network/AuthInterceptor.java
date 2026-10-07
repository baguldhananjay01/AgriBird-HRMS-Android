package com.agribird.hrmsapp.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final Context context;

    public AuthInterceptor(Context context) {
        this.context = context.getApplicationContext();

        Log.d("JWT_TEST", "AuthInterceptor CREATED");
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {

        Log.d("JWT_TEST", "INTERCEPTOR CALLED");

        SharedPreferences sharedPreferences =
                context.getSharedPreferences(
                        "HRMS_SESSION",
                        Context.MODE_PRIVATE
                );

        String token =
                sharedPreferences.getString("jwtToken", "");

        Log.d("JWT_TEST", "TOKEN = [" + token + "]");

        Request originalRequest = chain.request();

        Log.d(
                "JWT_TEST",
                "REQUEST = " + originalRequest.url()
        );

        if (token == null || token.trim().isEmpty()) {

            Log.d("JWT_TEST", "NO JWT TOKEN");

            return chain.proceed(originalRequest);
        }

        Request newRequest =
                originalRequest.newBuilder()
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
                        .build();

        Log.d(
                "JWT_TEST",
                "AUTH HEADER ADDED"
        );

        return chain.proceed(newRequest);
    }
}