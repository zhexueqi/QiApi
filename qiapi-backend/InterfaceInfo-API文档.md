# API开放平台 - 接口信息管理 API文档

**基础URL**: `http://localhost:8090`

**说明**: 本文档包含接口信息管理相关的所有API接口，用于前端开发参考。

---

## 📋 目录

1. [接口管理](#接口管理)
2. [接口调用](#接口调用) 
3. [API发现](#API发现)
4. [数据模型](#数据模型)
5. [错误码](#错误码)

---

## 🔧 接口管理

### 1. 创建接口

**接口地址**: `POST /interfaceInfo/add`

**请求头**:
```
Content-Type: application/json
Cookie: your-session-cookie
```

**请求参数**:
```json
{
  "name": "接口名称",
  "description": "接口描述", 
  "url": "/api/example",
  "method": "GET",
  "requestParams": "[{\"name\":\"param1\",\"type\":\"string\",\"required\":true}]",
  "requestHeader": "Content-Type: application/json",
  "responseHeader": "Content-Type: application/json",
  "requestExample": "{\"param1\":\"value1\"}",
  "responseExample": "{\"code\":0,\"data\":\"success\"}"
}
```

**响应示例**:
```json
{
  "code": 0,
  "data": 123456,
  "message": "ok"
}
```

**权限要求**: 需要登录

---

### 2. 删除接口

**接口地址**: `POST /interfaceInfo/delete`

**请求参数**:
```json
{
  "id": 123456
}
```

**响应示例**:
```json
{
  "code": 0,
  "data": true,
  "message": "ok"
}
```

**权限要求**: 仅本人或管理员可删除

---

### 3. 更新接口（仅管理员）

**接口地址**: `POST /interfaceInfo/update`

**请求参数**:
```json
{
  "id": 123456,
  "name": "更新后的接口名称",
  "description": "更新后的接口描述",
  "url": "/api/new-example",
  "method": "POST",
  "status": 1
}
```

**响应示例**:
```json
{
  "code": 0,
  "data": true,
  "message": "ok"
}
```

**权限要求**: 仅管理员

---

### 4. 根据ID获取接口详情

**接口地址**: `GET /interfaceInfo/get/vo`

**请求参数**:
```
id: 123456 (query参数)
```

**响应示例**:
```json
{
  "code": 0,
  "data": {
    "id": 123456,
    "name": "接口名称",
    "description": "接口描述",
    "url": "/api/example", 
    "method": "GET",
    "requestParams": "[{\"name\":\"param1\",\"type\":\"string\",\"required\":true}]",
    "requestHeader": "Content-Type: application/json",
    "responseHeader": "Content-Type: application/json",
    "status": 1,
    "createTime": "2024-08-22T10:00:00",
    "updateTime": "2024-08-22T10:00:00",
    "totalNum": 100,
    "leftNum": 50
  },
  "message": "ok"
}
```

---

### 5. 分页获取接口列表（仅管理员）

**接口地址**: `GET /interfaceInfo/list/page`

**请求参数**:
```
current: 1 (当前页)
pageSize: 10 (每页大小)
name: 接口名称 (可选，模糊搜索)
method: GET (可选，请求方法)
status: 1 (可选，接口状态)
```

**响应示例**:
```json
{
  "code": 0,
  "data": {
    "records": [
      {
        "id": 123456,
        "name": "接口名称",
        "description": "接口描述",
        "url": "/api/example",
        "method": "GET",
        "status": 1,
        "createTime": "2024-08-22T10:00:00"
      }
    ],
    "total": 50,
    "size": 10,
    "current": 1,
    "pages": 5
  },
  "message": "ok"
}
```

**权限要求**: 仅管理员

---

### 6. 分页获取接口列表（封装类）

**接口地址**: `POST /interfaceInfo/list/page/vo`

**请求参数**:
```json
{
  "current": 1,
  "pageSize": 10,
  "name": "接口名称",
  "method": "GET",
  "status": 1,
  "sortField": "createTime",
  "sortOrder": "desc"
}
```

**响应示例**: 同上，但返回InterfaceInfoVO对象

---

### 7. 获取当前用户创建的接口列表

**接口地址**: `POST /interfaceInfo/my/list/page/vo`

**请求参数**:
```json
{
  "current": 1,
  "pageSize": 10,
  "name": "接口名称"
}
```

**响应示例**: 返回当前用户创建的接口列表

**权限要求**: 需要登录

---

### 8. 发布接口（管理员操作）

**接口地址**: `POST /interfaceInfo/online`

**请求参数**:
```json
{
  "id": 123456
}
```

**响应示例**:
```json
{
  "code": 0,
  "data": true,
  "message": "ok"
}
```

**权限要求**: 仅管理员

---

### 9. 下线接口（管理员操作）

**接口地址**: `POST /interfaceInfo/offline`

**请求参数**:
```json
{
  "id": 123456
}
```

**响应示例**:
```json
{
  "code": 0,
  "data": true,
  "message": "ok"
}
```

**权限要求**: 仅管理员

---

## 🚀 接口调用

### 10. 调用接口

**接口地址**: `POST /interfaceInfo/invoke`

**请求参数**:
```json
{
  "id": 123456,
  "userRequestParams": "{\"name\":\"zhexueqi\"}"
}
```

**响应示例**:
```json
{
  "code": 0,
  "data": "POST 你的名字是:zhexueqi",
  "message": "ok"
}
```

**说明**: 
- `id`: 要调用的接口ID
- `userRequestParams`: 调用接口所需的参数，JSON字符串格式
- **支持完整URL**: 数据库中存储的接口URL应该是完整的第三方API地址
  - 示例: `http://api.example.com/v1/user/info`
  - 示例: `https://api.weather.com/current`
- **自动URL解析**: 系统会自动解析完整URL，提取baseUrl和path
- **本地测试兼容**: 仍然支持本地qiapi-interface服务（localhost:8081）
- 系统会根据接口配置自动选择对应的API进行调用
- 支持多种接口类型：GET、POST、RESTful等

**权限要求**: 需要登录，且用户需要有对应接口的调用权限

---

## 🔍 API发现

### 11. 获取所有可用的API列表

**接口地址**: `GET /interfaceInfo/available-apis`

**响应示例**:
```json
{
  "code": 0,
  "data": [
    {
      "apiId": "name.get",
      "apiName": "获取用户名",
      "description": "根据名称获取用户信息",
      "baseUrl": "http://localhost:8081",
      "path": "/api/name",
      "method": "GET",
      "paramType": "QUERY",
      "requireAuth": true,
      "authType": "SIGNATURE",
      "responseFormat": "TEXT",
      "status": "ACTIVE",
      "version": "1.0"
    },
    {
      "apiId": "name.restful",
      "apiName": "RESTful用户名接口",
      "description": "通过RESTful方式处理用户信息",
      "baseUrl": "http://localhost:8081",
      "path": "/api/name/restful",
      "method": "POST",
      "paramType": "BODY",
      "requireAuth": true,
      "authType": "SIGNATURE",
      "responseFormat": "TEXT",
      "status": "ACTIVE",
      "version": "1.0"
    }
  ],
  "message": "ok"
}
```

---

### 12. 根据分类获取API列表

**接口地址**: `GET /interfaceInfo/apis/category`

**请求参数**:
```
category: 用户 (分类关键词)
```

**响应示例**:
```json
{
  "code": 0,
  "data": [
    {
      "apiId": "name.get",
      "apiName": "获取用户名",
      "description": "根据名称获取用户信息",
      "method": "GET",
      "status": "ACTIVE"
    }
  ],
  "message": "ok"
}
```

---

## 📊 数据模型

### InterfaceInfoAddRequest
```typescript
interface InterfaceInfoAddRequest {
  name: string;           // 接口名称
  description?: string;   // 接口描述
  url: string;           // 完整的第三方API地址，如: http://api.example.com/v1/user
  method: string;        // 请求方法
  requestParams?: string; // 请求参数
  requestHeader?: string; // 请求头
  responseHeader?: string;// 响应头
  requestExample?: string;// 请求示例
  responseExample?: string;// 响应示例
}
```

### InterfaceInfoVO
```typescript
interface InterfaceInfoVO {
  id: number;            // 接口ID
  name: string;          // 接口名称  
  description: string;   // 接口描述
  url: string;           // 完整的第三方API地址
  method: string;        // 请求方法
  requestParams: string; // 请求参数
  requestHeader: string; // 请求头
  responseHeader: string;// 响应头
  status: number;        // 接口状态(0-关闭 1-开启)
  createTime: string;    // 创建时间
  updateTime: string;    // 更新时间
  totalNum?: number;     // 总调用次数
  leftNum?: number;      // 剩余调用次数
}
```

### BaseResponse
```typescript
interface BaseResponse<T> {
  code: number;    // 状态码 (0-成功)
  data: T;         // 响应数据
  message: string; // 响应消息
}
```

---

## ❌ 错误码

| 错误码 | 说明 | 解决方案 |
|--------|------|----------|
| 40000 | 请求参数错误 | 检查请求参数格式和必填项 |
| 40001 | 请求数据为空 | 确保请求体不为空 |
| 40101 | 未登录 | 请先登录获取session |
| 40301 | 无权限 | 检查用户权限或联系管理员 |
| 40400 | 请求数据不存在 | 检查请求的资源ID是否正确 |
| 50000 | 系统内部异常 | 联系技术支持 |

---

## 📝 使用说明

1. **环境**: 所有接口的基础URL为 `http://localhost:8090`
2. **认证**: 大部分接口需要登录状态，请确保携带有效的session cookie
3. **权限**: 部分接口需要管理员权限，注意权限要求
4. **分页**: 分页查询的pageSize限制为20，避免爬虫攻击
5. **调用限制**: 接口调用需要检查用户的调用次数余额
6. **完整URL**: 接口URL字段应存储完整的第三方API地址
   - ✅ 正确: `http://api.example.com/v1/user/info`
   - ✅ 正确: `https://api.weather.com/current?city=beijing`
   - ❌ 错误: `/api/user/info` (相对路径)
7. **本地测试**: 支持本地qiapi-interface服务，URL可以是 `http://localhost:8081/api/name`

---

## 🔄 接口调用流程

1. **查看可用API** → 调用 `/interfaceInfo/available-apis` 获取支持的API列表
2. **选择接口** → 从接口列表中选择要调用的接口
3. **构造参数** → 根据接口要求构造请求参数
4. **调用接口** → 使用 `/interfaceInfo/invoke` 调用具体接口
5. **处理响应** → 根据返回结果进行后续处理

---

**最后更新时间**: 2024-08-22  
**文档版本**: v1.0  
**技术支持**: zhexueqi
