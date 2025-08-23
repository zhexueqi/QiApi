# QiAPI接口开放平台

基于 React + Spring Boot + Dubbo + Gateway 的 API 接口开放调用平台。

管理员可以接入并发布接口，可视化各接口调用情况；用户可以开通接口调用权限、浏览接口及在线调试，并通过客户端 SDK 轻松调用接口。

## 项目特色

- 🚀 **微服务架构**: 基于Dubbo的分布式服务治理
- 🛡️ **网关统一**: Spring Cloud Gateway统一路由和过滤
- 🔒 **安全认证**: 双重认证机制，保障接口调用安全
- 📱 **响应式设计**: 基于Ant Design Pro的现代化前端界面
- ⚡ **高性能**: Redis缓存 + 数据库优化，支持高并发访问

## 项目结构
```
|--qiapi-frontend-master  —— 前端项目 (React)
|--qiapi-backend          —— 后端项目
    |-- qiapi-common        —— 通用模块
    |-- qiapi-client-sdk    —— 客户端 SDK
    |-- qiapi-interface     —— 接口模块
    |-- nacos               —— Nacos注册中心配置
|--qiapi-gateway          —— 网关模块
|--sql                    —— 数据库脚本
```

## 技术栈

### 前端
- React 18
- Ant Design Pro
- TypeScript
- Umi 4

### 后端
- Spring Boot 2.7+
- Spring Cloud Gateway
- Dubbo 3.0+
- Nacos 2.4.3
- MySQL 8.0
- MyBatis Plus
- Redis

## 快速开始

### 前端启动
```bash
cd qiapi-frontend-master
npm install
npm start
```

### 后端启动
1. 启动Nacos注册中心
```bash
cd qiapi-backend/nacos/bin
# Windows
startup.cmd -m standalone
# Linux/Mac
./startup.sh -m standalone
```

2. 启动后端服务
```bash
cd qiapi-backend
mvn clean package
java -jar target/qiapi-backend-0.0.1-SNAPSHOT.jar
```

3. 启动网关服务
```bash
cd qiapi-gateway
mvn clean package
java -jar target/qiapi-gateway-0.0.1-SNAPSHOT.jar
```

## 主要功能

- 🔐 **用户管理**: 用户注册、登录、权限管理
- 📊 **接口管理**: 接口发布、上线/下线、接口文档
- 🔍 **接口浏览**: 接口搜索、查看、在线调试
- 📈 **数据统计**: 接口调用次数统计、可视化图表
- 🔑 **密钥管理**: AccessKey/SecretKey管理
- 🛠️ **SDK调用**: 提供多语言SDK，简化接口调用
