package com.qiapi.config;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
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
    private long cacheExpireTime = 300; // 默认5分钟过期
    private String defaultPlatformUrl = "https://api.qiapi.com"; // 默认平台URL
    private final List<ConfigChangeListener> listeners = new ArrayList<>();
    
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
    public ApiConfig getApiConfigFromPlatform(String apiId, String platformUrl) {
        // 首先检查本地缓存
        ApiConfig cachedConfig = getCachedApiConfig(apiId);
        if (cachedConfig != null) {
            return cachedConfig;
        }
        
        try {
            // 优先尝试使用SDK专用接口获取配置
            String sdkConfigUrl = platformUrl + "/api/interfaceInfo/config/" + apiId;
            if (platformUrl.endsWith("/")) {
                sdkConfigUrl = platformUrl + "api/interfaceInfo/config/" + apiId;
            }
            
            // 发送HTTP请求获取配置
            HttpResponse response = HttpRequest.get(sdkConfigUrl).execute();
            if (response.isOk()) {
                String responseBody = response.body();
                // 解析响应体为ApiConfig对象
                ApiConfig config = parseApiConfigFromSdkResponse(responseBody, apiId);
                if (config != null) {
                    // 添加到本地缓存
                    addApiConfig(config);
                    log.info("从平台SDK接口加载API配置成功: {}", sdkConfigUrl);
                    return config;
                }
            }
            
            // 如果SDK专用接口失败，尝试使用通用接口
            String configUrl = platformUrl + "/api/config/interface/apiId/" + apiId;
            if (platformUrl.endsWith("/")) {
                configUrl = platformUrl + "api/config/interface/apiId/" + apiId;
            }
            
            // 发送HTTP请求获取配置
            response = HttpRequest.get(configUrl).execute();
            if (response.isOk()) {
                String responseBody = response.body();
                // 解析响应体为ApiConfig对象
                ApiConfig config = parseApiConfigFromPlatformResponse(responseBody, apiId);
                if (config != null) {
                    // 添加到本地缓存
                    addApiConfig(config);
                    log.info("从平台通用接口加载API配置成功: {}", configUrl);
                    return config;
                } else {
                    log.error("解析API配置失败: {}", responseBody);
                    return null;
                }
            } else {
                log.error("从平台加载API配置失败，状态码: {}", response.getStatus());
                return null;
            }
        } catch (Exception e) {
            log.error("从平台加载API配置异常", e);
            return null;
        }
    }

    /**
     * 从响应体解析API配置
     * 
     * @param responseBody 响应体
     * @param apiId API ID
     * @return ApiConfig对象
     */
    private ApiConfig parseApiConfigFromResponse(String responseBody, String apiId) {
        try {
            // 这里应该解析JSON响应体
            // 为了简化示例，我们创建一个简单的配置
            ApiConfig config = new ApiConfig();
            config.setApiId(apiId);
            config.setApiName("Dynamic API: " + apiId);
            config.setDescription("Dynamically loaded API configuration");
            config.setBaseUrl("http://api.example.com");
            config.setPath("/" + apiId);
            config.setMethod("GET");
            config.setRequireAuth(true);
            config.setAuthType("SIGNATURE");
            config.setResponseFormat("JSON");
            config.setStatus("ACTIVE");
            config.setVersion("1.0");
            
            return config;
        } catch (Exception e) {
            log.error("解析API配置失败", e);
            return null;
        }
    }

    /**
     * 从平台响应体解析API配置
     * 
     * @param responseBody 响应体
     * @param apiId API ID
     * @return ApiConfig对象
     */
    private ApiConfig parseApiConfigFromPlatformResponse(String responseBody, String apiId) {
        try {
            // 解析平台返回的JSON格式响应
            // 假设平台返回的格式为: {"code": 0, "data": {...}, "message": "success"}
            // 其中data字段包含InterfaceInfo对象
            JSONObject jsonObject = JSONUtil.parseObj(responseBody);
            
            if (jsonObject.containsKey("code") && jsonObject.getInt("code") == 0) {
                if (jsonObject.containsKey("data") && !jsonObject.isNull("data")) {
                    JSONObject dataObject = jsonObject.getJSONObject("data");
                    
                    // 创建ApiConfig对象并填充数据
                    ApiConfig config = new ApiConfig();
                    config.setApiId(apiId);
                    
                    // 从InterfaceInfo对象中提取信息
                    if (dataObject.containsKey("name")) {
                        config.setApiName(dataObject.getStr("name"));
                    } else {
                        config.setApiName("Dynamic API: " + apiId);
                    }
                    
                    if (dataObject.containsKey("description")) {
                        config.setDescription(dataObject.getStr("description"));
                    } else {
                        config.setDescription("Dynamically loaded API configuration");
                    }
                    
                    if (dataObject.containsKey("url")) {
                        String fullUrl = dataObject.getStr("url");
                        // 解析URL获取baseUrl和path
                        try {
                            java.net.URL url = new java.net.URL(fullUrl);
                            String baseUrl = url.getProtocol() + "://" + url.getHost();
                            if (url.getPort() != -1) {
                                baseUrl += ":" + url.getPort();
                            }
                            config.setBaseUrl(baseUrl);
                            config.setPath(url.getPath());
                        } catch (java.net.MalformedURLException e) {
                            log.warn("解析URL失败，使用完整URL: {}", fullUrl);
                            // 如果解析失败，使用默认值
                            config.setBaseUrl("http://api.example.com");
                            config.setPath("/" + apiId);
                        }
                    } else {
                        config.setBaseUrl("http://api.example.com");
                        config.setPath("/" + apiId);
                    }
                    
                    if (dataObject.containsKey("method")) {
                        config.setMethod(dataObject.getStr("method").toUpperCase());
                    } else {
                        config.setMethod("GET");
                    }
                    
                    config.setRequireAuth(true);
                    config.setAuthType("SIGNATURE");
                    config.setResponseFormat("JSON");
                    
                    if (dataObject.containsKey("status")) {
                        int status = dataObject.getInt("status");
                        config.setStatus(status == 1 ? "ACTIVE" : "INACTIVE");
                    } else {
                        config.setStatus("ACTIVE");
                    }
                    
                    config.setVersion("1.0");
                    
                    // 设置默认请求头
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    config.setHeaders(headers);
                    
                    // 根据方法设置参数类型
                    if ("GET".equalsIgnoreCase(config.getMethod())) {
                        config.setParamType("QUERY");
                    } else {
                        config.setParamType("BODY");
                    }
                    
                    return config;
                }
            }
            return null;
        } catch (Exception e) {
            log.error("解析平台API配置失败", e);
            return null;
        }
    }

    /**
     * 从SDK专用接口响应体解析API配置
     * 
     * @param responseBody 响应体
     * @param apiId API ID
     * @return ApiConfig对象
     */
    private ApiConfig parseApiConfigFromSdkResponse(String responseBody, String apiId) {
        try {
            // 解析SDK专用接口返回的JSON格式响应
            // 格式为: {"code": 0, "data": {...}, "message": "success"}
            // 其中data字段包含简化版的接口配置信息
            JSONObject jsonObject = JSONUtil.parseObj(responseBody);
            
            if (jsonObject.containsKey("code") && jsonObject.getInt("code") == 0) {
                if (jsonObject.containsKey("data") && !jsonObject.isNull("data")) {
                    JSONObject dataObject = jsonObject.getJSONObject("data");
                    
                    // 创建ApiConfig对象并填充数据
                    ApiConfig config = new ApiConfig();
                    config.setApiId(apiId);
                    
                    // 从简化版配置信息中提取信息
                    if (dataObject.containsKey("name")) {
                        config.setApiName(dataObject.getStr("name"));
                    } else {
                        config.setApiName("Dynamic API: " + apiId);
                    }
                    
                    if (dataObject.containsKey("description")) {
                        config.setDescription(dataObject.getStr("description"));
                    } else {
                        config.setDescription("Dynamically loaded API configuration");
                    }
                    
                    if (dataObject.containsKey("url")) {
                        String fullUrl = dataObject.getStr("url");
                        // 解析URL获取baseUrl和path
                        try {
                            java.net.URL url = new java.net.URL(fullUrl);
                            String baseUrl = url.getProtocol() + "://" + url.getHost();
                            if (url.getPort() != -1) {
                                baseUrl += ":" + url.getPort();
                            }
                            config.setBaseUrl(baseUrl);
                            config.setPath(url.getPath());
                        } catch (java.net.MalformedURLException e) {
                            log.warn("解析URL失败，使用完整URL: {}", fullUrl);
                            // 如果解析失败，使用默认值
                            config.setBaseUrl("http://api.example.com");
                            config.setPath("/" + apiId);
                        }
                    } else {
                        config.setBaseUrl("http://api.example.com");
                        config.setPath("/" + apiId);
                    }
                    
                    if (dataObject.containsKey("method")) {
                        config.setMethod(dataObject.getStr("method").toUpperCase());
                    } else {
                        config.setMethod("GET");
                    }
                    
                    config.setRequireAuth(true);
                    config.setAuthType("SIGNATURE");
                    config.setResponseFormat("JSON");
                    
                    if (dataObject.containsKey("status")) {
                        int status = dataObject.getInt("status");
                        config.setStatus(status == 1 ? "ACTIVE" : "INACTIVE");
                    } else {
                        config.setStatus("ACTIVE");
                    }
                    
                    config.setVersion("1.0");
                    
                    // 设置默认请求头
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    config.setHeaders(headers);
                    
                    // 根据方法设置参数类型
                    if ("GET".equalsIgnoreCase(config.getMethod())) {
                        config.setParamType("QUERY");
                    } else {
                        config.setParamType("BODY");
                    }
                    
                    return config;
                }
            }
            return null;
        } catch (Exception e) {
            log.error("解析SDK API配置失败", e);
            return null;
        }
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
            
            // 通知监听器
            notifyConfigChange(apiConfig.getApiId(), apiConfig);
        }
    }
    
    @Override
    public void removeApiConfig(String apiId) {
        if (apiId != null) {
            ApiConfig removedConfig = apiConfigs.remove(apiId);
            if (removedConfig != null) {
                log.info("API配置已删除: {}", apiId);
                
                // 通知监听器
                notifyConfigChange(apiId, null);
            }
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
    
    @Override
    public List<ApiConfig> loadApiConfigsFromPlatform(String platformUrl) {
        try {
            // 构造获取API配置的URL
            String configUrl = platformUrl + "/api/config/list";
            if (platformUrl.endsWith("/")) {
                configUrl = platformUrl + "api/config/list";
            }
            
            // 发送HTTP请求获取配置
            HttpResponse response = HttpRequest.get(configUrl).execute();
            if (response.isOk()) {
                String responseBody = response.body();
                // 这里应该解析响应体为ApiConfig列表
                // 为了简化示例，我们返回空列表
                log.info("从平台加载API配置成功: {}", configUrl);
                return new ArrayList<>();
            } else {
                log.error("从平台加载API配置失败，状态码: {}", response.getStatus());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("从平台加载API配置异常", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public void setCacheExpireTime(long expireTime) {
        this.cacheExpireTime = expireTime;
    }
    
    @Override
    public ApiConfig getCachedApiConfig(String apiId) {
        return getApiConfig(apiId);
    }
    
    @Override
    public void addConfigChangeListener(ConfigChangeListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }
    
    @Override
    public void setDefaultPlatformUrl(String platformUrl) {
        this.defaultPlatformUrl = platformUrl;
    }
    
    /**
     * 通知配置变更
     * 
     * @param apiId API标识
     * @param newConfig 新配置
     */
    private void notifyConfigChange(String apiId, ApiConfig newConfig) {
        for (ConfigChangeListener listener : listeners) {
            try {
                listener.onConfigChanged(apiId, newConfig);
            } catch (Exception e) {
                log.error("通知配置变更监听器时发生异常", e);
            }
        }
    }
}






