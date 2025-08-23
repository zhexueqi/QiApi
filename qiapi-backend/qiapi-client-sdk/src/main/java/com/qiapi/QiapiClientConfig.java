package com.qiapi;

import com.qiapi.client.QiApiClient;
import com.qiapi.config.ApiConfigManager;
import com.qiapi.config.DefaultApiConfigManager;
import com.qiapi.request.DefaultRequestBuilder;
import com.qiapi.request.RequestBuilder;
import com.qiapi.response.DefaultResponseHandler;
import com.qiapi.response.ResponseHandler;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * QiAPI客户端自动配置类
 * 支持多API平台配置
 * 
 * @author zhexueqi
 * @since 2024/8/2 17:10
 */
@Configuration
@ComponentScan
@Data
@ConfigurationProperties("qiapi.client")
@EnableConfigurationProperties(QiapiClientConfig.class)
public class QiapiClientConfig {
    
    private String accessKey;
    private String secretKey;
    
    /**
     * 平台基础URL，用于获取API配置
     */
    private String platformUrl = "http://localhost:8080";
    
    /**
     * 默认超时时间（毫秒）
     */
    private Integer defaultTimeout = 30000;
    
    /**
     * 是否启用API发现
     */
    private boolean enableApiDiscovery = true;
    
    @Bean
    public QiApiClient getQiApiClient() {
        ApiConfigManager apiConfigManager = apiConfigManager();
        RequestBuilder requestBuilder = requestBuilder();
        ResponseHandler responseHandler = responseHandler();
        
        return new QiApiClient(
            accessKey != null ? accessKey : "default-access-key", 
            secretKey != null ? secretKey : "default-secret-key", 
            apiConfigManager, 
            requestBuilder, 
            responseHandler
        );
    }
    
    @Bean
    public ApiConfigManager apiConfigManager() {
        return new DefaultApiConfigManager();
    }
    
    @Bean
    public RequestBuilder requestBuilder() {
        return new DefaultRequestBuilder();
    }
    
    @Bean
    public ResponseHandler responseHandler() {
        return new DefaultResponseHandler();
    }
}
