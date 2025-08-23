-- 额度系统相关表结构

-- 用户额度表
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

-- 用户积分表
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

-- 积分记录表
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

-- 额度套餐表
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

-- 额度消费记录表
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

-- 订单表
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

-- 插入默认积分配置数据
INSERT INTO user_points (user_id, total_points, available_points, frozen_points) 
SELECT id, 100, 100, 0 FROM user WHERE id NOT IN (SELECT user_id FROM user_points WHERE is_delete = 0);

-- 插入默认额度套餐数据
INSERT INTO credit_packages (package_name, package_type, credit_amount, price, points_price, validity_days, description, is_recommended, status, sort_order) VALUES
('基础套餐', 'BASIC', 1000, 9.90, 100, 365, '适合个人开发者，提供1000次API调用额度', 0, 1, 1),
('标准套餐', 'BASIC', 5000, 39.90, 400, 365, '适合小型项目，提供5000次API调用额度', 1, 1, 2),
('专业套餐', 'PREMIUM', 20000, 129.90, 1300, 365, '适合中型项目，提供20000次API调用额度', 1, 1, 3),
('企业基础套餐', 'ENTERPRISE', 50000, 299.00, 3000, 365, '适合企业用户，提供50000次API调用额度', 0, 1, 4),
('企业标准套餐', 'ENTERPRISE', 200000, 999.00, 10000, 365, '适合大型企业，提供200000次API调用额度', 1, 1, 5),
('企业高级套餐', 'ENTERPRISE', 1000000, 3999.00, 40000, 365, '适合超大型企业，提供1000000次API调用额度', 0, 1, 6);