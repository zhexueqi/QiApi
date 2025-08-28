# QiAPI Client SDK 优化设计方案

## 1. 概述

### 1.1 当前问题分析
当前的QiAPI Client SDK虽然已经实现了基本的多API调用功能，但在易用性方面仍有改进空间：
1. 调用API需要手动构造参数Map，不够直观
2. API配置管理较为复杂，需要了解内部配置结构
3. 缺乏面向特定API的便捷调用方法
4. 配置方式可以进一步简化

### 1.2 优化目标
1. 简化SDK使用流程，实现"引入依赖-配置密钥-直接调用"三步集成
2. 提供更直观的API调用方式，减少样板代码
3. 支持动态API发现和配置加载
4. 提供更好的错误处理和日志记录
5. 增强SDK的扩展性和可维护性

### 1.3 SDK架构概述
QiAPI Client SDK采用模块化设计，主要包含以下核心组件：
- **QiApiClient**: 核心客户端类，提供统一的API调用入口
- **ApiConfigManager**: API配置管理器，负责API配置的加载、缓存和管理
- **RequestBuilder**: 请求构建器，负责构建HTTP请求
- **ResponseHandler**: 响应处理器，负责处理HTTP响应并转换为统一格式
- **ApiService**: 业务API服务接口，提供面向特定业务的便捷调用方法

### 1.4 默认配置策略
为了解决platform-url硬编码问题，SDK采用以下默认配置策略：
1. platform-url提供合理的默认值，指向官方API平台
2. 用户可以根据需要自定义该配置，以支持私有部署或测试环境
3. 对于大多数用户，使用默认值即可，无需额外配置
4. 通过这种方式，既保证了易用性，又保持了灵活性


## 2. 优化方案设计

### 2.1 简化配置方式
当前配置方式：
```yaml
qiapi:
  client:
    access-key: "your-access-key"
    secret-key: "your-secret-key"
```

优化后保持一致，但增加更多配置选项：
```yaml
qiapi:
  client:
    access-key: "your-access-key"
    secret-key: "your-secret-key"
    platform-url: "https://api.qiapi.com"  # 平台地址，用于动态获取API配置，默认为官方平台地址
    default-timeout: 30000  # 默认超时时间
    enable-discovery: true  # 是否启用API发现
    cache-expire-time: 300  # API配置缓存过期时间(秒)
```

说明：
1. `platform-url` 提供默认值，指向官方API平台地址
2. 用户可以根据需要自定义该配置，以支持私有部署或测试环境
3. 对于大多数用户，使用默认值即可，无需额外配置
4. 默认值在SDK内部定义，不在代码中硬编码，可通过环境变量或系统属性覆盖

### 2.2 简化API调用方式

#### 2.2.1 当前调用方式
```java
@Autowired
private QiApiClient qiApiClient;

Map<String, Object> params = new HashMap<>();
params.put("name", "zhexueqi");
ApiResponse<String> response = qiApiClient.callApi("name.get", params, String.class);
```

#### 2.2.2 优化后调用方式
```java
// 方式1: 直接注入特定API服务（推荐）
@Autowired
private UserService userService;

// 直接调用，无需构造参数Map
User user = userService.getUserById(123L);
List<User> users = userService.listUsers("keyword", 1, 10);

// 方式2: 保持原有通用调用方式，但提供更便捷的方法
@Autowired
private QiApiClient qiApiClient;

// 简化参数传递
ApiResponse<User> response = qiApiClient.call("user.get", User.class)
    .param("id", 123L)
    .param("name", "zhexueqi")
    .execute();
```

### 2.3 新增API服务接口设计

#### 2.3.1 基础API服务接口
```java
public interface ApiService {
    /**
     * 创建API调用请求
     * @param apiId API标识
     * @param targetType 返回类型
     * @return ApiRequestBuilder
     */
    <T> ApiRequestBuilder<T> call(String apiId, Class<T> targetType);
    
    /**
     * 获取所有可用API配置
     * @return API配置列表
     */
    List<ApiConfig> getAvailableApis();
    
    /**
     * 刷新API配置缓存
     */
    void refreshApiConfigs();
}
```

