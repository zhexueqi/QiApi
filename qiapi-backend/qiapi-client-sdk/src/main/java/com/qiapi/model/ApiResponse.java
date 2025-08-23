package com.qiapi.model;

import lombok.Data;

import java.util.Map;

/**
 * API响应模型
 * 
 * @author zhexueqi
 */
@Data
public class ApiResponse<T> {
    
    /**
     * 响应状态码
     */
    private int statusCode;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 响应头
     */
    private Map<String, String> headers;
    
    /**
     * 原始响应内容
     */
    private String rawResponse;
    
    /**
     * 是否成功
     */
    private boolean success;
    
    /**
     * 错误码
     */
    private String errorCode;
    
    /**
     * 请求ID（用于追踪）
     */
    private String requestId;
    
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setStatusCode(200);
        response.setData(data);
        response.setMessage("success");
        return response;
    }
    
    public static <T> ApiResponse<T> error(int statusCode, String errorCode, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setStatusCode(statusCode);
        response.setErrorCode(errorCode);
        response.setMessage(message);
        return response;
    }
}