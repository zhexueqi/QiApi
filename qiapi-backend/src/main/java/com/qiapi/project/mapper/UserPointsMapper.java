package com.qiapi.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qiapi.qiapicommon.model.entity.UserPoints;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户积分数据库操作
 * @author zhexueqi
 */
@Mapper
public interface UserPointsMapper extends BaseMapper<UserPoints> {

    /**
     * 扣减用户积分
     * @param userId 用户ID
     * @param amount 扣减数量
     * @return 影响行数
     */
    int deductPoints(@Param("userId") Long userId, @Param("amount") Long amount);

    /**
     * 增加用户积分
     * @param userId 用户ID
     * @param amount 增加数量
     * @return 影响行数
     */
    int addPoints(@Param("userId") Long userId, @Param("amount") Long amount);
}