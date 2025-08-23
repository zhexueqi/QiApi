# QiAPI开放平台额度系统设计文档

## 1. 概述

本文档描述了QiAPI开放平台额度系统的完整设计方案，该系统将为用户提供灵活的API调用额度管理机制，支持固定额度申请、动态额度分配、消费记录跟踪等功能。

### 1.1 业务目标
- 为每个接口提供100次固定免费额度申请
- 实现基于积分/余额的额度购买机制
- 提供多种额度套餐和灵活的充值方案
- 建立完整的额度消费记录和统计体系
- 支持企业级用户的批量额度管理

### 1.2 核心特性
- **固定额度**: 每个接口可申请100次免费调用额度
- **积分体系**: 基于积分的额度购买和兑换机制
- **套餐管理**: 多层次的额度套餐设计
- **实时监控**: 额度消费的实时统计和告警
- **额度转移**: 支持用户间额度转移功能

## 2. 系统架构

### 2.1 整体架构设计

```mermaid
graph TB
    subgraph "用户层"
        U1[普通用户]
        U2[企业用户]
        U3[开发者]
    end
    
    subgraph "业务层"
        CS[额度服务 CreditService]
        PS[积分服务 PointService]
        OS[订单服务 OrderService]
        NS[通知服务 NotificationService]
    end
    
    subgraph "数据层"
        CT[用户额度表 user_credit]
        PT[积分记录表 user_points]
        CP[额度套餐表 credit_packages]
        CR[额度消费记录表 credit_records]
        OT[订单表 orders]
    end
    
    subgraph "外部系统"
        PG[支付网关]
        MQ[消息队列]
        CACHE[Redis缓存]
    end
    
    U1 --> CS
    U2 --> CS
    U3 --> CS
    
    CS --> CT
    CS --> CR
    PS --> PT
    OS --> OT
    
    CS --> CACHE
    OS --> PG
    NS --> MQ
```

### 2.2 核心组件关系

```mermaid
classDiagram
    class CreditService {
        +applyCreditLimit(userId, interfaceId)
        +purchaseCredit(userId, packageId)
        +consumeCredit(userId, interfaceId, amount)
        +transferCredit(fromUserId, toUserId, amount)
        +getCreditBalance(userId)
        +getCreditHistory(userId)
    }
    
    class PointService {
        +earnPoints(userId, amount, reason)
        +spendPoints(userId, amount, reason)
        +getPointBalance(userId)
        +exchangePointsForCredit(userId, pointAmount)
    }
    
    class CreditPackageService {
        +getAllPackages()
        +getPackageById(packageId)
        +createPackage(packageInfo)
        +updatePackage(packageId, packageInfo)
    }
    
    class OrderService {
        +createOrder(userId, packageId)
        +processPayment(orderId, paymentInfo)
        +confirmOrder(orderId)
        +cancelOrder(orderId)
    }
    
    CreditService --> PointService
    CreditService --> CreditPackageService
    CreditService --> OrderService
```

## 3. 数据模型设计

### 3.1 用户额度表 (user_credit)

```sql
CREATE TABLE user_credit (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    interface_id BIGINT NOT NULL COMMENT '接口ID',
    total_credit BIGINT DEFAULT 0 COMMENT '总额度',
    used_credit BIGINT DEFAULT 0 COMMENT '已使用额度',
    remaining_credit BIGINT DEFAULT 0 COMMENT '剩余额度',
    free_applied TINYINT DEFAULT 0 COMMENT '是否已申请免费额度 0-未申请 1-已申请',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-正常',
    expire_time DATETIME COMMENT '额度过期时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete TINYINT DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
    UNIQUE KEY uk_user_interface (user_id, interface_id),
    INDEX idx_user_id (user_id),
    INDEX idx_interface_id (interface_id)
) COMMENT '用户接口额度表';
```

### 3.2 用户积分表 (user_points)

```sql
CREATE TABLE user_points (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    total_points BIGINT DEFAULT 0 COMMENT '总积分',
    available_points BIGINT DEFAULT 0 COMMENT '可用积分',
    frozen_points BIGINT DEFAULT 0 COMMENT '冻结积分',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete TINYINT DEFAULT 0 COMMENT '是否删除',
    UNIQUE KEY uk_user_id (user_id)
) COMMENT '用户积分表';
```

### 3.3 积分记录表 (point_records)

