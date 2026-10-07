package com.agribird.hrmsapp.utils;

public class ApiState<T> {

    public enum Status {
        LOADING,
        SUCCESS,
        ERROR
    }

    private final Status status;
    private final T data;
    private final String message;

    private ApiState(Status status, T data, String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    public static <T> ApiState<T> loading() {
        return new ApiState<>(Status.LOADING, null, null);
    }

    public static <T> ApiState<T> success(T data, String message) {
        return new ApiState<>(Status.SUCCESS, data, null);
    }

    public static <T> ApiState<T> error(String message) {
        return new ApiState<>(Status.ERROR, null, message);
    }

    public Status getStatus() {
        return status;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    // Status helper methods

    public boolean isLoading() {
        return status == Status.LOADING;
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public boolean isError() {
        return status == Status.ERROR;
    }
}