package com.qiapi.project.model.dto.credit;

import lombok.Data;

import java.io.Serializable;

/**
 * 积分兑换额度请求
 */
@Data
public class PointExchangeRequest implements Serializable {

    /**
     * 接口ID
     */
    private Long interfaceId;

    /**
     * 使用积分数量
     */
    private Long pointAmount;

    private static final long serialVersionUID = 1L;
}