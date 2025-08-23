package com.qiapi.qiapicommon.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 额度套餐实体
 */
@TableName(value = "credit_packages")
@Data
public class CreditPackage implements Serializable {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 套餐名称
     */
    private String packageName;

    /**
     * 套餐类型（BASIC/PREMIUM/ENTERPRISE/CUSTOM）
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
     * 是否推荐 0-否 1-是
     */
    private Integer isRecommended;

    /**
     * 状态 0-下架 1-上架
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}