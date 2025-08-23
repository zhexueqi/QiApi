package com.qiapi.discovery;

import com.qiapi.model.ApiConfig;
import java.util.List;

/**
 * API发现服务接口
 * @author zhexueqi
 */
public interface ApiDiscoveryService {
    
    /**
     * 从远程平台发现API配置
     */
    List<ApiConfig> discoverApis(String platformUrl);
    
    /**
     * 从配置文件发现API配置
     */
    List<ApiConfig> discoverApisFromConfig(String configPath);
    
    /**
     * 根据API ID获取最新的API配置
     */
    ApiConfig getLatestApiConfig(String apiId);
    
    /**
     * 刷新API配置缓存
     */
    void refreshApiConfigs();
}