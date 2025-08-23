package com.qiapi.qiapicommon.service;

/**
 * 内部额度服务（供Dubbo调用）
 */
public interface InnerCreditService {

    /**
     * 检查用户接口额度是否充足
     * @param userId 用户ID
     * @param interfaceId 接口ID
     * @param amount 需要的额度
     * @return 是否充足
     */
    boolean checkCreditSufficient(Long userId, Long interfaceId, Long amount);

    /**
     * 消费接口额度
     * @param userId 用户ID
     * @param interfaceId 接口ID
     * @param amount 消费数量
     * @return 是否成功
     */
    boolean consumeCredit(Long userId, Long interfaceId, Long amount);
}