package com.qiapi.project.model.dto.credit;

import lombok.Data;

import java.io.Serializable;

/**
 * 额度申请请求
 */
@Data
public class CreditApplyRequest implements Serializable {

    /**
     * 接口ID
     */
    private Long interfaceId;

    private static final long serialVersionUID = 1L;
}