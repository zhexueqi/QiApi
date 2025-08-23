# QiAPI开放平台 - 完整接口文档

## 📋 项目概述

**项目名称**: QiAPI开放平台后端系统  
**基础URL**: http://localhost:8090  
**技术栈**: Spring Boot 2.7.2 + Dubbo 3.2.5 + MyBatis Plus 3.5.2  
**开发语言**: Java 17  

### 功能特性
- 🔐 用户管理与认证
- 🚀 接口信息管理
- 📊 接口调用统计
- 🔑 API密钥自主管理
- 🛡️ 网关统一认证
- 📈 数据分析功能

---

## 🔐 用户管理接口

### 1. 用户注册
**接口路径**: `POST /user/register`  
**功能描述**: 用户注册，自动生成API密钥对  
**权限要求**: 无  

**请求参数**:
```json
{
  "userAccount": "string",     // 用户账号，4-16位
  "userPassword": "string",    // 用户密码，不少于8位
  "checkPassword": "string"    // 确认密码，需与userPassword一致
}
```

**请求示例**:
```bash
curl -X POST http://localhost:8090/user/register \
  -H "Content-Type: application/json" \
  -d '{
    "userAccount": "testuser123",
    "userPassword": "12345678",
    "checkPassword": "12345678"
  }'
```

**响应示例**:
```json
{
  "code": 0,
  "data": 1001,
  "message": "ok"
}
```

### 2. 用户登录
**接口路径**: `POST /user/login`  
**功能描述**: 用户登录获取会话  
**权限要求**: 无  

**请求参数**:
```json
{
  "userAccount": "string",     // 用户账号
  "userPassword": "string"     // 用户密码
}
```

**请求示例**:
```bash
curl -X POST http://localhost:8090/user/login \
  -H "Content-Type: application/json" \
  -d '{
    "userAccount": "testuser123",
    "userPassword": "12345678"
  }'
```

**响应示例**:
```json
{
  "code": 0,
  "data": {
    "id": 1001,
    "userName": "testuser123",
    "userAvatar": "",
    "userProfile": "",
    "userRole": "user",
    "createTime": "2023-12-21T10:30:00",
    "updateTime": "2023-12-21T10:30:00"
  },
  "message": "ok"
}
```

### 3. 微信登录
**接口路径**: `GET /user/login/wx_open`  
**功能描述**: 微信开放平台登录  
**权限要求**: 无  

**请求参数**:
- `code`: 微信授权码（Query参数）

**请求示例**:
```bash
curl -X GET "http://localhost:8090/user/login/wx_open?code=wx_auth_code"
```

### 4. 用户登出
**接口路径**: `POST /user/logout`  
**功能描述**: 用户登出，清除会话  
**权限要求**: 需要登录  

**请求示例**:
```bash
curl -X POST http://localhost:8090/user/logout \
  -H "Cookie: SESSION=your-session-id"
```

### 5. 获取当前登录用户
**接口路径**: `GET /user/get/login`  
**功能描述**: 获取当前登录用户信息  
**权限要求**: 需要登录  

**请求示例**:
```bash
curl -X GET http://localhost:8090/user/get/login \
  -H "Cookie: SESSION=your-session-id"
```

### 6. 生成API密钥
**接口路径**: `POST /user/generate/keys`  
**功能描述**: 为用户生成新的API密钥对  
**权限要求**: 需要登录  

**请求示例**:
```bash
curl -X POST http://localhost:8090/user/generate/keys \
  -H "Cookie: SESSION=your-session-id"
```

**响应示例**:
```json
{
  "code": 0,
  "data": {
    "accessKey": "qiapi_1703123456789_abc12def",
    "secretKey": "A1b2C3d4E5f6G7h8I9j0K1l2M3n4O5p6Q7r8S9t0U1v2W3x4Y5z6A7b8C9d0E1f2",
    "hasKeys": true,
    "generateTime": "2023-12-21 10:30:00",
    "usage": "请妥善保管您的密钥信息，不要泄露给他人。AccessKey用于身份识别，SecretKey用于签名验证。"
  },
  "message": "ok"
}
```

### 7. 重新生成API密钥
**接口路径**: `POST /user/regenerate/keys`  
**功能描述**: 重新生成API密钥对（旧密钥失效）  
**权限要求**: 需要登录  

**请求示例**:
```bash
curl -X POST http://localhost:8090/user/regenerate/keys \
  -H "Cookie: SESSION=your-session-id"
```

### 8. 查看API密钥信息
**接口路径**: `GET /user/get/keys`  
**功能描述**: 查看当前用户的密钥信息（SecretKey掩码显示）  
**权限要求**: 需要登录  

