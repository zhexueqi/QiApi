package com.qiapi.project.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单视图对象
 */
@Data
public class OrderVO implements Serializable {

    /**
     * 订单ID
     */
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 套餐ID
     */
    private Long packageId;

    /**
     * 套餐名称
     */
    private String packageName;

    /**
     * 额度数量
     */
    private Long creditAmount;

    /**
     * 原价
     */
    private BigDecimal originalPrice;

    /**
     * 实际支付价格
     */
    private BigDecimal actualPrice;

    /**
     * 支付方式（MONEY/POINTS/MIXED）
     */
    private String paymentType;

    /**
     * 使用积分数
     */
    private Long pointsUsed;

    /**
     * 支付金额
     */
    private BigDecimal moneyPaid;

    /**
     * 订单状态（PENDING/PAID/COMPLETED/CANCELLED）
     */
    private String orderStatus;

    /**
     * 支付时间
     */
    private Date paymentTime;

    /**
     * 完成时间
     */
    private Date completionTime;

    /**
     * 订单过期时间
     */
    private Date expireTime;

    /**
     * 创建时间
     */
    private Date createTime;

    private static final long serialVersionUID = 1L;
}