```sql
CREATE TABLE point_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    point_change BIGINT NOT NULL COMMENT '积分变化量（正数为增加，负数为扣减）',
    balance_after BIGINT NOT NULL COMMENT '变化后余额',
    operation_type VARCHAR(20) NOT NULL COMMENT '操作类型（EARN/SPEND/EXCHANGE/REFUND）',
    reason VARCHAR(100) COMMENT '变化原因',
    related_order_id BIGINT COMMENT '关联订单ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_operation_type (operation_type),
    INDEX idx_create_time (create_time)
) COMMENT '积分变动记录表';
```

### 3.4 额度套餐表 (credit_packages)

```sql
CREATE TABLE credit_packages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    package_name VARCHAR(100) NOT NULL COMMENT '套餐名称',
    package_type VARCHAR(20) NOT NULL COMMENT '套餐类型（BASIC/PREMIUM/ENTERPRISE/CUSTOM）',
    credit_amount BIGINT NOT NULL COMMENT '额度数量',
    price DECIMAL(10,2) NOT NULL COMMENT '价格（元）',
    points_price BIGINT COMMENT '积分价格',
    validity_days INT DEFAULT 365 COMMENT '有效期（天）',
    description TEXT COMMENT '套餐描述',
    is_recommended TINYINT DEFAULT 0 COMMENT '是否推荐 0-否 1-是',
    status TINYINT DEFAULT 1 COMMENT '状态 0-下架 1-上架',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete TINYINT DEFAULT 0 COMMENT '是否删除',
    INDEX idx_package_type (package_type),
    INDEX idx_status (status)
) COMMENT '额度套餐表';
```

### 3.5 额度消费记录表 (credit_records)

```sql
CREATE TABLE credit_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    interface_id BIGINT NOT NULL COMMENT '接口ID',
    operation_type VARCHAR(20) NOT NULL COMMENT '操作类型（CONSUME/RECHARGE/TRANSFER/REFUND）',
    credit_change BIGINT NOT NULL COMMENT '额度变化量',
    balance_before BIGINT NOT NULL COMMENT '操作前余额',
    balance_after BIGINT NOT NULL COMMENT '操作后余额',
    related_order_id BIGINT COMMENT '关联订单ID',
    related_user_id BIGINT COMMENT '关联用户ID（转移时使用）',
    description VARCHAR(200) COMMENT '操作描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_interface_id (interface_id),
    INDEX idx_operation_type (operation_type),
    INDEX idx_create_time (create_time)
) COMMENT '额度操作记录表';
```

### 3.6 订单表 (orders)

```sql
CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    order_no VARCHAR(32) NOT NULL COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    package_id BIGINT NOT NULL COMMENT '套餐ID',
    package_name VARCHAR(100) NOT NULL COMMENT '套餐名称',
    credit_amount BIGINT NOT NULL COMMENT '额度数量',
    original_price DECIMAL(10,2) NOT NULL COMMENT '原价',
    actual_price DECIMAL(10,2) NOT NULL COMMENT '实际支付价格',
    payment_type VARCHAR(20) NOT NULL COMMENT '支付方式（MONEY/POINTS/MIXED）',
    points_used BIGINT DEFAULT 0 COMMENT '使用积分数',
    money_paid DECIMAL(10,2) DEFAULT 0 COMMENT '支付金额',
    order_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '订单状态（PENDING/PAID/COMPLETED/CANCELLED）',
    payment_time DATETIME COMMENT '支付时间',
    completion_time DATETIME COMMENT '完成时间',
    expire_time DATETIME COMMENT '订单过期时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete TINYINT DEFAULT 0 COMMENT '是否删除',
    UNIQUE KEY uk_order_no (order_no),
    INDEX idx_user_id (user_id),
    INDEX idx_order_status (order_status),
    INDEX idx_create_time (create_time)
) COMMENT '订单表';
```

## 4. 核心业务流程

### 4.1 固定额度申请流程

```mermaid
sequenceDiagram
    participant User as 用户
    participant Frontend as 前端
    participant CreditController as 额度控制器
    participant CreditService as 额度服务
    participant DB as 数据库
    
    User ->> Frontend: 申请接口免费额度
    Frontend ->> CreditController: POST /credit/apply-free
    CreditController ->> CreditService: applyCreditLimit(userId, interfaceId)
    
    CreditService ->> DB: 检查是否已申请过免费额度
    
    alt 未申请过
        CreditService ->> DB: 创建/更新用户额度记录
        Note over DB: total_credit += 100<br/>remaining_credit += 100<br/>free_applied = 1
        CreditService ->> DB: 记录额度变动
        CreditService -->> CreditController: 申请成功
        CreditController -->> Frontend: 返回成功结果
        Frontend -->> User: 显示申请成功
    else 已申请过
        CreditService -->> CreditController: 申请失败（已申请过）
        CreditController -->> Frontend: 返回错误信息
        Frontend -->> User: 显示已申请过的提示
    end
```