**请求示例**:
```bash
curl -X GET http://localhost:8090/user/get/keys \
  -H "Cookie: SESSION=your-session-id"
```

**响应示例**:
```json
{
  "code": 0,
  "data": {
    "accessKey": "qiapi_1703123456789_abc12def",
    "secretKey": "A1b2************************************************E1f2",
    "hasKeys": true,
    "generateTime": "2023-12-21 10:30:00",
    "usage": "请妥善保管您的密钥信息，不要泄露给他人。AccessKey用于身份识别，SecretKey用于签名验证。"
  },
  "message": "ok"
}
```

### 9. 更新个人信息
**接口路径**: `POST /user/update/my`  
**功能描述**: 更新当前用户的个人信息  
**权限要求**: 需要登录  

**请求参数**:
```json
{
  "userName": "string",        // 用户昵称（可选）
  "userAvatar": "string",      // 用户头像URL（可选）
  "userProfile": "string"      // 用户简介（可选）
}
```

---

## 📡 接口信息管理

### 1. 创建接口信息
**接口路径**: `POST /interfaceInfo/add`  
**功能描述**: 创建新的接口信息  
**权限要求**: 需要登录  

**请求参数**:
```json
{
  "name": "string",            // 接口名称
  "description": "string",     // 接口描述
  "url": "string",            // 接口URL（完整地址）
  "requestHeader": "string",   // 请求头说明
  "responseHeader": "string",  // 响应头说明
  "method": "string",         // 请求方法（GET/POST/PUT/DELETE）
  "requestParams": "string",   // 请求参数说明
  "responseFormat": "string"   // 响应格式说明
}
```

**请求示例**:
```bash
curl -X POST http://localhost:8090/interfaceInfo/add \
  -H "Content-Type: application/json" \
  -H "Cookie: SESSION=your-session-id" \
  -d '{
    "name": "获取用户信息",
    "description": "根据用户ID获取用户基本信息",
    "url": "http://api.example.com/v1/user/info",
    "method": "GET",
    "requestParams": "userId: 用户ID",
    "responseFormat": "JSON格式用户信息"
  }'
```

### 2. 删除接口信息
**接口路径**: `POST /interfaceInfo/delete`  
**功能描述**: 删除接口信息（仅创建者或管理员）  
**权限要求**: 需要登录  

**请求参数**:
```json
{
  "id": "number"              // 接口ID
}
```

### 3. 更新接口信息
**接口路径**: `POST /interfaceInfo/update`  
**功能描述**: 更新接口信息  
**权限要求**: 管理员权限  

### 4. 获取接口详情
**接口路径**: `GET /interfaceInfo/get/vo`  
**功能描述**: 根据ID获取接口详细信息  
**权限要求**: 需要登录  

**请求参数**:
- `id`: 接口ID（Query参数）

**请求示例**:
```bash
curl -X GET "http://localhost:8090/interfaceInfo/get/vo?id=1" \
  -H "Cookie: SESSION=your-session-id"
```

### 5. 分页获取接口列表
**接口路径**: `POST /interfaceInfo/list/page/vo`  
**功能描述**: 分页获取接口信息列表  
**权限要求**: 需要登录  

**请求参数**:
```json
{
  "current": 1,               // 当前页码
  "pageSize": 10,            // 每页大小
  "name": "string",          // 接口名称（模糊查询，可选）
  "description": "string",   // 接口描述（模糊查询，可选）
  "method": "string",        // 请求方法（可选）
  "status": 1                // 接口状态（可选）
}
```

**请求示例**:
```bash
curl -X POST http://localhost:8090/interfaceInfo/list/page/vo \
  -H "Content-Type: application/json" \
  -H "Cookie: SESSION=your-session-id" \
  -d '{
    "current": 1,
    "pageSize": 10
  }'
```

### 6. 接口上线
**接口路径**: `POST /interfaceInfo/online`  
**功能描述**: 发布接口，使其可被调用  
**权限要求**: 管理员权限  

**请求参数**:
```json
{
  "id": "number"              // 接口ID
}
```

### 7. 接口下线
**接口路径**: `POST /interfaceInfo/offline`  
**功能描述**: 下线接口，停止服务  
**权限要求**: 管理员权限  

### 8. 🚀 调用接口（核心功能）
**接口路径**: `POST /interfaceInfo/invoke`  
**功能描述**: 调用指定的API接口  
**权限要求**: 需要登录  

