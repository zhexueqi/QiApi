package com.qiapi.project.controller;

import com.qiapi.project.common.BaseResponse;
import com.qiapi.project.common.ResultUtils;
import com.qiapi.project.service.InterfaceInfoService;
import com.qiapi.qiapicommon.model.entity.InterfaceInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * API配置控制器
 * 用于提供API配置信息给SDK客户端
 *
 * @author zhexueqi
 */
@RestController
@RequestMapping("/api/config")
@Slf4j
public class ApiConfigController {

    @Resource
    private InterfaceInfoService interfaceInfoService;

    /**
     * 根据接口ID获取接口配置信息
     *
     * @param id 接口ID
     * @return 接口配置信息
     */
    @GetMapping("/interface/{id}")
    public BaseResponse<InterfaceInfo> getInterfaceConfigById(@PathVariable long id) {
        try {
            InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
            if (interfaceInfo == null) {
                return ResultUtils.error(404, "接口不存在");
            }
            return ResultUtils.success(interfaceInfo);
        } catch (Exception e) {
            log.error("获取接口配置信息失败", e);
            return ResultUtils.error(500, "获取接口配置信息失败: " + e.getMessage());
        }
    }

    /**
     * 根据apiId获取接口配置信息
     *
     * @param apiId API ID
     * @return 接口配置信息
     */
    @GetMapping("/interface/apiId/{apiId}")
    public BaseResponse<InterfaceInfo> getInterfaceConfigByApiId(@PathVariable String apiId) {
        try {
            if (apiId == null || apiId.isEmpty()) {
                return ResultUtils.error(400, "apiId不能为空");
            }
            
            // 根据apiId查找对应的接口信息
            long id = 0;
            if (apiId.startsWith("dynamic.")) {
                // 动态API格式: dynamic.{id}
                id = Long.parseLong(apiId.substring(8));
            } else if (apiId.matches("\\d+")) {
                // 纯数字格式
                id = Long.parseLong(apiId);
            } else {
                // 其他预置API格式，需要特殊处理
                // 这里可以根据实际需求进行映射
                return ResultUtils.error(400, "不支持的apiId格式");
            }
            
            InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
            if (interfaceInfo == null) {
                return ResultUtils.error(404, "接口不存在");
            }
            return ResultUtils.success(interfaceInfo);
        } catch (NumberFormatException e) {
            return ResultUtils.error(400, "无效的apiId格式");
        } catch (Exception e) {
            log.error("获取接口配置信息失败", e);
            return ResultUtils.error(500, "获取接口配置信息失败: " + e.getMessage());
        }
    }

    /**
     * 根据apiId获取简化版接口配置信息（专为SDK设计）
     *
     * @param apiId API ID
     * @return 简化版接口配置信息
     */
    @GetMapping("/sdk/interface/{apiId}")
    public BaseResponse<Map<String, Object>> getSdkInterfaceConfig(@PathVariable String apiId) {
        try {
            if (apiId == null || apiId.isEmpty()) {
                return ResultUtils.error(400, "apiId不能为空");
            }
            
            // 根据apiId查找对应的接口信息
            long id = 0;
            if (apiId.startsWith("dynamic.")) {
                // 动态API格式: dynamic.{id}
                id = Long.parseLong(apiId.substring(8));
            } else if (apiId.matches("\\d+")) {
                // 纯数字格式
                id = Long.parseLong(apiId);
            } else {
                // 其他预置API格式，需要特殊处理
                // 这里可以根据实际需求进行映射
                return ResultUtils.error(400, "不支持的apiId格式");
            }
            
            InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
            if (interfaceInfo == null) {
                return ResultUtils.error(404, "接口不存在");
            }
            
            // 构造SDK需要的简化配置信息
            Map<String, Object> config = new HashMap<>();
            config.put("apiId", apiId);
            config.put("name", interfaceInfo.getName());
            config.put("description", interfaceInfo.getDescription());
            config.put("url", interfaceInfo.getUrl());
            config.put("method", interfaceInfo.getMethod());
            config.put("status", interfaceInfo.getStatus());
            
            return ResultUtils.success(config);
        } catch (NumberFormatException e) {
            return ResultUtils.error(400, "无效的apiId格式");
        } catch (Exception e) {
            log.error("获取SDK接口配置信息失败", e);
            return ResultUtils.error(500, "获取SDK接口配置信息失败: " + e.getMessage());
        }
    }
}