### 4.2 积分兑换额度流程

```mermaid
sequenceDiagram
    participant User as 用户
    participant Frontend as 前端
    participant CreditController as 额度控制器
    participant CreditService as 额度服务
    participant PointService as 积分服务
    participant DB as 数据库
    
    User ->> Frontend: 选择积分兑换额度
    Frontend ->> CreditController: POST /credit/exchange-points
    CreditController ->> CreditService: exchangePointsForCredit(userId, pointAmount)
    
    CreditService ->> PointService: 检查积分余额
    PointService ->> DB: 查询用户积分
    
    alt 积分充足
        CreditService ->> PointService: 扣减积分
        PointService ->> DB: 更新积分余额
        PointService ->> DB: 记录积分变动
        
        CreditService ->> DB: 增加额度
        CreditService ->> DB: 记录额度变动
        
        CreditService -->> CreditController: 兑换成功
        CreditController -->> Frontend: 返回成功结果
        Frontend -->> User: 显示兑换成功
    else 积分不足
        PointService -->> CreditService: 积分不足
        CreditService -->> CreditController: 兑换失败
        CreditController -->> Frontend: 返回错误信息
        Frontend -->> User: 显示积分不足
    end
```

### 4.3 额度消费流程

```mermaid
sequenceDiagram
    participant Gateway as API网关
    participant CreditService as 额度服务
    participant UserInterfaceService as 用户接口服务
    participant DB as 数据库
    participant Interface as 第三方接口
    
    Gateway ->> CreditService: 请求前检查额度
    CreditService ->> DB: 查询用户剩余额度
    
    alt 额度充足
        CreditService -->> Gateway: 额度检查通过
        Gateway ->> Interface: 转发API请求
        Interface -->> Gateway: 返回响应
        
        alt API调用成功
            Gateway ->> CreditService: 扣减额度
            CreditService ->> DB: 更新额度余额
            CreditService ->> DB: 记录消费记录
            
            Gateway ->> UserInterfaceService: 更新调用统计
            UserInterfaceService ->> DB: 更新leftNum和totalNum
        end
        
        Gateway -->> 用户: 返回API响应
    else 额度不足
        CreditService -->> Gateway: 额度不足
        Gateway -->> 用户: 返回额度不足错误
    end
```

## 5. API接口设计

### 5.1 额度申请接口

```java
/**
 * 申请接口免费额度
 */
@PostMapping("/apply-free")
public BaseResponse<Boolean> applyCreditLimit(@RequestBody CreditApplyRequest request, HttpServletRequest httpRequest) {
    // 参数校验
    if (request == null || request.getInterfaceId() <= 0) {
        throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    
    // 获取当前用户
    User loginUser = userService.getLoginUser(httpRequest);
    
    // 申请免费额度
    boolean result = creditService.applyCreditLimit(loginUser.getId(), request.getInterfaceId());
    
    return ResultUtils.success(result);
}
```

### 5.2 积分兑换额度接口

```java
/**
 * 使用积分兑换额度
 */
@PostMapping("/exchange-points")
public BaseResponse<Boolean> exchangePointsForCredit(@RequestBody PointExchangeRequest request, HttpServletRequest httpRequest) {
    // 参数校验
    if (request == null || request.getPointAmount() <= 0 || request.getInterfaceId() <= 0) {
        throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    
    // 获取当前用户
    User loginUser = userService.getLoginUser(httpRequest);
    
    // 积分兑换额度
    boolean result = creditService.exchangePointsForCredit(
        loginUser.getId(), 
        request.getInterfaceId(),
        request.getPointAmount()
    );
    
    return ResultUtils.success(result);
}
```

### 5.3 额度套餐购买接口

```java
/**
 * 购买额度套餐
 */
@PostMapping("/purchase-package")
public BaseResponse<String> purchaseCreditPackage(@RequestBody PackagePurchaseRequest request, HttpServletRequest httpRequest) {
    // 参数校验
    if (request == null || request.getPackageId() <= 0) {
        throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    
    // 获取当前用户
    User loginUser = userService.getLoginUser(httpRequest);
    
    // 创建订单
    String orderNo = orderService.createOrder(loginUser.getId(), request);
    
    return ResultUtils.success(orderNo);
}
```

