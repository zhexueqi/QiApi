package com.qiapi.qiapicommon.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户接口额度实体
 */
@TableName(value = "user_credit")
@Data
public class UserCredit implements Serializable {

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
     * 总额度
     */
    private Long totalCredit;

    /**
     * 已使用额度
     */
    private Long usedCredit;

    /**
     * 剩余额度
     */
    private Long remainingCredit;

    /**
     * 是否已申请免费额度 0-未申请 1-已申请
     */
    private Integer freeApplied;

    /**
     * 状态 0-禁用 1-正常
     */
    private Integer status;

    /**
     * 额度过期时间
     */
    private Date expireTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除 0-未删除 1-已删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}