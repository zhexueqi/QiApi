package com.qiapi.config;

import com.qiapi.model.ApiConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * API配置管理器默认实现
 * 
 * @author zhexueqi
 */
@Slf4j
public class DefaultApiConfigManager implements ApiConfigManager {
    
    private final Map<String, ApiConfig> apiConfigs = new ConcurrentHashMap<>();
    
    public DefaultApiConfigManager() {
        initDefaultConfigs();
    }
    
    /**
     * 初始化默认配置
     */
    private void initDefaultConfigs() {
        // 初始化一些默认的API配置
        addDefaultNameApi();
        addDefaultWeatherApi();
        addDefaultUserApi();
    }
    
    private void addDefaultNameApi() {
        ApiConfig nameApi = new ApiConfig();
        nameApi.setApiId("name.get");
        nameApi.setApiName("获取用户名");
        nameApi.setDescription("根据名称获取用户信息");
        nameApi.setBaseUrl("http://localhost:8081");
        nameApi.setPath("/api/name");
        nameApi.setMethod("GET");
        nameApi.setParamType("QUERY");
        nameApi.setRequireAuth(true);
        nameApi.setAuthType("SIGNATURE");
        nameApi.setResponseFormat("TEXT");
        nameApi.setStatus("ACTIVE");
        nameApi.setVersion("1.0");
        
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        nameApi.setHeaders(headers);
        
        addApiConfig(nameApi);
        
        // POST版本的name接口
        ApiConfig namePostApi = new ApiConfig();
        namePostApi.setApiId("name.post");
        namePostApi.setApiName("提交用户名");
        namePostApi.setDescription("通过POST方式提交用户名");
        namePostApi.setBaseUrl("http://localhost:8081");
        namePostApi.setPath("/api/name");
        namePostApi.setMethod("POST");
        namePostApi.setParamType("FORM");
        namePostApi.setRequireAuth(true);
        namePostApi.setAuthType("SIGNATURE");
        namePostApi.setResponseFormat("TEXT");
        namePostApi.setStatus("ACTIVE");
        namePostApi.setVersion("1.0");
        namePostApi.setHeaders(headers);
        
        addApiConfig(namePostApi);
        
        // RESTful版本的name接口
        ApiConfig nameRestfulApi = new ApiConfig();
        nameRestfulApi.setApiId("name.restful");
        nameRestfulApi.setApiName("RESTful用户名接口");
        nameRestfulApi.setDescription("通过RESTful方式处理用户信息");
        nameRestfulApi.setBaseUrl("http://localhost:8081");
        nameRestfulApi.setPath("/api/name/restful");
        nameRestfulApi.setMethod("POST");
        nameRestfulApi.setParamType("BODY");
        nameRestfulApi.setRequireAuth(true);
        nameRestfulApi.setAuthType("SIGNATURE");
        nameRestfulApi.setResponseFormat("TEXT");
        nameRestfulApi.setStatus("ACTIVE");
        nameRestfulApi.setVersion("1.0");
        nameRestfulApi.setHeaders(headers);
        
        addApiConfig(nameRestfulApi);
    }
    
    private void addDefaultWeatherApi() {
        ApiConfig weatherApi = new ApiConfig();
        weatherApi.setApiId("weather.current");
        weatherApi.setApiName("当前天气");
        weatherApi.setDescription("获取指定城市的当前天气信息");
        weatherApi.setBaseUrl("http://api.weather.com");
        weatherApi.setPath("/v1/current");
        weatherApi.setMethod("GET");
        weatherApi.setParamType("QUERY");
        weatherApi.setRequireAuth(true);
        weatherApi.setAuthType("SIGNATURE");
        weatherApi.setResponseFormat("JSON");
        weatherApi.setStatus("ACTIVE");
        weatherApi.setVersion("1.0");
        
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        weatherApi.setHeaders(headers);
        
        addApiConfig(weatherApi);
    }
    
    private void addDefaultUserApi() {
        ApiConfig userApi = new ApiConfig();
        userApi.setApiId("user.info");
        userApi.setApiName("用户信息");
        userApi.setDescription("获取用户详细信息");
        userApi.setBaseUrl("http://localhost:8080");
        userApi.setPath("/api/user/info");
        userApi.setMethod("GET");
        userApi.setParamType("QUERY");
        userApi.setRequireAuth(true);
        userApi.setAuthType("SIGNATURE");
        userApi.setResponseFormat("JSON");
        userApi.setStatus("ACTIVE");
        userApi.setVersion("1.0");
        
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        userApi.setHeaders(headers);
        
        addApiConfig(userApi);
    }
    
    @Override
    public ApiConfig getApiConfig(String apiId) {
        return apiConfigs.get(apiId);
    }
    
    @Override
    public List<ApiConfig> getAllApiConfigs() {
        return new ArrayList<>(apiConfigs.values());
    }
    
    @Override
    public void addApiConfig(ApiConfig apiConfig) {
        if (apiConfig != null && apiConfig.getApiId() != null) {
            apiConfigs.put(apiConfig.getApiId(), apiConfig);
            log.info("API配置已添加: {}", apiConfig.getApiId());
        }
    }
    
    @Override
    public void updateApiConfig(ApiConfig apiConfig) {
        if (apiConfig != null && apiConfig.getApiId() != null) {
            apiConfigs.put(apiConfig.getApiId(), apiConfig);
            log.info("API配置已更新: {}", apiConfig.getApiId());
        }
    }
    
    @Override
    public void removeApiConfig(String apiId) {
        if (apiId != null) {
            apiConfigs.remove(apiId);
            log.info("API配置已删除: {}", apiId);
        }
    }
    
    @Override
    public boolean isApiAvailable(String apiId) {
        ApiConfig config = getApiConfig(apiId);
        return config != null && "ACTIVE".equals(config.getStatus());
    }
    
    @Override
    public List<ApiConfig> getApiConfigsByCategory(String category) {
        // 这里可以根据实际需要扩展分类逻辑
        return apiConfigs.values().stream()
                .filter(config -> config.getApiName().contains(category))
                .collect(Collectors.toList());
    }
}