### 5.4 额度查询接口

```java
/**
 * 查询用户额度余额
 */
@GetMapping("/balance")
public BaseResponse<List<CreditBalanceVO>> getCreditBalance(HttpServletRequest httpRequest) {
    // 获取当前用户
    User loginUser = userService.getLoginUser(httpRequest);
    
    // 查询用户所有接口的额度余额
    List<CreditBalanceVO> balances = creditService.getCreditBalance(loginUser.getId());
    
    return ResultUtils.success(balances);
}
```

## 6. 业务规则设计

### 6.1 固定额度申请规则

| 规则项 | 规则内容 |
|--------|----------|
| 申请条件 | 用户必须已注册并完成实名认证 |
| 额度数量 | 每个接口可申请100次免费调用额度 |
| 申请次数 | 每个用户对每个接口只能申请一次免费额度 |
| 有效期 | 免费额度永久有效（除非用户违规） |
| 限制条件 | 被封禁用户不能申请免费额度 |

### 6.2 积分体系规则

| 积分来源 | 获得积分 | 获得条件 |
|----------|----------|----------|
| 注册奖励 | 100积分 | 完成注册并实名认证 |
| 每日签到 | 10积分 | 每日首次登录 |
| 接口调用 | 1积分/次 | 成功调用API接口 |
| 邀请好友 | 50积分 | 好友完成注册 |
| 分享推广 | 20积分 | 分享接口给他人使用 |

| 积分消费 | 所需积分 | 兑换内容 |
|----------|----------|----------|
| 额度兑换 | 10积分 | 1次API调用额度 |
| 高级功能 | 100积分 | 开通某些高级功能 |
| 实物奖品 | 1000积分+ | 周边产品等 |

### 6.3 额度套餐设计

```mermaid
graph TB
    subgraph "基础套餐"
        B1[基础包 - 1000次额度<br/>价格: 9.9元 / 100积分]
        B2[标准包 - 5000次额度<br/>价格: 39.9元 / 400积分]
        B3[专业包 - 20000次额度<br/>价格: 129.9元 / 1300积分]
    end
    
    subgraph "企业套餐"
        E1[企业基础包 - 50000次额度<br/>价格: 299元]
        E2[企业标准包 - 200000次额度<br/>价格: 999元]
        E3[企业旗舰包 - 1000000次额度<br/>价格: 3999元]
    end
    
    subgraph "特殊套餐"
        S1[学生优惠包 - 2000次额度<br/>价格: 19.9元]
        S2[开发者试用包 - 500次额度<br/>价格: 免费]
    end
```

## 7. 监控与告警机制

### 7.1 额度监控指标

```mermaid
graph LR
    subgraph "用户维度监控"
        U1[额度余额预警]
        U2[使用频率异常]
        U3[额度即将过期]
    end
    
    subgraph "接口维度监控"
        I1[接口调用总量]
        I2[额度消耗排行]
        I3[接口成功率]
    end
    
    subgraph "系统维度监控"
        S1[总额度池监控]
        S2[收入统计]
        S3[异常交易监控]
    end
```

### 7.2 告警策略

| 告警类型 | 触发条件 | 告警方式 | 处理建议 |
|----------|----------|----------|----------|
| 额度不足 | 剩余额度 < 10次 | 站内信 + 邮件 | 提醒用户购买额度 |
| 额度即将过期 | 距离过期 < 7天 | 站内信 | 提醒用户及时使用 |
| 异常消费 | 短时间大量消费 | 实时告警 | 人工审核账户 |
| 支付异常 | 支付失败率 > 5% | 实时告警 | 检查支付系统 |

## 8. 性能优化策略

### 8.1 缓存策略

```mermaid
graph TB
    subgraph "Redis缓存层"
        R1[用户额度缓存<br/>Key: credit:user:{userId}:{interfaceId}<br/>TTL: 1小时]
        R2[积分余额缓存<br/>Key: points:user:{userId}<br/>TTL: 30分钟]
        R3[套餐信息缓存<br/>Key: packages:all<br/>TTL: 24小时]
        R4[热点接口缓存<br/>Key: interface:hot<br/>TTL: 1小时]
    end
    
    subgraph "本地缓存"
        L1[套餐配置缓存]
        L2[汇率配置缓存]
        L3[系统配置缓存]
    end
    
    R1 --> DB[(数据库)]
    R2 --> DB
    R3 --> DB
    R4 --> DB
    L1 --> R3
    L2 --> Redis
    L3 --> Redis
```

