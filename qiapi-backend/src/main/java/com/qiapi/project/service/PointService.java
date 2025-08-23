package com.qiapi.project.service;

import com.qiapi.project.model.vo.PointBalanceVO;

/**
 * 积分服务
 */
public interface PointService {

    /**
     * 获得积分
     * @param userId 用户ID
     * @param amount 积分数量
     * @param reason 获得原因
     * @return 是否成功
     */
    boolean earnPoints(Long userId, Long amount, String reason);

    /**
     * 消费积分
     * @param userId 用户ID
     * @param amount 积分数量
     * @param reason 消费原因
     * @return 是否成功
     */
    boolean spendPoints(Long userId, Long amount, String reason);

    /**
     * 查询用户积分余额
     * @param userId 用户ID
     * @return 积分余额信息
     */
    PointBalanceVO getPointBalance(Long userId);

    /**
     * 初始化用户积分账户
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean initUserPoints(Long userId);

    /**
     * 检查用户积分是否充足
     * @param userId 用户ID
     * @param amount 需要的积分
     * @return 是否充足
     */
    boolean checkPointsSufficient(Long userId, Long amount);
}