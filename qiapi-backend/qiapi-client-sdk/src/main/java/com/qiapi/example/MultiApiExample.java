package com.qiapi.example;

import com.qiapi.client.QiApiClient;
import com.qiapi.model.ApiConfig;
import com.qiapi.model.ApiRequest;
import com.qiapi.model.ApiResponse;
import com.qiapi.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 多API使用示例
 * 展示如何使用新的QiApiClient调用各种不同的API
 * 
 * @author zhexueqi
 */
@Component
public class MultiApiExample {
    
    @Autowired
    private QiApiClient qiApiClient;
    
    /**
     * 示例1: 使用简单方式调用API
     */
    public void example1_SimpleApiCall() {
        System.out.println("=== 示例1: 简单API调用 ===");
        
        // 调用获取用户名API
        Map<String, Object> params = new HashMap<>();
        params.put("name", "zhexueqi");
        
        ApiResponse<String> response = qiApiClient.callApi("name.get", params, String.class);
        
        if (response.isSuccess()) {
            System.out.println("API调用成功: " + response.getData());
        } else {
            System.out.println("API调用失败: " + response.getMessage());
        }
    }
    
    /**
     * 示例2: 使用完整API请求对象
     */
    public void example2_FullApiRequest() {
        System.out.println("=== 示例2: 完整API请求 ===");
        
        // 创建API请求对象
        ApiRequest apiRequest = new ApiRequest("name.restful");
        
        // 设置请求体
        User user = new User();
        user.setName("zhexueqi");
        apiRequest.setBody(user);
        
        // 设置自定义请求头
        Map<String, String> headers = new HashMap<>();
        headers.put("Custom-Header", "CustomValue");
        apiRequest.setHeaders(headers);
        
        // 设置超时时间
        apiRequest.setTimeout(10000);
        
        ApiResponse<String> response = qiApiClient.callApi(apiRequest, String.class);
        
        if (response.isSuccess()) {
            System.out.println("RESTful API调用成功: " + response.getData());
        } else {
            System.out.println("RESTful API调用失败: " + response.getMessage());
        }
    }
    
    /**
     * 示例3: 调用天气API
     */
    public void example3_WeatherApi() {
        System.out.println("=== 示例3: 天气API调用 ===");
        
        Map<String, Object> params = new HashMap<>();
        params.put("city", "北京");
        
        ApiResponse<Object> response = qiApiClient.callApi("weather.current", params, Object.class);
        
        if (response.isSuccess()) {
            System.out.println("天气API调用成功: " + response.getData());
        } else {
            System.out.println("天气API调用失败: " + response.getMessage());
        }
    }
    
    /**
     * 示例4: 获取可用API列表
     */
    public void example4_ListAvailableApis() {
        System.out.println("=== 示例4: 获取可用API列表 ===");
        
        List<ApiConfig> apis = qiApiClient.getAvailableApis();
        
        System.out.println("可用API列表:");
        for (ApiConfig api : apis) {
            System.out.printf("- %s: %s (%s %s)%n", 
                api.getApiId(), 
                api.getApiName(), 
                api.getMethod(), 
                api.getPath()
            );
        }
    }
    
    /**
     * 示例5: 根据分类获取API
     */
    public void example5_GetApisByCategory() {
        System.out.println("=== 示例5: 根据分类获取API ===");
        
        List<ApiConfig> nameApis = qiApiClient.getApisByCategory("用户");
        
        System.out.println("用户相关API:");
        for (ApiConfig api : nameApis) {
            System.out.printf("- %s: %s%n", api.getApiId(), api.getApiName());
        }
    }
    
    /**
     * 运行所有示例
     */
    public void runAllExamples() {
        example1_SimpleApiCall();
        System.out.println();
        
        example2_FullApiRequest();
        System.out.println();
        
        example3_WeatherApi();
        System.out.println();
        
        example4_ListAvailableApis();
        System.out.println();
        
        example5_GetApisByCategory();
    }
}