#### 2.3.2 API请求构建器
```java
public class ApiRequestBuilder<T> {
    private final ApiRequest apiRequest;
    private final Class<T> targetType;
    private final QiApiClient client;
    
    public ApiRequestBuilder<T> param(String key, Object value) {
        if (apiRequest.getParams() == null) {
            apiRequest.setParams(new HashMap<>());
        }
        apiRequest.getParams().put(key, value);
        return this;
    }
    
    public ApiRequestBuilder<T> header(String key, String value) {
        if (apiRequest.getHeaders() == null) {
            apiRequest.setHeaders(new HashMap<>());
        }
        apiRequest.getHeaders().put(key, value);
        return this;
    }
    
    public ApiRequestBuilder<T> body(Object body) {
        apiRequest.setBody(body);
        return this;
    }
    
    public ApiRequestBuilder<T> timeout(int timeout) {
        apiRequest.setTimeout(timeout);
        return this;
    }
    
    public ApiResponse<T> execute() {
        return client.callApi(apiRequest, targetType);
    }
    
    public CompletableFuture<ApiResponse<T>> async() {
        return CompletableFuture.supplyAsync(this::execute);
    }
}
```

#### 2.3.3 具体业务API服务示例
```java
@Service
public class UserService {
    @Autowired
    private QiApiClient qiApiClient;
    
    /**
     * 根据ID获取用户信息
     * @param id 用户ID
     * @return 用户信息
     */
    public User getUserById(Long id) {
        ApiResponse<User> response = qiApiClient.call("user.get", User.class)
            .param("id", id)
            .execute();
        return response.isSuccess() ? response.getData() : null;
    }
    
    /**
     * 获取用户列表
     * @param keyword 搜索关键词
     * @param page 页码
     * @param size 每页大小
     * @return 用户列表
     */
    public List<User> listUsers(String keyword, int page, int size) {
        ApiResponse<List<User>> response = qiApiClient.call("user.list", List.class)
            .param("keyword", keyword)
            .param("page", page)
            .param("size", size)
            .execute();
        return response.isSuccess() ? response.getData() : Collections.emptyList();
    }
    
    /**
     * 创建用户
     * @param user 用户信息
     * @return 创建结果
     */
    public boolean createUser(User user) {
        ApiResponse<Boolean> response = qiApiClient.call("user.create", Boolean.class)
            .body(user)
            .execute();
        return response.isSuccess() && response.getData();
    }
    
    /**
     * 异步获取用户信息
     * @param id 用户ID
     * @return CompletableFuture<User>
     */
    public CompletableFuture<User> getUserByIdAsync(Long id) {
        return qiApiClient.call("user.get", User.class)
            .param("id", id)
            .async()
            .thenApply(response -> response.isSuccess() ? response.getData() : null);
    }
}
```

## 3. 核心组件优化

### 3.1 QiApiClient 优化
增强QiApiClient类，添加便捷方法：

```java
@Slf4j
public class QiApiClient implements ApiService {
    // 原有字段和构造函数保持不变
    
    /**
     * 创建API调用请求构建器
     * @param apiId API标识
     * @param targetType 返回类型
     * @return ApiRequestBuilder
     */
    @Override
    public <T> ApiRequestBuilder<T> call(String apiId, Class<T> targetType) {
        return new ApiRequestBuilder<>(new ApiRequest(apiId), targetType, this);
    }
    
    // 原有的callApi方法保持不变，以保证向后兼容
}
```

### 3.2 ApiConfigManager 优化
增强API配置管理器，支持从远程平台动态加载配置：

```java
public interface ApiConfigManager {
    // 原有方法保持不变
    
    /**
     * 从远程平台加载API配置
     * @param platformUrl 平台URL
     * @return API配置列表
     */
    List<ApiConfig> loadApiConfigsFromPlatform(String platformUrl);
    
    /**
     * 设置API配置缓存过期时间
     * @param expireTime 过期时间(秒)
     */
    void setCacheExpireTime(long expireTime);
    
    /**
     * 获取缓存的API配置
     * @param apiId API标识
     * @return API配置
     */
    ApiConfig getCachedApiConfig(String apiId);
    
    /**
     * 添加API配置监听器
     * @param listener 监听器
     */
    void addConfigChangeListener(ConfigChangeListener listener);
    
    /**
     * 设置默认平台URL
     * @param platformUrl 平台URL
     */
    void setDefaultPlatformUrl(String platformUrl);
}

public interface ConfigChangeListener {
    /**
     * 配置变更回调
     * @param apiId API标识
     * @param newConfig 新配置
     */
    void onConfigChanged(String apiId, ApiConfig newConfig);
}
```

### 3.3 自动配置优化
增强自动配置类，支持更多配置选项：

