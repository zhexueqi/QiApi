-- 额度系统相关表结构

-- 用户额度表
CREATE TABLE user_credit (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    userId BIGINT NOT NULL COMMENT '用户ID',
    interfaceId BIGINT NOT NULL COMMENT '接口ID',
    interfaceName VARCHAR(255) NOT NULL COMMENT '接口名称',
    totalCredit BIGINT DEFAULT 0 COMMENT '总额度',
    usedCredit BIGINT DEFAULT 0 COMMENT '已使用额度',
    remainingCredit BIGINT DEFAULT 0 COMMENT '剩余额度',
    freeApplied TINYINT DEFAULT 0 COMMENT '是否已申请免费额度 0-未申请 1-已申请',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-正常',
    expireTime DATETIME COMMENT '额度过期时间',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    isDelete TINYINT DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
    UNIQUE KEY uk_user_interface (userId, interfaceId),
    INDEX idx_user_id (userId),
    INDEX idx_interface_id (interfaceId)
) COMMENT '用户接口额度表';

-- 用户积分表
CREATE TABLE user_points (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    userId BIGINT NOT NULL COMMENT '用户ID',
    totalPoints BIGINT DEFAULT 0 COMMENT '总积分',
    availablePoints BIGINT DEFAULT 0 COMMENT '可用积分',
    frozenPoints BIGINT DEFAULT 0 COMMENT '冻结积分',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    isDelete TINYINT DEFAULT 0 COMMENT '是否删除',
    UNIQUE KEY uk_user_id (userId)
) COMMENT '用户积分表';

-- 积分记录表
CREATE TABLE point_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    userId BIGINT NOT NULL COMMENT '用户ID',
    pointChange BIGINT NOT NULL COMMENT '积分变化量（正数为增加，负数为扣减）',
    balanceAfter BIGINT NOT NULL COMMENT '变化后余额',
    operationType VARCHAR(20) NOT NULL COMMENT '操作类型（EARN/SPEND/EXCHANGE/REFUND）',
    reason VARCHAR(100) COMMENT '变化原因',
    relatedOrderId BIGINT COMMENT '关联订单ID',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (userId),
    INDEX idx_operation_type (operationType),
    INDEX idx_create_time (createTime)
) COMMENT '积分变动记录表';

-- 额度套餐表
CREATE TABLE credit_packages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    packageName VARCHAR(100) NOT NULL COMMENT '套餐名称',
    packageType VARCHAR(20) NOT NULL COMMENT '套餐类型（BASIC/PREMIUM/ENTERPRISE/CUSTOM）',
    creditAmount BIGINT NOT NULL COMMENT '额度数量',
    price DECIMAL(10,2) NOT NULL COMMENT '价格（元）',
    pointsPrice BIGINT COMMENT '积分价格',
    validityDays INT DEFAULT 365 COMMENT '有效期（天）',
    description TEXT COMMENT '套餐描述',
    isRecommended TINYINT DEFAULT 0 COMMENT '是否推荐 0-否 1-是',
    status TINYINT DEFAULT 1 COMMENT '状态 0-下架 1-上架',
    sortOrder INT DEFAULT 0 COMMENT '排序',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    isDelete TINYINT DEFAULT 0 COMMENT '是否删除',
    INDEX idx_package_type (packageType),
    INDEX idx_status (status)
) COMMENT '额度套餐表';

-- 额度消费记录表
CREATE TABLE credit_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    userId BIGINT NOT NULL COMMENT '用户ID',
    interfaceId BIGINT NOT NULL COMMENT '接口ID',
    operationType VARCHAR(20) NOT NULL COMMENT '操作类型（CONSUME/RECHARGE/TRANSFER/REFUND）',
    creditChange BIGINT NOT NULL COMMENT '额度变化量',
    balanceBefore BIGINT NOT NULL COMMENT '操作前余额',
    balanceAfter BIGINT NOT NULL COMMENT '操作后余额',
    relatedOrderId BIGINT COMMENT '关联订单ID',
    relatedUserId BIGINT COMMENT '关联用户ID（转移时使用）',
    description VARCHAR(200) COMMENT '操作描述',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (userId),
    INDEX idx_interface_id (interfaceId),
    INDEX idx_operation_type (operationType),
    INDEX idx_create_time (createTime)
) COMMENT '额度操作记录表';

-- 订单表
CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    orderNo VARCHAR(32) NOT NULL COMMENT '订单号',
    userId BIGINT NOT NULL COMMENT '用户ID',
    packageId BIGINT NOT NULL COMMENT '套餐ID',
    packageName VARCHAR(100) NOT NULL COMMENT '套餐名称',
    creditAmount BIGINT NOT NULL COMMENT '额度数量',
    originalPrice DECIMAL(10,2) NOT NULL COMMENT '原价',
    actualPrice DECIMAL(10,2) NOT NULL COMMENT '实际支付价格',
    paymentType VARCHAR(20) NOT NULL COMMENT '支付方式（MONEY/POINTS/MIXED）',
    pointsUsed BIGINT DEFAULT 0 COMMENT '使用积分数',
    moneyPaid DECIMAL(10,2) DEFAULT 0 COMMENT '支付金额',
    orderStatus VARCHAR(20) DEFAULT 'PENDING' COMMENT '订单状态（PENDING/PAID/COMPLETED/CANCELLED）',
    paymentTime DATETIME COMMENT '支付时间',
    completionTime DATETIME COMMENT '完成时间',
    expireTime DATETIME COMMENT '订单过期时间',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    isDelete TINYINT DEFAULT 0 COMMENT '是否删除',
    UNIQUE KEY uk_order_no (orderNo),
    INDEX idx_user_id (userId),
    INDEX idx_order_status (orderStatus),
    INDEX idx_create_time (createTime)
) COMMENT '订单表';

-- 插入默认积分配置数据
INSERT INTO user_points (userId, totalPoints, availablePoints, frozenPoints) 
SELECT id, 100, 100, 0 FROM user WHERE id NOT IN (SELECT userId FROM user_points WHERE isDelete = 0);

-- 插入默认额度套餐数据
INSERT INTO credit_packages (packageName, packageType, creditAmount, price, pointsPrice, validityDays, description, isRecommended, status, sortOrder) VALUES
('基础套餐', 'BASIC', 1000, 9.90, 100, 365, '适合个人开发者，提供1000次API调用额度', 0, 1, 1),
('标准套餐', 'BASIC', 5000, 39.90, 400, 365, '适合小型项目，提供5000次API调用额度', 1, 1, 2),
('专业套餐', 'PREMIUM', 20000, 129.90, 1300, 365, '适合中型项目，提供20000次API调用额度', 1, 1, 3),
('企业基础套餐', 'ENTERPRISE', 50000, 299.00, 3000, 365, '适合企业用户，提供50000次API调用额度', 0, 1, 4),
('企业标准套餐', 'ENTERPRISE', 200000, 999.00, 10000, 365, '适合大型企业，提供200000次API调用额度', 1, 1, 5),
('企业高级套餐', 'ENTERPRISE', 1000000, 3999.00, 40000, 365, '适合超大型企业，提供1000000次API调用额度', 0, 1, 6);