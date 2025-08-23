package com.qiapi.config;

import com.qiapi.model.ApiConfig;

import java.util.List;

/**
 * API配置管理器接口
 * 
 * @author zhexueqi
 */
public interface ApiConfigManager {
    
    /**
     * 根据API ID获取API配置
     * 
     * @param apiId API唯一标识
     * @return API配置信息
     */
    ApiConfig getApiConfig(String apiId);
    
    /**
     * 获取所有可用的API配置
     * 
     * @return API配置列表
     */
    List<ApiConfig> getAllApiConfigs();
    
    /**
     * 添加API配置
     * 
     * @param apiConfig API配置信息
     */
    void addApiConfig(ApiConfig apiConfig);
    
    /**
     * 更新API配置
     * 
     * @param apiConfig API配置信息
     */
    void updateApiConfig(ApiConfig apiConfig);
    
    /**
     * 删除API配置
     * 
     * @param apiId API唯一标识
     */
    void removeApiConfig(String apiId);
    
    /**
     * 检查API是否存在且可用
     * 
     * @param apiId API唯一标识
     * @return 是否可用
     */
    boolean isApiAvailable(String apiId);
    
    /**
     * 根据分类获取API配置
     * 
     * @param category 分类名称
     * @return API配置列表
     */
    List<ApiConfig> getApiConfigsByCategory(String category);
}