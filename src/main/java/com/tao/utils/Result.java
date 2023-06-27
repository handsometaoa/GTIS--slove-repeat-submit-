package com.tao.utils;

public class Result<T> {

    String code;
    String message;
    T data;

    public static <T> Result<T> success(T data) {
        return new Result("200", "success", data);
    }

    public Result(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> failure(String code, String errorMsg) {
        return new Result<>(code, errorMsg, null);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
