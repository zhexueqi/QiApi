package com.qiapi.project.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 套餐信息视图对象
 */
@Data
public class CreditPackageVO implements Serializable {

    /**
     * 套餐ID
     */
    private Long id;

    /**
     * 套餐名称
     */
    private String packageName;

    /**
     * 套餐类型
     */
    private String packageType;

    /**
     * 额度数量
     */
    private Long creditAmount;

    /**
     * 价格（元）
     */
    private BigDecimal price;

    /**
     * 积分价格
     */
    private Long pointsPrice;

    /**
     * 有效期（天）
     */
    private Integer validityDays;

    /**
     * 套餐描述
     */
    private String description;

    /**
     * 是否推荐
     */
    private Boolean isRecommended;

    private static final long serialVersionUID = 1L;
}