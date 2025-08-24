package com.qiapi.project.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.qiapi.client.QiApiClient;
import com.qiapi.project.annotation.AuthCheck;
import com.qiapi.project.common.*;
import com.qiapi.project.constant.UserConstant;
import com.qiapi.project.exception.BusinessException;
import com.qiapi.project.exception.ThrowUtils;
import com.qiapi.project.model.dto.interfaceinfo.InterfaceInfoAddRequest;
import com.qiapi.project.model.dto.interfaceinfo.InterfaceInfoInvokeRequest;
import com.qiapi.project.model.dto.interfaceinfo.InterfaceInfoQueryRequest;
import com.qiapi.project.model.dto.interfaceinfo.InterfaceInfoUpdateRequest;
import com.qiapi.project.model.vo.InterfaceInfoVO;
import com.qiapi.project.service.CreditService;
import com.qiapi.project.service.InterfaceInfoService;
import com.qiapi.project.service.UserService;
import com.qiapi.qiapicommon.model.entity.InterfaceInfo;
import com.qiapi.qiapicommon.model.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import com.qiapi.service.UserInterfaceInfoService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 接口信息控制层
 *
 * @author zhexueqi
 */
@RestController
@RequestMapping("/interfaceInfo")
@Slf4j
public class InterfaceInfoController {

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Resource
    private UserService userService;

    @Resource
    private QiApiClient qiApiClient;
    @Resource
    private CreditService creditService;


