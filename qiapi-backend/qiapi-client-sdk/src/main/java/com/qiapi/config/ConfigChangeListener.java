package com.qiapi.config;

import com.qiapi.model.ApiConfig;

/**
 * 配置变更监听器
 * 当API配置发生变更时回调
 * 
 * @author zhexueqi
 */
public interface ConfigChangeListener {
    /**
     * 配置变更回调
     * 
     * @param apiId API标识
     * @param newConfig 新配置
     */
    void onConfigChanged(String apiId, ApiConfig newConfig);
}