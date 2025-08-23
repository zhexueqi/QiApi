package com.qiapi.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qiapi.project.model.vo.CreditAnalysisVO;
import com.qiapi.project.model.vo.UserCreditStatsVO;
import com.qiapi.qiapicommon.model.entity.UserCredit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户额度数据库操作
 * 
 * @author zhexueqi
 */
@Mapper
public interface UserCreditMapper extends BaseMapper<UserCredit> {

    /**
     * 扣减用户接口额度
     * 
     * @param userId      用户ID
     * @param interfaceId 接口ID
     * @param amount      扣减数量
     * @return 影响行数
     */
    int deductCredit(@Param("userId") Long userId, @Param("interfaceId") Long interfaceId,
            @Param("amount") Long amount);

    /**
     * 增加用户接口额度
     * 
     * @param userId      用户ID
     * @param interfaceId 接口ID
     * @param amount      增加数量
     * @return 影响行数
     */
    int addCredit(@Param("userId") Long userId, @Param("interfaceId") Long interfaceId, @Param("amount") Long amount);

    /**
     * 获取接口额度使用排行榜
     * 
     * @param limit 限制数量
     * @return 接口额度统计列表
     */
    List<CreditAnalysisVO> listTopCreditConsumedInterfaces(@Param("limit") int limit);

    /**
     * 获取用户额度消费排行榜
     * 
     * @param limit 限制数量
     * @return 用户额度统计列表
     */
    List<UserCreditStatsVO> listTopCreditConsumingUsers(@Param("limit") int limit);

    /**
     * 获取所有接口的额度统计信息
     * 
     * @return 接口额度统计列表
     */
    List<CreditAnalysisVO> getAllInterfacesCreditStats();

    /**
     * 获取低额度用户列表
     * 
     * @param threshold 剩余额度阈值
     * @param limit     限制数量
     * @return 用户额度统计列表
     */
    List<UserCreditStatsVO> listLowCreditUsers(@Param("threshold") Long threshold, @Param("limit") int limit);
}