package com.qiapi.request;

import com.qiapi.client.QiApiClient;
import com.qiapi.model.ApiRequest;
import com.qiapi.model.ApiResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * API请求构建器
 * 提供链式调用方式构建API请求
 * 
 * @author zhexueqi
 * @param <T> 返回数据类型
 */
public class ApiRequestBuilder<T> {
    private final ApiRequest apiRequest;
    private final Class<T> targetType;
    private final QiApiClient client;
    
    public ApiRequestBuilder(ApiRequest apiRequest, Class<T> targetType, QiApiClient client) {
        this.apiRequest = apiRequest;
        this.targetType = targetType;
        this.client = client;
    }
    
    /**
     * 添加请求参数
     * 
     * @param key 参数名
     * @param value 参数值
     * @return ApiRequestBuilder
     */
    public ApiRequestBuilder<T> param(String key, Object value) {
        if (apiRequest.getParams() == null) {
            apiRequest.setParams(new HashMap<>());
        }
        apiRequest.getParams().put(key, value);
        return this;
    }
    
    /**
     * 添加请求头
     * 
     * @param key 头名称
     * @param value 头值
     * @return ApiRequestBuilder
     */
    public ApiRequestBuilder<T> header(String key, String value) {
        if (apiRequest.getHeaders() == null) {
            apiRequest.setHeaders(new HashMap<>());
        }
        apiRequest.getHeaders().put(key, value);
        return this;
    }
    
    /**
     * 设置请求体
     * 
     * @param body 请求体
     * @return ApiRequestBuilder
     */
    public ApiRequestBuilder<T> body(Object body) {
        apiRequest.setBody(body);
        return this;
    }
    
    /**
     * 设置超时时间
     * 
     * @param timeout 超时时间(毫秒)
     * @return ApiRequestBuilder
     */
    public ApiRequestBuilder<T> timeout(int timeout) {
        apiRequest.setTimeout(timeout);
        return this;
    }
    
    /**
     * 执行同步请求
     * 
     * @return ApiResponse
     */
    public ApiResponse<T> execute() {
        return client.callApi(apiRequest, targetType);
    }
    
    /**
     * 执行异步请求
     * 
     * @return CompletableFuture<ApiResponse<T>>
     */
    public CompletableFuture<ApiResponse<T>> async() {
        return CompletableFuture.supplyAsync(this::execute);
    }
}