package com.enterprise.kb.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.Instant;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final String message;
    private final String code;
    private final Object details;
    private final String traceId;
    private final Instant timestamp = Instant.now();

    private ApiResponse(boolean success, T data, String message, String code, Object details, String traceId) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.code = code;
        this.details = details;
        this.traceId = traceId;
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, null, null, null);
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(true, data, message, null, null, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return error("ERROR", message, null, null);
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return error(code, message, null, null);
    }

    public static <T> ApiResponse<T> error(String code, String message, Object details, String traceId) {
        return new ApiResponse<>(false, null, message, code, details, traceId);
    }
}
