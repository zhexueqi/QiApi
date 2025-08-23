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

    @Override
    public boolean checkCreditSufficientBySession(String sessionId, Long interfaceId, Long amount) {
        try {
            // 通过SessionID获取用户信息
            User user = innerUserService.getUserBySessionId(sessionId);
            if (user == null || user.getId() == null) {
                log.warn("无法通过SessionID获取用户信息 - SessionID: {}", sessionId);
                return false;
            }

            // 调用原有的检查方法
            return creditService.checkCreditSufficient(user.getId(), interfaceId, amount);
        } catch (Exception e) {
            log.error("基于SessionID检查额度失败 - SessionID: {}, InterfaceID: {}, Amount: {}", 
                     sessionId, interfaceId, amount, e);
            return false;
        }
    }

    @Override
    public boolean consumeCreditBySession(String sessionId, Long interfaceId, Long amount) {
        try {
            // 通过SessionID获取用户信息
            User user = innerUserService.getUserBySessionId(sessionId);
            if (user == null || user.getId() == null) {
                log.warn("无法通过SessionID获取用户信息 - SessionID: {}", sessionId);
                return false;
            }

            // 调用原有的消费方法
            return creditService.consumeCredit(user.getId(), interfaceId, amount);
        } catch (Exception e) {
            log.error("基于SessionID消费额度失败 - SessionID: {}, InterfaceID: {}, Amount: {}", 
                     sessionId, interfaceId, amount, e);
            return false;
        }
    }
}