package com.qiapi.model;

import lombok.Data;

import java.util.Map;

/**
 * API接口配置信息
 * 
 * @author zhexueqi
 */
@Data
public class ApiConfig {
    
    /**
     * API唯一标识
     */
    private String apiId;
    
    /**
     * API名称
     */
    private String apiName;
    
    /**
     * API描述
     */
    private String description;
    
    /**
     * 基础URL（不包含路径）
     */
    private String baseUrl;
    
    /**
     * API路径
     */
    private String path;
    
    /**
     * HTTP方法（GET, POST, PUT, DELETE等）
     */
    private String method;
    
    /**
     * 请求参数类型（QUERY, BODY, FORM等）
     */
    private String paramType;
    
    /**
     * 是否需要认证
     */
    private boolean requireAuth;
    
    /**
     * 认证类型（SIGNATURE, TOKEN等）
     */
    private String authType;
    
    /**
     * 请求头配置
     */
    private Map<String, String> headers;
    
    /**
     * 参数映射配置
     */
    private Map<String, String> paramMapping;
    
    /**
     * 响应数据格式（JSON, XML, TEXT等）
     */
    private String responseFormat;
    
    /**
     * API状态（ACTIVE, INACTIVE）
     */
    private String status;
    
    /**
     * API版本
     */
    private String version;
}