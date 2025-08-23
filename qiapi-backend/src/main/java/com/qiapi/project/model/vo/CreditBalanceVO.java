package com.qiapi.project.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 额度余额视图对象
 */
@Data
public class CreditBalanceVO implements Serializable {

    /**
     * 接口ID
     */
    private Long interfaceId;

    /**
     * 接口名称
     */
    private String interfaceName;

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
     * 是否已申请免费额度
     */
    private Boolean freeApplied;

    /**
     * 额度状态 0-禁用 1-正常
     */
    private Integer status;

    /**
     * 额度过期时间
     */
    private Date expireTime;

    private static final long serialVersionUID = 1L;
}