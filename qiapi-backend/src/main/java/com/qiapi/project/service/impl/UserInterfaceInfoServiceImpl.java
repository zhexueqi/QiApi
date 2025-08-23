package com.qiapi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiapi.project.common.ErrorCode;
import com.qiapi.project.exception.BusinessException;
import com.qiapi.project.mapper.UserInterfaceInfoMapper;
import com.qiapi.project.service.CreditService;
import com.qiapi.qiapicommon.model.entity.UserInterfaceInfo;
import com.qiapi.service.UserInterfaceInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 用户接口信息服务实现类
 *
 */
@Service
@Slf4j
public class UserInterfaceInfoServiceImpl extends ServiceImpl<UserInterfaceInfoMapper, UserInterfaceInfo>
        implements UserInterfaceInfoService {

    @Resource
    private CreditService creditService;

    @Override
    public void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add) {
        if (userInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 创建时，所有参数必须非空
        if (add) {
            if (userInterfaceInfo.getInterfaceInfoId() <= 0 || userInterfaceInfo.getUserId() <= 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口或用户不存在");
            }
        }
        if (userInterfaceInfo.getLeftNum() < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "剩余次数不能小于 0");
        }
    }

    /**
     * 减少调用次数（使用额度系统）
     */
    @Override
    public boolean invokeCount(long interfaceInfoId, long userId) {
        // 参数校验
        if (interfaceInfoId <= 0 || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        try {
            // 优先使用新的额度系统
            boolean result = creditService.consumeCredit(userId, interfaceInfoId, 1L);
            if (result) {
                log.info("用户{}调用接口{}，额度扣减成功", userId, interfaceInfoId);
                return true;
            } else {
                log.warn("用户{}调用接口{}，额度不足，尝试使用旧系统", userId, interfaceInfoId);
                // 如果额度系统失败，降级使用旧的调用次数系统
                return invokeCountLegacy(interfaceInfoId, userId);
            }
        } catch (Exception e) {
            log.error("额度系统调用失败，降级使用旧系统 - 用户:{}, 接口:{}", userId, interfaceInfoId, e);
            // 异常时降级使用旧的调用次数系统
            return invokeCountLegacy(interfaceInfoId, userId);
        }
    }

    /**
     * 旧的调用次数扣减逻辑（作为降级方案）
     */
    private boolean invokeCountLegacy(long interfaceInfoId, long userId) {
        log.info("使用旧的调用次数系统 - 用户:{}, 接口:{}", userId, interfaceInfoId);
        UpdateWrapper<UserInterfaceInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("interfaceInfoId", interfaceInfoId);
        updateWrapper.eq("userId", userId);
        updateWrapper.gt("leftNum", 0); // 确保剩余次数大于0
        updateWrapper.setSql("leftNum = leftNum - 1, totalNum = totalNum + 1");
        return this.update(updateWrapper);
    }

    @Override
    public UserInterfaceInfo getUserInterfaceInfo(long interfaceInfoId, long userId) {
        LambdaQueryWrapper<UserInterfaceInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInterfaceInfo::getInterfaceInfoId, interfaceInfoId);
        queryWrapper.eq(UserInterfaceInfo::getUserId, userId);
        return this.getOne(queryWrapper);
    }

}
