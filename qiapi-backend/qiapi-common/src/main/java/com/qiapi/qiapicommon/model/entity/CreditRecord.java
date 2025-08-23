package com.qiapi.qiapicommon.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 额度操作记录实体
 */
@TableName(value = "credit_records")
@Data
public class CreditRecord implements Serializable {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 接口ID
     */
    private Long interfaceId;

    /**
     * 操作类型（CONSUME/RECHARGE/TRANSFER/REFUND）
     */
    private String operationType;

    /**
     * 额度变化量
     */
    private Long creditChange;

    /**
     * 操作前余额
     */
    private Long balanceBefore;

    /**
     * 操作后余额
     */
    private Long balanceAfter;

    /**
     * 关联订单ID
     */
    private Long relatedOrderId;

    /**
     * 关联用户ID（转移时使用）
     */
    private Long relatedUserId;

    /**
     * 操作描述
     */
    private String description;

    /**
     * 创建时间
     */
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}