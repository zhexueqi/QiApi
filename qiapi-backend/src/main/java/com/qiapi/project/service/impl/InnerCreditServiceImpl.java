package com.qiapi.project.service.impl;

import com.qiapi.project.service.CreditService;
import com.qiapi.qiapicommon.service.InnerCreditService;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;

/**
 * 内部额度服务实现（供Dubbo调用）
 */
@DubboService
public class InnerCreditServiceImpl implements InnerCreditService {

    @Resource
    private CreditService creditService;

    @Override
    public boolean checkCreditSufficient(Long userId, Long interfaceId, Long amount) {
        return creditService.checkCreditSufficient(userId, interfaceId, amount);
    }

    @Override
    public boolean consumeCredit(Long userId, Long interfaceId, Long amount) {
        return creditService.consumeCredit(userId, interfaceId, amount);
    }
}