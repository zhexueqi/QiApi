package com.qiapi.response;

import cn.hutool.http.HttpResponse;
import com.qiapi.model.ApiConfig;
import com.qiapi.model.ApiResponse;

/**
 * API响应处理器接口
 * 
 * @author zhexueqi
 */
public interface ResponseHandler {
    
    /**
     * 处理HTTP响应
     * 
     * @param httpResponse HTTP响应
     * @param apiConfig API配置
     * @param targetType 目标类型
     * @param <T> 泛型类型
     * @return API响应对象
     */
    <T> ApiResponse<T> handleResponse(HttpResponse httpResponse, ApiConfig apiConfig, Class<T> targetType);
    
    /**
     * 解析响应数据
     * 
     * @param responseBody 响应体
     * @param responseFormat 响应格式
     * @param targetType 目标类型
     * @param <T> 泛型类型
     * @return 解析后的数据
     */
    <T> T parseResponseData(String responseBody, String responseFormat, Class<T> targetType);
}