### 8.2 数据库优化

1. **分表策略**: 按用户ID哈希分表，将额度记录表分为16个子表
2. **索引优化**: 在高频查询字段上建立联合索引
3. **读写分离**: 读操作走从库，写操作走主库
4. **定期清理**: 定期清理过期的记录数据

### 8.3 并发控制

```mermaid
sequenceDiagram
    participant Client as 客户端
    participant Redis as Redis分布式锁
    participant Service as 额度服务
    participant DB as 数据库
    
    Client ->> Redis: 获取分布式锁
    Redis -->> Client: 锁获取成功
    
    Client ->> Service: 执行额度操作
    Service ->> DB: 更新额度
    Service ->> Redis: 更新缓存
    Service -->> Client: 操作完成
    
    Client ->> Redis: 释放锁
```

## 9. 安全机制

### 9.1 防刷机制

1. **接口限流**: 使用令牌桶算法限制接口调用频率
2. **IP限制**: 同一IP在短时间内的请求次数限制
3. **用户行为检测**: 检测异常的用户行为模式
4. **验证码保护**: 敏感操作需要验证码确认

### 9.2 数据安全

1. **敏感数据加密**: 对关键字段进行加密存储
2. **操作日志**: 记录所有额度相关的操作日志
3. **权限控制**: 严格的角色权限控制
4. **审计追踪**: 完整的操作审计链路

## 10. 运营策略

### 10.1 营销活动设计

| 活动类型 | 活动内容 | 参与条件 | 奖励机制 |
|----------|----------|----------|----------|
| 新用户礼包 | 注册送500次额度 | 完成实名认证 | 一次性奖励 |
| 充值返利 | 充值满100送20% | 单次充值≥100元 | 额外额度奖励 |
| 推荐有奖 | 邀请好友奖励 | 好友完成首次充值 | 双方都获得奖励 |
| 节日活动 | 特殊节日优惠 | 全体用户 | 限时折扣 |

### 10.2 用户分层策略

```mermaid
pyramid
    title 用户分层金字塔
    section 钻石用户
        monthly_spending >= 1000元
        exclusive_support
        priority_access
    section 黄金用户  
        monthly_spending >= 300元
        dedicated_support
        early_features
    section 银牌用户
        monthly_spending >= 100元
        email_support
        regular_features
    section 普通用户
        monthly_spending < 100元
        community_support
        basic_features
```

## 11. 技术实现要点

### 11.1 关键算法

1. **额度扣减原子性**
```java
// 使用数据库原子操作确保并发安全
UPDATE user_credit 
SET remaining_credit = remaining_credit - 1, 
    used_credit = used_credit + 1 
WHERE user_id = ? AND interface_id = ? AND remaining_credit > 0;
```

2. **分布式锁防并发**
```java
@Transactional
public boolean consumeCredit(Long userId, Long interfaceId) {
    String lockKey = "credit:lock:" + userId + ":" + interfaceId;
    return redisTemplate.execute(new RedisCallback<Boolean>() {
        @Override
        public Boolean doInRedis(RedisConnection connection) {
            // 获取分布式锁
            Boolean lockAcquired = connection.setNX(lockKey.getBytes(), "1".getBytes());
            if (lockAcquired) {
                connection.expire(lockKey.getBytes(), 10); // 10秒过期
                try {
                    // 执行额度扣减逻辑
                    return doConsumeCredit(userId, interfaceId);
                } finally {
                    connection.del(lockKey.getBytes());
                }
            }
            return false;
        }
    });
}
```

### 11.2 错误处理机制

| 错误码 | 描述 | 处理方式 |
|--------|------|----------|
| 40001 | 额度不足 | 提示购买额度 |
| 40002 | 已申请过免费额度 | 引导购买套餐 |
| 40003 | 积分不足 | 提示充值积分 |
| 50001 | 系统异常 | 自动重试 |

### 11.3 性能优化

1. **缓存策略**: Redis缓存用户额度信息，TTL设置为1小时
2. **读写分离**: 查询操作走从库，更新操作走主库
3. **异步处理**: 积分奖励等非核心操作异步执行
4. **批量操作**: 支持批量额度操作以提高性能
