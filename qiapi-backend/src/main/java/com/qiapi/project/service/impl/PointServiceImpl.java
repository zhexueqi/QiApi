package com.qiapi.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiapi.project.common.ErrorCode;
import com.qiapi.project.exception.BusinessException;
import com.qiapi.project.mapper.UserPointsMapper;
import com.qiapi.project.mapper.PointRecordMapper;
import com.qiapi.project.model.vo.PointBalanceVO;
import com.qiapi.project.service.PointService;
import com.qiapi.qiapicommon.model.entity.UserPoints;
import com.qiapi.qiapicommon.model.entity.PointRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 积分服务实现
 */
@Service
@Slf4j
public class PointServiceImpl extends ServiceImpl<UserPointsMapper, UserPoints> implements PointService {

    @Resource
    private UserPointsMapper userPointsMapper;

    @Resource
    private PointRecordMapper pointRecordMapper;

    private static final Long INITIAL_POINTS = 100L; // 初始积分

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean earnPoints(Long userId, Long amount, String reason) {
        if (userId == null || amount == null || userId <= 0 || amount <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 确保用户积分账户存在
        ensureUserPointsExists(userId);

        // 获取当前积分信息
        UserPoints userPoints = getUserPointsByUserId(userId);
        Long balanceBefore = userPoints.getAvailablePoints();

        // 增加积分
        int result = userPointsMapper.addPoints(userId, amount);
        if (result > 0) {
            // 记录积分变动
            savePointRecord(userId, amount, balanceBefore + amount, "EARN", reason, null);
            return true;
        }

        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean spendPoints(Long userId, Long amount, String reason) {
        if (userId == null || amount == null || userId <= 0 || amount <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 检查积分是否充足
        if (!checkPointsSufficient(userId, amount)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "积分不足");
        }

        // 获取当前积分信息
        UserPoints userPoints = getUserPointsByUserId(userId);
        Long balanceBefore = userPoints.getAvailablePoints();

        // 扣减积分
        int result = userPointsMapper.deductPoints(userId, amount);
        if (result > 0) {
            // 记录积分变动
            savePointRecord(userId, -amount, balanceBefore - amount, "SPEND", reason, null);
            return true;
        }

        return false;
    }

    @Override
    public PointBalanceVO getPointBalance(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 确保用户积分账户存在
        ensureUserPointsExists(userId);

        UserPoints userPoints = getUserPointsByUserId(userId);
        PointBalanceVO vo = new PointBalanceVO();
        BeanUtils.copyProperties(userPoints, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean initUserPoints(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 检查是否已存在
        QueryWrapper<UserPoints> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId).eq("isDelete", 0);
        UserPoints existingPoints = this.getOne(queryWrapper);

        if (existingPoints != null) {
            log.info("用户{}积分账户已存在", userId);
            return true;
        }

        // 创建新的积分账户
        UserPoints userPoints = new UserPoints();
        userPoints.setUserId(userId);
        userPoints.setTotalPoints(INITIAL_POINTS);
        userPoints.setAvailablePoints(INITIAL_POINTS);
        userPoints.setFrozenPoints(0L);
        userPoints.setCreateTime(new Date());
        userPoints.setUpdateTime(new Date());
        userPoints.setIsDelete(0);

        boolean saveResult = this.save(userPoints);

        if (saveResult) {
            // 记录积分获得
            savePointRecord(userId, INITIAL_POINTS, INITIAL_POINTS, "EARN", "注册奖励", null);
        }

        return saveResult;
    }

    @Override
    public boolean checkPointsSufficient(Long userId, Long amount) {
        if (userId == null || amount == null || userId <= 0 || amount <= 0) {
            return false;
        }

        UserPoints userPoints = getUserPointsByUserId(userId);
        return userPoints != null && userPoints.getAvailablePoints() >= amount;
    }

    /**
     * 确保用户积分账户存在
     */
    private void ensureUserPointsExists(Long userId) {
        QueryWrapper<UserPoints> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId).eq("isDelete", 0);
        UserPoints userPoints = this.getOne(queryWrapper);

        if (userPoints == null) {
            initUserPoints(userId);
        }
    }

    /**
     * 根据用户ID获取积分信息
     */
    private UserPoints getUserPointsByUserId(Long userId) {
        QueryWrapper<UserPoints> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId).eq("isDelete", 0);
        UserPoints userPoints = this.getOne(queryWrapper);

        if (userPoints == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到用户积分账户");
        }

        return userPoints;
    }

    /**
     * 保存积分变动记录
     */
    private void savePointRecord(Long userId, Long pointChange, Long balanceAfter, 
                               String operationType, String reason, Long relatedOrderId) {
        PointRecord record = new PointRecord();
        record.setUserId(userId);
        record.setPointChange(pointChange);
        record.setBalanceAfter(balanceAfter);
        record.setOperationType(operationType);
        record.setReason(reason);
        record.setRelatedOrderId(relatedOrderId);
        record.setCreateTime(new Date());

        pointRecordMapper.insert(record);
    }
}