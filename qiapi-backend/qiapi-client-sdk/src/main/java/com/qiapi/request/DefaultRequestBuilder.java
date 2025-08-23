package com.qiapi.request;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.qiapi.model.ApiConfig;
import com.qiapi.model.ApiRequest;
import com.qiapi.project.utils.SignUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 默认请求构建器实现
 * 
 * @author zhexueqi
 */
@Slf4j
public class DefaultRequestBuilder implements RequestBuilder {
    
    @Override
    public HttpRequest buildRequest(ApiConfig apiConfig, ApiRequest apiRequest) {
        String url = buildUrl(apiConfig, apiRequest);
        HttpRequest request;
        
        String method = apiConfig.getMethod().toUpperCase();
        switch (method) {
            case "GET":
                request = HttpRequest.get(url);
                break;
            case "POST":
                request = HttpRequest.post(url);
                break;
            case "PUT":
                request = HttpRequest.put(url);
                break;
            case "DELETE":
                request = HttpRequest.delete(url);
                break;
            default:
                throw new IllegalArgumentException("不支持的HTTP方法: " + method);
        }
        
        // 设置请求头
        Map<String, String> headers = buildHeaders(apiConfig, apiRequest);
        if (headers != null && !headers.isEmpty()) {
            request.headerMap(headers, true);
        }
        
        // 设置请求体
        if ("POST".equals(method) || "PUT".equals(method)) {
            String body = buildBody(apiConfig, apiRequest);
            if (body != null && !body.isEmpty()) {
                request.body(body);
            }
        }
        
        // 设置超时时间
        if (apiRequest.getTimeout() != null) {
            request.timeout(apiRequest.getTimeout());
        } else {
            request.timeout(30000); // 默认30秒
        }
        
        return request;
    }
    
    @Override
    public String buildUrl(ApiConfig apiConfig, ApiRequest apiRequest) {
        String baseUrl = apiConfig.getBaseUrl();
        String path = apiConfig.getPath();
        
        // 确保baseUrl不以/结尾，path以/开头
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        
        String fullUrl = baseUrl + path;
        
        // 如果是GET请求且有查询参数，添加到URL中
        if ("GET".equals(apiConfig.getMethod()) && apiRequest.getParams() != null) {
            StringBuilder queryString = new StringBuilder();
            for (Map.Entry<String, Object> entry : apiRequest.getParams().entrySet()) {
                if (queryString.length() > 0) {
                    queryString.append("&");
                }
                queryString.append(entry.getKey()).append("=").append(entry.getValue());
            }
            if (queryString.length() > 0) {
                fullUrl += "?" + queryString.toString();
            }
        }
        
        return fullUrl;
    }
    
    @Override
    public Map<String, String> buildHeaders(ApiConfig apiConfig, ApiRequest apiRequest) {
        Map<String, String> headers = new HashMap<>();
        
        // 添加API配置中的默认头
        if (apiConfig.getHeaders() != null) {
            headers.putAll(apiConfig.getHeaders());
        }
        
        // 添加用户自定义头
        if (apiRequest.getHeaders() != null) {
            headers.putAll(apiRequest.getHeaders());
        }
        
        // 如果需要认证，添加认证相关头
        if (apiConfig.isRequireAuth() && "SIGNATURE".equals(apiConfig.getAuthType())) {
            addSignatureHeaders(headers, apiConfig, apiRequest);
        }
        
        return headers;
    }
    
    /**
     * 添加签名认证头
     */
    private void addSignatureHeaders(Map<String, String> headers, ApiConfig apiConfig, ApiRequest apiRequest) {
        String accessKey = apiRequest.getAccessKey();
        String secretKey = apiRequest.getSecretKey();
        
        if (accessKey == null || secretKey == null) {
            log.warn("API需要认证但未提供accessKey或secretKey");
            return;
        }
        
        String nonce = RandomUtil.randomNumbers(5);
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String body = buildBody(apiConfig, apiRequest);
        
        // 构建签名参数
        Map<String, String> signParams = new HashMap<>();
        signParams.put("accessKey", accessKey);
        signParams.put("nonce", nonce);
        signParams.put("timestamp", timestamp);
        signParams.put("body", body != null ? body : "");
        
        String sign = SignUtils.getSign(secretKey, new HashMap<>(signParams));
        
        headers.put("accessKey", accessKey);
        headers.put("nonce", nonce);
        headers.put("timestamp", timestamp);
        headers.put("sign", sign);
        headers.put("body", body != null ? body : "");
    }
    
    @Override
    public String buildBody(ApiConfig apiConfig, ApiRequest apiRequest) {
        String paramType = apiConfig.getParamType();
        
        if ("BODY".equals(paramType)) {
            // JSON格式的请求体
            if (apiRequest.getBody() != null) {
                if (apiRequest.getBody() instanceof String) {
                    return (String) apiRequest.getBody();
                } else {
                    return JSONUtil.toJsonStr(apiRequest.getBody());
                }
            }
        } else if ("FORM".equals(paramType)) {
            // 表单格式的请求体
            if (apiRequest.getParams() != null) {
                StringBuilder formData = new StringBuilder();
                for (Map.Entry<String, Object> entry : apiRequest.getParams().entrySet()) {
                    if (formData.length() > 0) {
                        formData.append("&");
                    }
                    formData.append(entry.getKey()).append("=").append(entry.getValue());
                }
                return formData.toString();
            }
        }
        
        return null;
    }
}