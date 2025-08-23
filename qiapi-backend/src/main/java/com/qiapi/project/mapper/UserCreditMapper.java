package com.qiapi.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qiapi.qiapicommon.model.entity.UserCredit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户额度数据库操作
 */
@Mapper
public interface UserCreditMapper extends BaseMapper<UserCredit> {

    /**
     * 扣减用户接口额度
     * @param userId 用户ID
     * @param interfaceId 接口ID
     * @param amount 扣减数量
     * @return 影响行数
     */
    int deductCredit(@Param("userId") Long userId, @Param("interfaceId") Long interfaceId, @Param("amount") Long amount);

    /**
     * 增加用户接口额度
     * @param userId 用户ID
     * @param interfaceId 接口ID
     * @param amount 增加数量
     * @return 影响行数
     */
    int addCredit(@Param("userId") Long userId, @Param("interfaceId") Long interfaceId, @Param("amount") Long amount);
}