# QiAPI客户端SDK - 多API平台支持

## 概述
QiAPI客户端SDK升级为支持多个第三方API的通用SDK，提供统一的API调用接口。

## 主要特性
- 统一接口调用各种API服务
- 动态API配置管理（按需从平台获取配置，避免初始化时加载全部配置）
- 自动签名认证
- 多种响应格式支持
- 统一错误处理
- 链式调用构建器
- 异步调用支持
- 面向业务的API服务

## 快速开始

### 1. 添加Maven依赖
```xml
<dependency>
    <groupId>com.qiapi</groupId>
    <artifactId>qiapi-client-sdk</artifactId>
    <version>0.0.1</version>
</dependency>
```

### 2. 配置application.yml
```yaml
qiapi:
  client:
    access-key: "your-access-key"
    secret-key: "your-secret-key"
    # 平台地址，用于动态获取API配置，默认为官方平台地址
    platform-url: "https://api.qiapi.com"
    # 默认超时时间
    default-timeout: 30000
    # 是否启用API发现
    enable-discovery: true
    # API配置缓存过期时间(秒)
    cache-expire-time: 300
```

### 3. 直接使用（推荐方式）
```java
@Service
public class BusinessService {
    @Autowired
    private UserService userService;
    
    public void businessMethod() {
        // 一行代码调用API，SDK会在需要时自动从平台获取API配置
        User user = userService.getUserById(123L);
        System.out.println(user);
    }
}
```

### 4. 高级使用方式
```java
@Service
public class AdvancedService {
    @Autowired
    private QiApiClient qiApiClient;
    
    public void advancedUsage() {
        // 使用构建器模式调用API，SDK会在需要时自动从平台获取API配置
        ApiResponse<User> response = qiApiClient.call("user.info", User.class)
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
        
        // 异步调用示例
        CompletableFuture<ApiResponse<User>> future = qiApiClient.call("user.info", User.class)
            .param("id", 123L)
            .async();  // 异步执行

        future.thenAccept(response -> {
            if (response.isSuccess()) {
                User user = response.getData();
                // 处理响应
            }
        });
    }
}
```

## 动态API配置加载机制

SDK采用了动态API配置加载机制，避免在初始化时加载所有API配置，从而减少系统压力：

1. 当调用某个API时，SDK首先在本地缓存中查找配置
2. 如果本地缓存中没有找到，SDK会自动向配置的平台URL发起请求获取API配置
3. 获取到的配置会被缓存，避免重复请求
4. 缓存有过期时间，确保配置的时效性

这种方式特别适用于API数量较多的场景，可以显著减少系统初始化时间和内存占用。

## 支持的API
- name.get: GET方式获取用户名
- name.post: POST方式提交用户名  
- name.restful: RESTful方式处理用户信息
- weather.current: 获取天气信息
- user.info: 获取用户信息

查看所有可用API:
```java
List<ApiConfig> apis = qiApiClient.getAvailableApis();
```

## 向后兼容性
为确保向后兼容，本次优化将：
1. 保留原有的`callApi`方法和使用方式
2. 保留所有现有的配置选项
3. 通过注解标记过时的方法，但不立即移除
4. 提供迁移指南帮助用户升级