```java
@Configuration
@EnableConfigurationProperties(QiapiClientConfig.class)
public class QiapiClientAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public QiApiClient qiApiClient(QiapiClientConfig config) {
        // 创建客户端实例
        QiApiClient client = new QiApiClient(
            config.getAccessKey(),
            config.getSecretKey(),
            apiConfigManager(config),
            requestBuilder(),
            responseHandler()
        );
        
        // 如果启用API发现，则从平台加载配置
        if (config.isEnableDiscovery()) {
            ApiConfigManager configManager = apiConfigManager(config);
            // 使用配置的平台URL，如果未配置则使用默认值
            String platformUrl = config.getPlatformUrl();
            if (platformUrl != null && !platformUrl.isEmpty()) {
                configManager.loadApiConfigsFromPlatform(platformUrl);
            }
        }
        
        return client;
    }
    
    @Bean
    @ConditionalOnMissingBean
    public ApiConfigManager apiConfigManager(QiapiClientConfig config) {
        DefaultApiConfigManager manager = new DefaultApiConfigManager();
        // 设置默认平台URL
        if (config.getPlatformUrl() != null && !config.getPlatformUrl().isEmpty()) {
            manager.setDefaultPlatformUrl(config.getPlatformUrl());
        }
        if (config.getCacheExpireTime() > 0) {
            manager.setCacheExpireTime(config.getCacheExpireTime());
        }
        return manager;
    }
}
```

## 4. 使用示例

### 4.1 基本使用（三步集成）
1. 添加Maven依赖：
```xml
<dependency>
    <groupId>com.qiapi</groupId>
    <artifactId>qiapi-client-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

2. 配置application.yml（仅需配置密钥）：
```yaml
qiapi:
  client:
    access-key: "your-access-key"
    secret-key: "your-secret-key"
```

3. 直接使用：
```java
@Service
public class BusinessService {
    @Autowired
    private UserService userService;
    
    public void businessMethod() {
        // 一行代码调用API
        User user = userService.getUserById(123L);
        System.out.println(user);
    }
}
```

说明：platform-url等其他配置项都有合理的默认值，用户通常无需配置。只有在特殊场景下（如私有部署、测试环境）才需要自定义配置。

### 4.2 高级使用
```java
@Service
public class AdvancedService {
    @Autowired
    private QiApiClient qiApiClient;
    
    public void advancedUsage() {
        // 使用构建器模式调用API
        ApiResponse<User> response = qiApiClient.call("user.get", User.class)
            .param("id", 123L)
            .header("Custom-Header", "CustomValue")
            .timeout(5000)
            .execute();
            
        if (response.isSuccess()) {
            User user = response.getData();
            // 处理业务逻辑
        } else {
            // 处理错误
            log.error("API调用失败: {}", response.getMessage());
        }
    }
}
```

## 5. 向后兼容性

为确保向后兼容，本次优化将：
1. 保留原有的`callApi`方法和使用方式
2. 保留所有现有的配置选项
3. 通过注解标记过时的方法，但不立即移除
4. 提供迁移指南帮助用户升级

## 6. 性能优化

1. **缓存机制**：API配置信息将被缓存，减少重复加载
2. **连接池**：HTTP请求将使用连接池管理，提高请求效率
3. **异步支持**：提供异步调用方法，避免阻塞主线程

```java
// 异步调用示例
CompletableFuture<ApiResponse<User>> future = qiApiClient.call("user.get", User.class)
    .param("id", 123L)
    .async();  // 异步执行

future.thenAccept(response -> {
    if (response.isSuccess()) {
        User user = response.getData();
        // 处理响应
    }
});
```

## 7. 安全性增强

1. **密钥安全管理**：支持从环境变量或配置中心读取密钥
2. **请求签名优化**：增强签名算法安全性
3. **请求频率限制**：内置请求频率控制机制
4. **配置优先级**：支持多种配置方式，优先级为 环境变量 > application.yml > 默认值

## 8. 扩展性设计

1. **插件化架构**：支持自定义请求构建器和响应处理器
2. **拦截器机制**：支持添加请求和响应拦截器
3. **负载均衡**：支持多节点API服务的负载均衡
4. **自定义认证**：支持不同的认证方式扩展

```java
// 自定义拦截器示例
@Component
public class CustomInterceptor implements RequestInterceptor, ResponseInterceptor {
    @Override
    public void interceptRequest(HttpRequest request) {
        // 在请求发送前进行处理
        request.header("X-Custom-TraceId", generateTraceId());
    }
    
    @Override
    public void interceptResponse(HttpResponse response) {
        // 在响应处理后进行处理
        log.info("Response status: {}", response.getStatus());
    }
}

// 自定义认证示例
public class CustomAuthenticator implements Authenticator {
    @Override
    public void authenticate(HttpRequest request, ApiConfig config) {
        // 实现自定义认证逻辑
        String token = generateOAuthToken();
        request.header("Authorization", "Bearer " + token);
    }
}
```
