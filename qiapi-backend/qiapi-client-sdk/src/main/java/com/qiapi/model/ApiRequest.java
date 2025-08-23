package com.qiapi.model;

import lombok.Data;

import java.util.Map;

/**
 * API请求模型
 * 
 * @author zhexueqi
 */
@Data
public class ApiRequest {
    
    /**
     * API唯一标识
     */
    private String apiId;
    
    /**
     * 请求参数
     */
    private Map<String, Object> params;
    
    /**
     * 请求体（用于POST等请求）
     */
    private Object body;
    
    /**
     * 自定义请求头
     */
    private Map<String, String> headers;
    
    /**
     * 用户的accessKey
     */
    private String accessKey;
    
    /**
     * 用户的secretKey
     */
    private String secretKey;
    
    /**
     * 请求超时时间（毫秒）
     */
    private Integer timeout;
    
    public ApiRequest(String apiId) {
        this.apiId = apiId;
    }
}