**请求参数**:
```json
{
  "id": "number",             // 接口ID
  "userRequestParams": "string" // 用户请求参数（JSON字符串）
}
```

**请求示例**:
```bash
curl -X POST http://localhost:8090/interfaceInfo/invoke \
  -H "Content-Type: application/json" \
  -H "Cookie: SESSION=your-session-id" \
  -d '{
    "id": 1,
    "userRequestParams": "{\"name\":\"张三\"}"
  }'
```

**响应示例**:
```json
{
  "code": 0,
  "data": {
    "result": "Hello, 张三!",
    "timestamp": "2023-12-21T10:30:00"
  },
  "message": "ok"
}
```

### 9. 获取可用API列表
**接口路径**: `GET /interfaceInfo/available-apis`  
**功能描述**: 获取所有可用的API配置  
**权限要求**: 需要登录  

**请求示例**:
```bash
curl -X GET http://localhost:8090/interfaceInfo/available-apis \
  -H "Cookie: SESSION=your-session-id"
```

### 10. 按分类获取API
**接口路径**: `GET /interfaceInfo/apis/category`  
**功能描述**: 根据分类获取API列表  
**权限要求**: 需要登录  

**请求参数**:
- `category`: 分类名称（Query参数）

---

## 📊 数据分析接口

### 1. 获取接口调用排行
**接口路径**: `GET /analysis/top/interface/invoke`  
**功能描述**: 获取调用次数最多的接口TOP3  
**权限要求**: 管理员权限  

**请求示例**:
```bash
curl -X GET http://localhost:8090/analysis/top/interface/invoke \
  -H "Cookie: SESSION=your-session-id"
```

**响应示例**:
```json
{
  "code": 0,
  "data": [
    {
      "id": 1,
      "name": "获取用户信息",
      "description": "根据用户ID获取用户信息",
      "url": "http://api.example.com/v1/user/info",
      "method": "GET",
      "totalNum": 1520
    },
    {
      "id": 2,
      "name": "天气查询",
      "description": "获取指定城市天气信息",
      "url": "http://weather.api.com/v1/current",
      "method": "GET",
      "totalNum": 980
    }
  ],
  "message": "ok"
}
```

---

## 🔒 认证机制

### API调用签名认证
所有通过网关的API调用都需要进行签名认证，除了白名单路径：
- `/user/register` - 用户注册
- `/user/login` - 用户登录
- `/user/login/wx_open` - 微信登录
- `/user/logout` - 用户登出

#### 签名算法
使用用户的AccessKey和SecretKey进行HMAC-SHA256签名：

**请求头参数**:
```
accessKey: 用户的AccessKey
nonce: 随机数（小于10000）
timestamp: 时间戳（秒级，5分钟内有效）
sign: 签名值
body: 请求体内容
```

**签名生成示例**（Java）:
```java
Map<String, String> paramMap = new HashMap<>();
paramMap.put("body", requestBody);
paramMap.put("accessKey", accessKey);
paramMap.put("nonce", "12345");
paramMap.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));

String sign = SignUtils.getSign(requestBody, paramMap);
```

---

## 📋 数据模型

### User（用户实体）
```json
{
  "id": "number",              // 用户ID
  "userName": "string",        // 用户昵称
  "userAccount": "string",     // 用户账号
  "userAvatar": "string",      // 用户头像
  "userRole": "string",        // 用户角色（user/admin）
  "userProfile": "string",     // 用户简介
  "accessKey": "string",       // API访问密钥
  "secretKey": "string",       // API秘密密钥
  "createTime": "datetime",    // 创建时间
  "updateTime": "datetime"     // 更新时间
}
```

### InterfaceInfo（接口信息实体）
```json
{
  "id": "number",              // 接口ID
  "name": "string",            // 接口名称
  "description": "string",     // 接口描述
  "url": "string",            // 接口URL
  "requestHeader": "string",   // 请求头
  "responseHeader": "string",  // 响应头
  "status": "number",         // 接口状态（0-关闭，1-开启）
  "method": "string",         // 请求方法
  "userId": "number",         // 创建者ID
  "createTime": "datetime",   // 创建时间
  "updateTime": "datetime"    // 更新时间
}
```

### UserKeyVO（用户密钥视图对象）
```json
{
  "accessKey": "string",       // 访问密钥
  "secretKey": "string",       // 秘密密钥（可能掩码显示）
  "hasKeys": "boolean",        // 是否已生成密钥
  "generateTime": "string",    // 生成时间
  "usage": "string"           // 使用说明
}
```

---

## ⚠️ 错误码说明

