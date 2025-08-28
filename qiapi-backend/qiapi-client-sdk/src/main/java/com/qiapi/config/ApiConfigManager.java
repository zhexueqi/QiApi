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
     * 根据API ID从远程平台获取API配置
     * 
     * @param apiId API唯一标识
     * @param platformUrl 平台URL
     * @return API配置信息
     */
    ApiConfig getApiConfigFromPlatform(String apiId, String platformUrl);
    
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
    
    /**
     * 从远程平台加载API配置
     * 
     * @param platformUrl 平台URL
     * @return API配置列表
     */
    List<ApiConfig> loadApiConfigsFromPlatform(String platformUrl);
    
    /**
     * 设置API配置缓存过期时间
     * 
     * @param expireTime 过期时间(秒)
     */
    void setCacheExpireTime(long expireTime);
    
    /**
     * 获取缓存的API配置
     * 
     * @param apiId API标识
     * @return API配置
     */
    ApiConfig getCachedApiConfig(String apiId);
    
    /**
     * 添加API配置监听器
     * 
     * @param listener 监听器
     */
    void addConfigChangeListener(ConfigChangeListener listener);
    
    /**
     * 设置默认平台URL
     * 
     * @param platformUrl 平台URL
     */
    void setDefaultPlatformUrl(String platformUrl);
}