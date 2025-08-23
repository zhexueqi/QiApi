package com.qiapi.project.model.dto.credit;

import lombok.Data;

import java.io.Serializable;

/**
 * 套餐购买请求
 */
@Data
public class PackagePurchaseRequest implements Serializable {

    /**
     * 套餐ID
     */
    private Long packageId;

    /**
     * 支付方式（MONEY/POINTS/MIXED）
     */
    private String paymentType;

    /**
     * 使用积分数量（支付方式为POINTS或MIXED时必填）
     */
    private Long pointsUsed;

    private static final long serialVersionUID = 1L;
}