| 错误码 | 说明 | 处理建议 |
|--------|------|----------|
| 0 | 成功 | - |
| 40000 | 请求参数错误 | 检查请求参数格式和必填项 |
| 40001 | 请求数据为空 | 确保请求体不为空 |
| 40101 | 未登录 | 需要先登录获取会话 |
| 40102 | 无权限 | 当前用户无访问权限 |
| 40103 | 禁止访问 | 用户被禁用或IP被限制 |
| 40400 | 请求数据不存在 | 检查资源ID是否正确 |
| 50000 | 系统内部异常 | 联系管理员处理 |
| 50001 | 操作失败 | 重试或联系管理员 |

---

## 🚀 前端开发建议

### 1. 认证状态管理
```javascript
// 检查登录状态
const checkLoginStatus = async () => {
  try {
    const response = await fetch('/user/get/login');
    const result = await response.json();
    return result.code === 0;
  } catch (error) {
    return false;
  }
};

// 统一错误处理
const handleApiError = (result) => {
  switch (result.code) {
    case 40101:
      // 跳转到登录页
      window.location.href = '/login';
      break;
    case 40102:
      alert('无权限访问');
      break;
    default:
      alert(result.message || '操作失败');
  }
};
```

### 2. API调用封装
```javascript
// 统一API调用方法
const apiCall = async (url, options = {}) => {
  const response = await fetch(url, {
    credentials: 'include', // 携带Cookie
    headers: {
      'Content-Type': 'application/json',
      ...options.headers
    },
    ...options
  });
  
  const result = await response.json();
  
  if (result.code !== 0) {
    handleApiError(result);
    throw new Error(result.message);
  }
  
  return result.data;
};

// 使用示例
const getUserKeys = () => apiCall('/user/get/keys');
const invokeApi = (data) => apiCall('/interfaceInfo/invoke', {
  method: 'POST',
  body: JSON.stringify(data)
});
```

### 3. 密钥管理组件
```javascript
const KeyManagement = () => {
  const [keys, setKeys] = useState(null);
  
  const generateKeys = async () => {
    try {
      const newKeys = await apiCall('/user/generate/keys', {method: 'POST'});
      setKeys(newKeys);
      alert('密钥生成成功！请妥善保管。');
    } catch (error) {
      console.error('密钥生成失败:', error);
    }
  };
  
  const regenerateKeys = async () => {
    if (confirm('重新生成密钥将使旧密钥失效，确定继续？')) {
      try {
        const newKeys = await apiCall('/user/regenerate/keys', {method: 'POST'});
        setKeys(newKeys);
        alert('密钥重新生成成功！');
      } catch (error) {
        console.error('密钥重新生成失败:', error);
      }
    }
  };
  
  // 组件渲染逻辑...
};
```

---

## 📝 使用流程示例

### 完整的用户使用流程

1. **用户注册**
```bash
curl -X POST http://localhost:8090/user/register \
  -H "Content-Type: application/json" \
  -d '{"userAccount":"testuser","userPassword":"12345678","checkPassword":"12345678"}'
```

2. **用户登录**
```bash
curl -X POST http://localhost:8090/user/login \
  -H "Content-Type: application/json" \
  -d '{"userAccount":"testuser","userPassword":"12345678"}' \
  -c cookies.txt
```

3. **查看密钥信息**
```bash
curl -X GET http://localhost:8090/user/get/keys \
  -b cookies.txt
```

4. **查看可用接口**
```bash
curl -X POST http://localhost:8090/interfaceInfo/list/page/vo \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{"current":1,"pageSize":10}'
```

5. **调用接口**
```bash
curl -X POST http://localhost:8090/interfaceInfo/invoke \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{"id":1,"userRequestParams":"{\"name\":\"测试\"}"}'
```

---

## 🔧 环境配置

### 开发环境启动
1. 启动 Nacos（端口：8848）
2. 启动 MySQL（端口：3306）
3. 启动 Redis（端口：6379）
4. 启动后端服务（端口：7529）
5. 启动网关服务（端口：8090）
6. 启动接口服务（端口：8101）

### 网关路由配置
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user_route
          uri: http://localhost:7529
          predicates:
            - Path=/user/**
        - id: interface_route
          uri: http://localhost:7529
          predicates:
            - Path=/interfaceInfo/**,/analysis/**
        - id: api_route
          uri: http://localhost:8101
          predicates:
            - Path=/api/**
```

---

**文档版本**: v1.0  
**更新时间**: 2023-12-21  
**维护人员**: 开发团队  

如有疑问，请联系技术支持或查看项目源码获取更多详细信息。
