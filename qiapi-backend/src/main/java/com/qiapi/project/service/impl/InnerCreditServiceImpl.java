package com.qiapi.project.service.impl;

import com.qiapi.project.service.CreditService;
import com.qiapi.qiapicommon.model.entity.User;
import com.qiapi.qiapicommon.service.InnerCreditService;
import com.qiapi.qiapicommon.service.InnerUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;

/**
 * 内部额度服务实现（供Dubbo调用）
 */
@Slf4j
@DubboService
public class InnerCreditServiceImpl implements InnerCreditService {

    @Resource
    private CreditService creditService;

    @Resource
    private InnerUserService innerUserService;

    @Override
    public boolean checkCreditSufficient(Long userId, Long interfaceId, Long amount) {
        return creditService.checkCreditSufficient(userId, interfaceId, amount);
    }

    @Override
    public boolean consumeCredit(Long userId, Long interfaceId, Long amount) {
        return creditService.consumeCredit(userId, interfaceId, amount);
    }


}