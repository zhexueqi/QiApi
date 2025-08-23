package com.qiapi.response;

import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.qiapi.model.ApiConfig;
import com.qiapi.model.ApiResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 默认响应处理器实现
 * 
 * @author zhexueqi
 */
@Slf4j
public class DefaultResponseHandler implements ResponseHandler {
    
    @Override
    public <T> ApiResponse<T> handleResponse(HttpResponse httpResponse, ApiConfig apiConfig, Class<T> targetType) {
        ApiResponse<T> apiResponse = new ApiResponse<>();
        
        try {
            // 设置基本信息
            apiResponse.setStatusCode(httpResponse.getStatus());
            apiResponse.setRawResponse(httpResponse.body());
            
            // 设置响应头
            Map<String, String> headers = new HashMap<>();
            httpResponse.headers().forEach((key, values) -> {
                if (!values.isEmpty()) {
                    headers.put(key, values.get(0));
                }
            });
            apiResponse.setHeaders(headers);
            
            // 判断是否成功
            boolean isSuccess = httpResponse.isOk();
            apiResponse.setSuccess(isSuccess);
            
            if (isSuccess) {
                // 解析响应数据
                String responseBody = httpResponse.body();
                T data = parseResponseData(responseBody, apiConfig.getResponseFormat(), targetType);
                apiResponse.setData(data);
                apiResponse.setMessage("success");
            } else {
                // 处理错误响应
                apiResponse.setMessage("API调用失败");
                apiResponse.setErrorCode("API_ERROR");
                log.error("API调用失败，状态码: {}, 响应: {}", httpResponse.getStatus(), httpResponse.body());
            }
            
        } catch (Exception e) {
            log.error("处理API响应时发生异常", e);
            apiResponse.setSuccess(false);
            apiResponse.setStatusCode(500);
            apiResponse.setMessage("响应处理异常: " + e.getMessage());
            apiResponse.setErrorCode("RESPONSE_PROCESS_ERROR");
        }
        
        return apiResponse;
    }
    
    @Override
    public <T> T parseResponseData(String responseBody, String responseFormat, Class<T> targetType) {
        if (responseBody == null || responseBody.trim().isEmpty()) {
            return null;
        }
        
        try {
            if ("JSON".equals(responseFormat)) {
                return parseJsonResponse(responseBody, targetType);
            } else if ("TEXT".equals(responseFormat) || "PLAIN".equals(responseFormat)) {
                return parseTextResponse(responseBody, targetType);
            } else if ("XML".equals(responseFormat)) {
                return parseXmlResponse(responseBody, targetType);
            } else {
                // 默认按文本处理
                return parseTextResponse(responseBody, targetType);
            }
        } catch (Exception e) {
            log.error("解析响应数据失败，格式: {}, 内容: {}", responseFormat, responseBody, e);
            throw new RuntimeException("解析响应数据失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 解析JSON响应
     */
    @SuppressWarnings("unchecked")
    private <T> T parseJsonResponse(String responseBody, Class<T> targetType) {
        if (targetType == String.class) {
            return (T) responseBody;
        } else if (targetType == Object.class) {
            return (T) JSONUtil.parse(responseBody);
        } else {
            return JSONUtil.toBean(responseBody, targetType);
        }
    }
    
    /**
     * 解析文本响应
     */
    @SuppressWarnings("unchecked")
    private <T> T parseTextResponse(String responseBody, Class<T> targetType) {
        if (targetType == String.class) {
            return (T) responseBody;
        } else {
            // 如果目标类型不是String，尝试进行类型转换
            try {
                if (targetType == Integer.class || targetType == int.class) {
                    return (T) Integer.valueOf(responseBody.trim());
                } else if (targetType == Long.class || targetType == long.class) {
                    return (T) Long.valueOf(responseBody.trim());
                } else if (targetType == Double.class || targetType == double.class) {
                    return (T) Double.valueOf(responseBody.trim());
                } else if (targetType == Boolean.class || targetType == boolean.class) {
                    return (T) Boolean.valueOf(responseBody.trim());
                } else {
                    return (T) responseBody;
                }
            } catch (NumberFormatException e) {
                log.warn("文本转换为{}类型失败，返回原始字符串", targetType.getSimpleName());
                return (T) responseBody;
            }
        }
    }
    
    /**
     * 解析XML响应
     */
    @SuppressWarnings("unchecked")
    private <T> T parseXmlResponse(String responseBody, Class<T> targetType) {
        // 这里可以根据需要实现XML解析逻辑
        // 暂时返回原始字符串
        log.warn("XML解析暂未实现，返回原始字符串");
        return (T) responseBody;
    }
}