    /**
     * 创建
     *
     * @param interfaceInfoAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addInterfaceInfo(@RequestBody InterfaceInfoAddRequest interfaceInfoAddRequest, HttpServletRequest request) {
        if (interfaceInfoAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoAddRequest, interfaceInfo);
        //参数校验
        interfaceInfoService.validInterfaceInfo(interfaceInfo, true);
        User loginUser = userService.getLoginUser(request);
        interfaceInfo.setUserId(loginUser.getId());
        boolean result = interfaceInfoService.save(interfaceInfo);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newInterfaceInfoId = interfaceInfo.getId();
        return ResultUtils.success(newInterfaceInfoId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteInterfaceInfo(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        ThrowUtils.throwIf(oldInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldInterfaceInfo.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = interfaceInfoService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param interfaceInfoUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateInterfaceInfo(@RequestBody InterfaceInfoUpdateRequest interfaceInfoUpdateRequest) {
        if (interfaceInfoUpdateRequest == null || interfaceInfoUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoUpdateRequest, interfaceInfo);
        // 参数校验
        interfaceInfoService.validInterfaceInfo(interfaceInfo, false);
        long id = interfaceInfoUpdateRequest.getId();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        ThrowUtils.throwIf(oldInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = interfaceInfoService.updateById(interfaceInfo);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<InterfaceInfoVO> getInterfaceInfoVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(interfaceInfoService.getInterfaceInfoVO(interfaceInfo, request));
    }

    /**
     * 分页获取列表（仅管理员）
     *
     * @param interfaceInfoQueryRequest
     * @return
     */
    @GetMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<InterfaceInfo>> listInterfaceInfoByPage(InterfaceInfoQueryRequest interfaceInfoQueryRequest) {
        long current = interfaceInfoQueryRequest.getCurrent();
        long size = interfaceInfoQueryRequest.getPageSize();
        Page<InterfaceInfo> interfaceInfoPage = interfaceInfoService.page(new Page<>(current, size),
                interfaceInfoService.getQueryWrapper(interfaceInfoQueryRequest));
        return ResultUtils.success(interfaceInfoPage);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param interfaceInfoQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list/page/vo")
    public BaseResponse<Page<InterfaceInfoVO>> listInterfaceInfoVOByPage(InterfaceInfoQueryRequest interfaceInfoQueryRequest,
                                                                         HttpServletRequest request) {
        long current = interfaceInfoQueryRequest.getCurrent();
        long size = interfaceInfoQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<InterfaceInfo> interfaceInfoPage = interfaceInfoService.page(new Page<>(current, size),
                interfaceInfoService.getQueryWrapper(interfaceInfoQueryRequest));
        return ResultUtils.success(interfaceInfoService.getInterfaceInfoVOPage(interfaceInfoPage, request));
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param interfaceInfoQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<InterfaceInfoVO>> listMyInterfaceInfoVOByPage(@RequestBody InterfaceInfoQueryRequest interfaceInfoQueryRequest,
                                                                           HttpServletRequest request) {
        if (interfaceInfoQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        interfaceInfoQueryRequest.setUserId(loginUser.getId());
        long current = interfaceInfoQueryRequest.getCurrent();
        long size = interfaceInfoQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<InterfaceInfo> interfaceInfoPage = interfaceInfoService.page(new Page<>(current, size),
                interfaceInfoService.getQueryWrapper(interfaceInfoQueryRequest));
        return ResultUtils.success(interfaceInfoService.getInterfaceInfoVOPage(interfaceInfoPage, request));
    }

    /**
     * 发布接口(管理员操作)
     */
    @PostMapping("/online")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> onlineInterfaceInfo(@RequestBody IdRequest idRequest, HttpServletRequest request) {
        if (idRequest == null || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = idRequest.getId();
        boolean result = interfaceInfoService.onlineInterfaceInfo(id, request);
        return ResultUtils.success(result);
    }

    /**
     * 下线接口(管理员操作)
     */
    @PostMapping("/offline")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> offlineInterfaceInfo(@RequestBody IdRequest idRequest, HttpServletRequest request) {
        if (idRequest == null || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = idRequest.getId();
        boolean res = interfaceInfoService.offOnline(id, request);
        return ResultUtils.success(res);
    }

    /**
     * 调用接口
     */
    @PostMapping("/invoke")
    public BaseResponse<Object> invokeInterfaceInfo(@RequestBody InterfaceInfoInvokeRequest interfaceInfoInvokeRequest, HttpServletRequest request) {
        if (interfaceInfoInvokeRequest == null || interfaceInfoInvokeRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        long id = interfaceInfoInvokeRequest.getId();

        // 根据接口ID获取接口信息
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "接口不存在");
        }

        // 检查接口状态是否为上线状态
        if (interfaceInfo.getStatus() != 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口未上线");
        }
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 检查额度是否充足
        if (!creditService.checkCreditSufficient(loginUser.getId(), interfaceInfo.getId(), 1L)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "额度不足");
        }

        String accessKey = loginUser.getAccessKey();
        String secretKey = loginUser.getSecretKey();

        try {
            // 根据接口信息动态调用对应的API
            Object result = invokeApiByInterfaceInfo(interfaceInfo, interfaceInfoInvokeRequest.getUserRequestParams(), accessKey, secretKey);
            return ResultUtils.success(result);
        } catch (Exception e) {
            log.error("接口调用失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口调用失败: " + e.getMessage());
        }
    }

    /**
     * 根据接口信息动态调用API
     *
     * @param interfaceInfo     接口信息
     * @param userRequestParams 用户请求参数
     * @param accessKey         访问密钥
     * @param secretKey         秘钥
     * @return 调用结果
     */
    private Object invokeApiByInterfaceInfo(InterfaceInfo interfaceInfo, String userRequestParams, String accessKey, String secretKey) {
        // 根据接口的URL和方法确定对应的API ID
        String apiId = getApiIdByInterface(interfaceInfo);

        if (apiId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的接口类型");
        }

        // 创建新的QiApiClient实例（使用用户的密钥）
        QiApiClient userApiClient = createUserApiClient(accessKey, secretKey);

        // 解析用户请求参数
        Object requestParams = parseUserRequestParams(userRequestParams, apiId);

        // 调用API
        if (requestParams instanceof java.util.Map) {
            // 参数形式调用
            com.qiapi.model.ApiResponse<Object> response = userApiClient.callApi(apiId, (java.util.Map<String, Object>) requestParams, Object.class);
            if (response.isSuccess()) {
                return response.getData();
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "API调用失败: " + response.getMessage());
            }
        } else {
            // 对象形式调用
            com.qiapi.model.ApiRequest apiRequest = new com.qiapi.model.ApiRequest(apiId);
            apiRequest.setBody(requestParams);
            apiRequest.setAccessKey(accessKey);
            apiRequest.setSecretKey(secretKey);

            com.qiapi.model.ApiResponse<Object> response = userApiClient.callApi(apiRequest, Object.class);
            if (response.isSuccess()) {
                return response.getData();
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "API调用失败: " + response.getMessage());
            }
        }
    }

    /**
     * 根据接口信息获取对应的API ID
     */
    private String getApiIdByInterface(InterfaceInfo interfaceInfo) {
        String url = interfaceInfo.getUrl();
        String method = interfaceInfo.getMethod();

        // 对于本地测试的qiapi-interface服务，仍然保持兼容
        // 支持路径匹配和完整URL匹配
        if (isLocalTestInterface(url)) {
            return getLocalTestApiId(url, method);
        }

        // 对于其他第三方API，直接创建动态配置
        return createDynamicApiConfig(interfaceInfo);
    }

    /**
     * 判断是否为本地测试接口
     */
    private boolean isLocalTestInterface(String url) {
        // 如果是本地测试接口（qiapi-interface）
        if (url.startsWith("http://localhost:8081") || url.startsWith("/api/name")) {
            return true;
        }
        return false;
    }

    /**
     * 获取本地测试接口的API ID
     */
    private String getLocalTestApiId(String url, String method) {
        // 提取路径部分
        String path = url;
        if (url.startsWith("http")) {
            try {
                java.net.URL urlObj = new java.net.URL(url);
                path = urlObj.getPath();
            } catch (java.net.MalformedURLException e) {
                // 如果解析失败，使用原始字符串
                path = url;
            }
        }

        // 根据路径和方法匹配预置的API ID
        if ("/api/name".equals(path)) {
            if ("GET".equalsIgnoreCase(method)) {
                return "name.get";
            } else if ("POST".equalsIgnoreCase(method)) {
                return "name.post";
            }
        } else if ("/api/name/restful".equals(path) && "POST".equalsIgnoreCase(method)) {
            return "name.restful";
        }

        // 如果没有匹配的预置接口，返回null让外层创建动态配置
        return null;
    }

    /**
     * 动态创建API配置
     */
    private String createDynamicApiConfig(InterfaceInfo interfaceInfo) {
        String apiId = "dynamic." + interfaceInfo.getId();

        // 创建动态API配置
        com.qiapi.model.ApiConfig apiConfig = new com.qiapi.model.ApiConfig();
        apiConfig.setApiId(apiId);
        apiConfig.setApiName(interfaceInfo.getName());
        apiConfig.setDescription(interfaceInfo.getDescription());

        // 直接使用数据库中存储的完整URL，不需要baseUrl
        // 数据库中的URL应该是完整的第三方API地址，如: http://api.example.com/v1/user
        String fullUrl = interfaceInfo.getUrl();

        // 解析URL获取baseUrl和path
        try {
            java.net.URL url = new java.net.URL(fullUrl);
            String baseUrl = url.getProtocol() + "://" + url.getHost();
            if (url.getPort() != -1) {
                baseUrl += ":" + url.getPort();
            }
            String path = url.getPath();
            if (path == null || path.isEmpty()) {
                path = "/";
            }

            apiConfig.setBaseUrl(baseUrl);
            apiConfig.setPath(path);
        } catch (java.net.MalformedURLException e) {
            // 如果URL格式不正确，抛出异常
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口URL格式不正确: " + fullUrl);
        }

        apiConfig.setMethod(interfaceInfo.getMethod());
        apiConfig.setRequireAuth(true);
        apiConfig.setAuthType("SIGNATURE");
        apiConfig.setResponseFormat("JSON");
        apiConfig.setStatus("ACTIVE");
        apiConfig.setVersion("1.0");

        // 设置参数类型
        if ("GET".equalsIgnoreCase(interfaceInfo.getMethod())) {
            apiConfig.setParamType("QUERY");
        } else {
            apiConfig.setParamType("BODY");
        }

        // 设置默认请求头
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        apiConfig.setHeaders(headers);

        // 添加到配置管理器
        qiApiClient.getApiConfigManager().addApiConfig(apiConfig);

        return apiId;
    }

    /**
     * 解析用户请求参数
     */
    private Object parseUserRequestParams(String userRequestParams, String apiId) {
        if (userRequestParams == null || userRequestParams.trim().isEmpty()) {
            return new java.util.HashMap<String, Object>();
        }

        Gson gson = new Gson();

        try {
            // 如果是name.restful接口，解析为User对象
            if ("name.restful".equals(apiId)) {
                return gson.fromJson(userRequestParams, com.qiapi.model.User.class);
            }

            // 其他情况尝试解析为Map
            java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<java.util.Map<String, Object>>() {
            }.getType();
            return gson.fromJson(userRequestParams, type);
        } catch (Exception e) {
            log.warn("参数解析失败，使用原始字符串: {}", e.getMessage());
            // 如果解析失败，将整个字符串作为单个参数
            java.util.Map<String, Object> params = new java.util.HashMap<>();
            params.put("data", userRequestParams);
            return params;
        }
    }

    /**
     * 创建用户专用的API客户端
     */
    private QiApiClient createUserApiClient(String accessKey, String secretKey) {
        // 使用用户的密钥创建新的客户端实例
        return new QiApiClient(accessKey, secretKey,
                qiApiClient.getApiConfigManager(),
                qiApiClient.getRequestBuilder(),
                qiApiClient.getResponseHandler());
    }

    /**
     * 获取所有可用的API列表
     */
    @GetMapping("/available-apis")
    public BaseResponse<java.util.List<com.qiapi.model.ApiConfig>> getAvailableApis() {
        java.util.List<com.qiapi.model.ApiConfig> apis = qiApiClient.getAvailableApis();
        return ResultUtils.success(apis);
    }

    /**
     * 根据分类获取API列表
     */
    @GetMapping("/apis/category")
    public BaseResponse<java.util.List<com.qiapi.model.ApiConfig>> getApisByCategory(@RequestParam String category) {
        java.util.List<com.qiapi.model.ApiConfig> apis = qiApiClient.getApisByCategory(category);
        return ResultUtils.success(apis);
    }
}
