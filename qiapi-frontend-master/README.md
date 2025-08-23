# QiAPI接口开放平台 - 前端

这是QiAPI接口开放平台的前端项目，基于React + Ant Design Pro构建的现代化管理后台。

## 技术栈

- **React 18** - 前端框架
- **Ant Design Pro** - 企业级UI解决方案
- **TypeScript** - 类型安全的JavaScript
- **Umi 4** - 企业级前端应用框架
- **ProComponents** - 中后台组件库

## 主要功能

### 用户功能
- 🔐 用户注册、登录
- 👤 个人信息管理
- 🔑 AccessKey/SecretKey管理
- 🔒 密码修改

### 接口管理
- 📋 接口列表浏览
- 🔍 接口搜索和筛选
- 📖 接口详情查看
- 🧪 在线接口调试
- 📊 接口调用统计

### 管理员功能
- 👥 用户管理
- 📡 接口管理（增删改查）
- 📈 数据分析和可视化
- 🛠️ 系统配置管理

## 快速开始

### 安装依赖
```bash
npm install
# 或
yarn install
```

### 启动开发服务器
```bash
npm start
# 或
yarn start
```

### 构建生产版本
```bash
npm run build
# 或
yarn build
```

## 项目结构

```
src/
├── components/          # 通用组件
├── pages/              # 页面组件
│   ├── Admin/          # 管理员页面
│   ├── Index/          # 首页
│   ├── InterfaceInfo/  # 接口详情页
│   ├── Profile/        # 个人中心
│   └── User/          # 用户相关页面
├── services/          # API服务
├── access.ts          # 权限配置
├── app.tsx           # 应用入口配置
└── requestConfig.ts  # 请求配置
```

## 开发说明

### 代码规范
- 使用 ESLint + Prettier 进行代码格式化
- 遵循 TypeScript 严格模式
- 组件使用 React Hooks

### API接口
- 所有API请求统一通过 `src/services` 管理
- 使用 OpenAPI 自动生成接口类型
- 支持请求/响应拦截器

### 权限管理
- 基于角色的权限控制(RBAC)
- 页面级和组件级权限控制
- 动态路由和菜单生成

## 相关链接

- 后端项目：`../qiapi-backend`
- 网关项目：`../qiapi-gateway`
- 项目文档：查看根目录README.md
