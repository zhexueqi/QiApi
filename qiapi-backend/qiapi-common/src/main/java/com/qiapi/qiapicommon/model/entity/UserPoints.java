package com.qiapi.qiapicommon.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户积分实体
 * @author zhexueqi
 */
@TableName(value = "user_points")
@Data
public class UserPoints implements Serializable {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("userId")
    private Long userId;

    /**
     * 总积分
     */
    @TableField("totalPoints")
    private Long totalPoints;

    /**
     * 可用积分
     */
    @TableField("availablePoints")
    private Long availablePoints;

    /**
     * 冻结积分
     */
    @TableField("frozenPoints")
    private Long frozenPoints;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}