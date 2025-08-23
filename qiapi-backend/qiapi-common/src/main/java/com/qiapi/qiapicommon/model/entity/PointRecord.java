package com.qiapi.qiapicommon.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 积分变动记录实体
 */
@TableName(value = "point_records")
@Data
public class PointRecord implements Serializable {

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
     * 积分变化量（正数为增加，负数为扣减）
     */
    private Long pointChange;

    /**
     * 变化后余额
     */
    private Long balanceAfter;

    /**
     * 操作类型（EARN/SPEND/EXCHANGE/REFUND）
     */
    private String operationType;

    /**
     * 变化原因
     */
    private String reason;

    /**
     * 关联订单ID
     */
    private Long relatedOrderId;

    /**
     * 创建时间
     */
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}