package com.qiapi.request;

import cn.hutool.http.HttpRequest;
import com.qiapi.model.ApiConfig;
import com.qiapi.model.ApiRequest;

/**
 * API请求构建器接口
 * 
 * @author zhexueqi
 */
public interface RequestBuilder {
    
    /**
     * 构建HTTP请求
     * 
     * @param apiConfig API配置
     * @param apiRequest API请求
     * @return HttpRequest对象
     */
    HttpRequest buildRequest(ApiConfig apiConfig, ApiRequest apiRequest);
    
    /**
     * 构建请求URL
     * 
     * @param apiConfig API配置
     * @param apiRequest API请求
     * @return 完整的请求URL
     */
    String buildUrl(ApiConfig apiConfig, ApiRequest apiRequest);
    
    /**
     * 构建请求头
     * 
     * @param apiConfig API配置
     * @param apiRequest API请求
     * @return 请求头Map
     */
    java.util.Map<String, String> buildHeaders(ApiConfig apiConfig, ApiRequest apiRequest);
    
    /**
     * 构建请求体
     * 
     * @param apiConfig API配置
     * @param apiRequest API请求
     * @return 请求体内容
     */
    String buildBody(ApiConfig apiConfig, ApiRequest apiRequest);
}