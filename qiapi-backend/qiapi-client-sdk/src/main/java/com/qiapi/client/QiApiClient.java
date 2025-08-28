package com.qiapi.client;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.qiapi.config.ApiConfigManager;
import com.qiapi.model.*;
import com.qiapi.request.ApiRequestBuilder;
import com.qiapi.request.RequestBuilder;
import com.qiapi.response.ResponseHandler;
import com.qiapi.service.ApiService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * QiAPI多功能客户端
 * 支持动态调用各种第三方API接口
 * 
 * @author zhexueqi
 * @since 2024/8/2 17:27
 */
@Slf4j
public class QiApiClient implements ApiService {

    private final String accessKey;
    private final String secretKey;
    private final ApiConfigManager apiConfigManager;
    private final RequestBuilder requestBuilder;
    private final ResponseHandler responseHandler;
    /**
     * 平台URL
     */
    private final String platformUrl;

    public QiApiClient(String accessKey, String secretKey,
            ApiConfigManager apiConfigManager,
            RequestBuilder requestBuilder,
            ResponseHandler responseHandler) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.apiConfigManager = apiConfigManager;
        this.requestBuilder = requestBuilder;
        this.responseHandler = responseHandler;
        // 默认平台URL
        this.platformUrl = "http://localhost:8101/";
    }

    public QiApiClient(String accessKey, String secretKey,
            ApiConfigManager apiConfigManager,
            RequestBuilder requestBuilder,
            ResponseHandler responseHandler, String platformUrl) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.apiConfigManager = apiConfigManager;
        this.requestBuilder = requestBuilder;
        this.responseHandler = responseHandler;
        this.platformUrl = platformUrl;
    }

    /**
     * 创建API调用请求构建器
     * 
     * @param apiId      API唯一标识
     * @param targetType 返回数据类型
     * @param <T>        泛型类型
     * @return ApiRequestBuilder
     */
    @Override
    public <T> ApiRequestBuilder<T> call(String apiId, Class<T> targetType) {
        return new ApiRequestBuilder<>(new ApiRequest(apiId), targetType, this);
    }

    /**
     * 通用API调用方法
     * 
     * @param apiId      API唯一标识
     * @param params     请求参数
     * @param targetType 返回数据类型
     * @param <T>        泛型类型
     * @return API响应
     */
    public <T> ApiResponse<T> callApi(String apiId, Map<String, Object> params, Class<T> targetType) {
        ApiRequest apiRequest = new ApiRequest(apiId);
        apiRequest.setParams(params);
        apiRequest.setBody(params);
        apiRequest.setAccessKey(accessKey);
        apiRequest.setSecretKey(secretKey);

        return callApi(apiRequest, targetType);
    }

    /**
     * 通用API调用方法（完整版）
     * 
     * @param apiRequest API请求对象
     * @param targetType 返回数据类型
     * @param <T>        泛型类型
     * @return API响应
     */
    public <T> ApiResponse<T> callApi(ApiRequest apiRequest, Class<T> targetType) {
        try {
            // 1. 获取API配置
            ApiConfig apiConfig = apiConfigManager.getApiConfig(apiRequest.getApiId());

            // 如果本地没有找到配置，尝试从远程平台获取
            if (apiConfig == null) {
                apiConfig = apiConfigManager.getApiConfigFromPlatform(apiRequest.getApiId(), platformUrl);
            }

            if (apiConfig == null) {
                return ApiResponse.error(404, "API_NOT_FOUND", "API不存在: " + apiRequest.getApiId());
            }

            // 2. 检查API是否可用
            if (!apiConfigManager.isApiAvailable(apiRequest.getApiId())) {
                return ApiResponse.error(503, "API_UNAVAILABLE", "API不可用: " + apiRequest.getApiId());
            }

            // 3. 设置默认的accessKey和secretKey
            if (apiRequest.getAccessKey() == null) {
                apiRequest.setAccessKey(accessKey);
            }
            if (apiRequest.getSecretKey() == null) {
                apiRequest.setSecretKey(secretKey);
            }

            // 4. 构建HTTP请求
            HttpRequest httpRequest = requestBuilder.buildRequest(apiConfig, apiRequest);

            // 5. 执行请求
            log.info("调用API: {}, URL: {}", apiRequest.getApiId(), httpRequest.getUrl());
            HttpResponse httpResponse = httpRequest.execute();

            // 6. 处理响应
            return responseHandler.handleResponse(httpResponse, apiConfig, targetType);

        } catch (Exception e) {
            log.error("API调用异常: {}", apiRequest.getApiId(), e);
            return ApiResponse.error(500, "INTERNAL_ERROR", "API调用异常: " + e.getMessage());
        }
    }

    /**
     * 获取所有可用的API列表
     * 
     * @return API配置列表
     */
    @Override
    public List<ApiConfig> getAvailableApis() {
        return apiConfigManager.getAllApiConfigs();
    }

    /**
     * 刷新API配置缓存
     */
    @Override
    public void refreshApiConfigs() {
        // 对于默认实现，这里可以重新加载API配置
        // 在更复杂的实现中，可能需要从远程服务器重新获取配置
        log.info("刷新API配置缓存");
    }

    /**
     * 根据分类获取API列表
     * 
     * @param category 分类名称
     * @return API配置列表
     */
    public List<ApiConfig> getApisByCategory(String category) {
        return apiConfigManager.getApiConfigsByCategory(category);
    }

    /**
     * 获取API配置管理器
     * 
     * @return API配置管理器
     */
    @Override
    public ApiConfigManager getApiConfigManager() {
        return apiConfigManager;
    }

    /**
     * 获取请求构建器
     * 
     * @return 请求构建器
     */
    public RequestBuilder getRequestBuilder() {
        return requestBuilder;
    }

    /**
     * 获取响应处理器
     * 
     * @return 响应处理器
     */
    public ResponseHandler getResponseHandler() {
        return responseHandler;
    }

    /**
     * 获取平台URL
     * 
     * @return 平台URL
     */
    public String getPlatformUrl() {
        return platformUrl;
    }

    // ========== 兼容性方法（保持向后兼容） ==========

    /**
     * 获取用户名（GET方式）
     * 
     * @deprecated 建议使用通用的callApi方法
     */
    @Deprecated
    public String getName(String name) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);

        ApiResponse<String> response = callApi("name.get", params, String.class);
        return response.isSuccess() ? response.getData() : response.getMessage();
    }

    /**
     * 获取用户名（POST方式）
     * 
     * @deprecated 建议使用通用的callApi方法
     */
    @Deprecated
    public String getNameByPost(String name) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);

        ApiResponse<String> response = callApi("name.post", params, String.class);
        return response.isSuccess() ? response.getData() : response.getMessage();
    }

    /**
     * RESTful方式获取用户名
     * 
     * @deprecated 建议使用通用的callApi方法
     */
    @Deprecated
    public String getNameByRestful(User user) {
        ApiRequest apiRequest = new ApiRequest("name.restful");
        apiRequest.setBody(user);
        apiRequest.setAccessKey(accessKey);
        apiRequest.setSecretKey(secretKey);

        ApiResponse<String> response = callApi(apiRequest, String.class);
        return response.isSuccess() ? response.getData() : response.getMessage();
    }
}