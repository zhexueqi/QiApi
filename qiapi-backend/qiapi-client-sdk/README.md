# QiAPI客户端SDK - 多API平台支持

## 概述
QiAPI客户端SDK升级为支持多个第三方API的通用SDK，提供统一的API调用接口。

## 主要特性
- 统一接口调用各种API服务
- 动态API配置管理
- 自动签名认证
- 多种响应格式支持
- 统一错误处理

## 快速开始

### 配置
```yaml
qiapi:
  client:
    access-key: "your-access-key"
    secret-key: "your-secret-key"
```

### 基本使用
```java
@Autowired
private QiApiClient qiApiClient;

Map<String, Object> params = new HashMap<>();
params.put("name", "zhexueqi");

ApiResponse<String> response = qiApiClient.callApi("name.get", params, String.class);

if (response.isSuccess()) {
    System.out.println("结果: " + response.getData());
}
```

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
