# QiApi接口开放平台
基于 React + Spring Boot + Dubbo + Gateway 的 API 接口开放调用平台。

管理员可以接入并发布接口，可视化各接口调用情况；用户可以开通接口调用权限、浏览接口及在线调试，并通过客户端 SDK 轻松调用接口。

## 项目结构
```
|--apiQi               —— 前端项目
|--nacos-server-2.4.3  —— 注册中心
|--qiapi-backend       —— 后端项目
    |-- qiapi-common        —— 通用模块
    |-- qiapi-client-sdk    —— 客户端 SDK
    |-- qiapi-interface     —— 接口模块
|--qiapi-gateway       —— 网关
```