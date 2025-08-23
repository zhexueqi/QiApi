package com.qiapi.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiapi.project.common.ErrorCode;
import com.qiapi.project.exception.BusinessException;
import com.qiapi.project.mapper.UserCreditMapper;
import com.qiapi.project.mapper.CreditRecordMapper;
import com.qiapi.project.model.vo.CreditBalanceVO;
import com.qiapi.project.service.CreditService;
import com.qiapi.project.service.PointService;
import com.qiapi.qiapicommon.model.entity.UserCredit;
import com.qiapi.qiapicommon.model.entity.CreditRecord;
import com.qiapi.qiapicommon.model.entity.InterfaceInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 额度服务实现
 */
@Service
@Slf4j
public class CreditServiceImpl extends ServiceImpl<UserCreditMapper, UserCredit> implements CreditService {

    @Resource
    private UserCreditMapper userCreditMapper;

    @Resource
    private CreditRecordMapper creditRecordMapper;

    @Resource
    private PointService pointService;

    private static final Long FREE_CREDIT_AMOUNT = 100L;
    private static final Long POINT_TO_CREDIT_RATIO = 10L; // 10积分 = 1次额度

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean applyCreditLimit(Long userId, Long interfaceId) {
        if (userId == null || interfaceId == null || userId <= 0 || interfaceId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 查询是否已存在记录
        QueryWrapper<UserCredit> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .eq("interface_id", interfaceId)
                   .eq("is_delete", 0);
        UserCredit existingCredit = this.getOne(queryWrapper);

        if (existingCredit != null) {
            // 检查是否已申请过免费额度
            if (existingCredit.getFreeApplied() == 1) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "已申请过免费额度");
            }
            
            // 更新现有记录
            existingCredit.setTotalCredit(existingCredit.getTotalCredit() + FREE_CREDIT_AMOUNT);
            existingCredit.setRemainingCredit(existingCredit.getRemainingCredit() + FREE_CREDIT_AMOUNT);
            existingCredit.setFreeApplied(1);
            existingCredit.setUpdateTime(new Date());
            boolean updateResult = this.updateById(existingCredit);
            
            if (updateResult) {
                // 记录额度变动
                saveCreditRecord(userId, interfaceId, "RECHARGE", FREE_CREDIT_AMOUNT, 
                               existingCredit.getRemainingCredit() - FREE_CREDIT_AMOUNT, 
                               existingCredit.getRemainingCredit(), null, null, "申请免费额度");
            }
            
            return updateResult;
        } else {
            // 创建新记录
            UserCredit newCredit = new UserCredit();
            newCredit.setUserId(userId);
            newCredit.setInterfaceId(interfaceId);
            newCredit.setTotalCredit(FREE_CREDIT_AMOUNT);
            newCredit.setUsedCredit(0L);
            newCredit.setRemainingCredit(FREE_CREDIT_AMOUNT);
            newCredit.setFreeApplied(1);
            newCredit.setStatus(1);
            newCredit.setCreateTime(new Date());
            newCredit.setUpdateTime(new Date());
            newCredit.setIsDelete(0);
            
            boolean saveResult = this.save(newCredit);
            
            if (saveResult) {
                // 记录额度变动
                saveCreditRecord(userId, interfaceId, "RECHARGE", FREE_CREDIT_AMOUNT, 
                               0L, FREE_CREDIT_AMOUNT, null, null, "申请免费额度");
            }
            
            return saveResult;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean consumeCredit(Long userId, Long interfaceId, Long amount) {
        if (userId == null || interfaceId == null || amount == null || 
            userId <= 0 || interfaceId <= 0 || amount <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 检查额度是否充足
        if (!checkCreditSufficient(userId, interfaceId, amount)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "额度不足");
        }

        // 获取当前额度信息
        QueryWrapper<UserCredit> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .eq("interface_id", interfaceId)
                   .eq("is_delete", 0);
        UserCredit userCredit = this.getOne(queryWrapper);
        
        if (userCredit == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到额度记录");
        }

        Long balanceBefore = userCredit.getRemainingCredit();
        
        // 扣减额度
        int result = userCreditMapper.deductCredit(userId, interfaceId, amount);
        if (result > 0) {
            // 记录额度变动
            saveCreditRecord(userId, interfaceId, "CONSUME", -amount, 
                           balanceBefore, balanceBefore - amount, null, null, "消费额度");
            return true;
        }
        
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rechargeCredit(Long userId, Long interfaceId, Long amount, String description) {
        if (userId == null || interfaceId == null || amount == null || 
            userId <= 0 || interfaceId <= 0 || amount <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 查询是否已存在记录
        QueryWrapper<UserCredit> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .eq("interface_id", interfaceId)
                   .eq("is_delete", 0);
        UserCredit existingCredit = this.getOne(queryWrapper);

        if (existingCredit != null) {
            Long balanceBefore = existingCredit.getRemainingCredit();
            
            // 增加额度
            int result = userCreditMapper.addCredit(userId, interfaceId, amount);
            if (result > 0) {
                // 记录额度变动
                saveCreditRecord(userId, interfaceId, "RECHARGE", amount, 
                               balanceBefore, balanceBefore + amount, null, null, description);
                return true;
            }
        } else {
            // 创建新记录
            UserCredit newCredit = new UserCredit();
            newCredit.setUserId(userId);
            newCredit.setInterfaceId(interfaceId);
            newCredit.setTotalCredit(amount);
            newCredit.setUsedCredit(0L);
            newCredit.setRemainingCredit(amount);
            newCredit.setFreeApplied(0);
            newCredit.setStatus(1);
            newCredit.setCreateTime(new Date());
            newCredit.setUpdateTime(new Date());
            newCredit.setIsDelete(0);
            
            boolean saveResult = this.save(newCredit);
            
            if (saveResult) {
                // 记录额度变动
                saveCreditRecord(userId, interfaceId, "RECHARGE", amount, 
                               0L, amount, null, null, description);
            }
            
            return saveResult;
        }
        
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean exchangePointsForCredit(Long userId, Long interfaceId, Long pointAmount) {
        if (userId == null || interfaceId == null || pointAmount == null || 
            userId <= 0 || interfaceId <= 0 || pointAmount <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 计算可兑换的额度数量
        if (pointAmount % POINT_TO_CREDIT_RATIO != 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "积分数量必须是" + POINT_TO_CREDIT_RATIO + "的倍数");
        }
        
        Long creditAmount = pointAmount / POINT_TO_CREDIT_RATIO;

        // 扣减积分
        boolean pointDeductResult = pointService.spendPoints(userId, pointAmount, "兑换额度");
        if (!pointDeductResult) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "积分扣减失败");
        }

        try {
            // 增加额度
            return rechargeCredit(userId, interfaceId, creditAmount, "积分兑换额度");
        } catch (Exception e) {
            // 如果额度增加失败，回滚积分
            pointService.earnPoints(userId, pointAmount, "兑换额度失败回滚");
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "额度兑换失败");
        }
    }

    @Override
    public List<CreditBalanceVO> getCreditBalance(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        QueryWrapper<UserCredit> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .eq("is_delete", 0)
                   .orderByDesc("update_time");
        List<UserCredit> userCredits = this.list(queryWrapper);

        // TODO: 关联查询接口信息，获取接口名称
        return userCredits.stream().map(userCredit -> {
            CreditBalanceVO vo = new CreditBalanceVO();
            BeanUtils.copyProperties(userCredit, vo);
            vo.setFreeApplied(userCredit.getFreeApplied() == 1);
            // 这里需要查询接口信息获取接口名称
            vo.setInterfaceName("接口名称"); // 临时设置，需要后续完善
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public boolean checkCreditSufficient(Long userId, Long interfaceId, Long amount) {
        if (userId == null || interfaceId == null || amount == null || 
            userId <= 0 || interfaceId <= 0 || amount <= 0) {
            return false;
        }

        QueryWrapper<UserCredit> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .eq("interface_id", interfaceId)
                   .eq("is_delete", 0)
                   .eq("status", 1);
        UserCredit userCredit = this.getOne(queryWrapper);

        if (userCredit == null) {
            return false;
        }

        return userCredit.getRemainingCredit() >= amount;
    }

    /**
     * 保存额度变动记录
     */
    private void saveCreditRecord(Long userId, Long interfaceId, String operationType, 
                                 Long creditChange, Long balanceBefore, Long balanceAfter,
                                 Long relatedOrderId, Long relatedUserId, String description) {
        CreditRecord record = new CreditRecord();
        record.setUserId(userId);
        record.setInterfaceId(interfaceId);
        record.setOperationType(operationType);
        record.setCreditChange(creditChange);
        record.setBalanceBefore(balanceBefore);
        record.setBalanceAfter(balanceAfter);
        record.setRelatedOrderId(relatedOrderId);
        record.setRelatedUserId(relatedUserId);
        record.setDescription(description);
        record.setCreateTime(new Date());
        
        creditRecordMapper.insert(record);
    }
}