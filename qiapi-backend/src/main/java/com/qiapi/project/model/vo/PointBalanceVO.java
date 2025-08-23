package com.qiapi.project.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 积分余额视图对象
 */
@Data
public class PointBalanceVO implements Serializable {

    /**
     * 总积分
     */
    private Long totalPoints;

    /**
     * 可用积分
     */
    private Long availablePoints;

    /**
     * 冻结积分
     */
    private Long frozenPoints;

    private static final long serialVersionUID = 1L;
}