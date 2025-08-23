package com.qiapi.project.service;

import com.qiapi.project.model.vo.CreditBalanceVO;

import java.util.List;

/**
 * 额度服务
 */
public interface CreditService {

    /**
     * 申请接口免费额度
     * @param userId 用户ID
     * @param interfaceId 接口ID
     * @return 是否申请成功
     */
    boolean applyCreditLimit(Long userId, Long interfaceId);

    /**
     * 消费接口额度
     * @param userId 用户ID
     * @param interfaceId 接口ID
     * @param amount 消费数量
     * @return 是否消费成功
     */
    boolean consumeCredit(Long userId, Long interfaceId, Long amount);

    /**
     * 充值接口额度
     * @param userId 用户ID
     * @param interfaceId 接口ID
     * @param amount 充值数量
     * @param description 充值描述
     * @return 是否充值成功
     */
    boolean rechargeCredit(Long userId, Long interfaceId, Long amount, String description);

    /**
     * 使用积分兑换额度
     * @param userId 用户ID
     * @param interfaceId 接口ID
     * @param pointAmount 积分数量
     * @return 是否兑换成功
     */
    boolean exchangePointsForCredit(Long userId, Long interfaceId, Long pointAmount);

    /**
     * 查询用户额度余额
     * @param userId 用户ID
     * @return 额度余额列表
     */
    List<CreditBalanceVO> getCreditBalance(Long userId);

    /**
     * 检查用户是否有足够额度
     * @param userId 用户ID
     * @param interfaceId 接口ID
     * @param amount 需要的额度
     * @return 是否有足够额度
     */
    boolean checkCreditSufficient(Long userId, Long interfaceId, Long amount);
}