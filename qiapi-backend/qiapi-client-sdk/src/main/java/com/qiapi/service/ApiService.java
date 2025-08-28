package com.qiapi.service;

import com.qiapi.config.ApiConfigManager;
import com.qiapi.model.ApiConfig;
import com.qiapi.request.ApiRequestBuilder;

import java.util.List;

/**
 * API服务基础接口
 * 定义通用API服务方法
 * 
 * @author zhexueqi
 */
public interface ApiService {
    
    /**
     * 创建API调用请求构建器
     * 
     * @param apiId API标识
     * @param targetType 返回类型
     * @param <T> 泛型类型
     * @return ApiRequestBuilder
     */
    <T> ApiRequestBuilder<T> call(String apiId, Class<T> targetType);
    
    /**
     * 获取所有可用API配置
     * 
     * @return API配置列表
     */
    List<ApiConfig> getAvailableApis();
    
    /**
     * 刷新API配置缓存
     */
    void refreshApiConfigs();
    
    /**
     * 获取API配置管理器
     * 
     * @return API配置管理器
     */
    ApiConfigManager getApiConfigManager();
}