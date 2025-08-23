package com.qiapi.project.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qiapi.project.annotation.AuthCheck;
import com.qiapi.project.common.BaseResponse;
import com.qiapi.project.common.ErrorCode;
import com.qiapi.project.common.ResultUtils;
import com.qiapi.project.exception.BusinessException;
import com.qiapi.project.mapper.CreditRecordMapper;
import com.qiapi.project.mapper.UserCreditMapper;
import com.qiapi.project.mapper.UserInterfaceInfoMapper;
import com.qiapi.project.model.vo.*;
import com.qiapi.project.service.InterfaceInfoService;
import com.qiapi.qiapicommon.model.entity.InterfaceInfo;
import com.qiapi.qiapicommon.model.entity.UserInterfaceInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 分析控制器 - 支持传统调用次数统计和额度系统统计
 *
 */
@RestController
@RequestMapping("/analysis")
@Slf4j
@Api(tags = "数据分析接口")
public class AnalysisController {

    @Resource
    private UserInterfaceInfoMapper userInterfaceInfoMapper;

    @Resource
    private UserCreditMapper userCreditMapper;

    @Resource
    private CreditRecordMapper creditRecordMapper;

    @Resource
    private InterfaceInfoService interfaceInfoService;

    // ==================== 传统调用次数统计 ====================

    @GetMapping("/top/interface/invoke")
    @AuthCheck(mustRole = "admin")
    @ApiOperation("获取接口调用次数排行榜")
    public BaseResponse<List<InterfaceInfoVO>> listTopInvokeInterfaceInfo() {
        List<UserInterfaceInfo> userInterfaceInfoList = userInterfaceInfoMapper.listTopInvokeInterfaceInfo(3);
        Map<Long, List<UserInterfaceInfo>> interfaceInfoIdObjMap = userInterfaceInfoList.stream()
                .collect(Collectors.groupingBy(UserInterfaceInfo::getInterfaceInfoId));
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", interfaceInfoIdObjMap.keySet());
        List<InterfaceInfo> list = interfaceInfoService.list(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        List<InterfaceInfoVO> interfaceInfoVOList = list.stream().map(interfaceInfo -> {
            InterfaceInfoVO interfaceInfoVO = new InterfaceInfoVO();
            BeanUtils.copyProperties(interfaceInfo, interfaceInfoVO);
            int totalNum = interfaceInfoIdObjMap.get(interfaceInfo.getId()).get(0).getTotalNum();
            interfaceInfoVO.setTotalNum(totalNum);
            return interfaceInfoVO;
        }).collect(Collectors.toList());
        return ResultUtils.success(interfaceInfoVOList);
    }

    // ==================== 额度系统统计 ====================

    @GetMapping("/credit/top/interfaces")
    @AuthCheck(mustRole = "admin")
    @ApiOperation("获取接口额度消费排行榜")
    public BaseResponse<List<CreditAnalysisVO>> listTopCreditConsumedInterfaces(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<CreditAnalysisVO> result = userCreditMapper.listTopCreditConsumedInterfaces(limit);
            return ResultUtils.success(result);
        } catch (Exception e) {
            log.error("获取接口额度消费排行榜失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取统计数据失败");
        }
    }

    @GetMapping("/credit/top/users")
    @AuthCheck(mustRole = "admin")
    @ApiOperation("获取用户额度消费排行榜")
    public BaseResponse<List<UserCreditStatsVO>> listTopCreditConsumingUsers(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<UserCreditStatsVO> result = userCreditMapper.listTopCreditConsumingUsers(limit);
            return ResultUtils.success(result);
        } catch (Exception e) {
            log.error("获取用户额度消费排行榜失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取统计数据失败");
        }
    }

    @GetMapping("/credit/interfaces/all")
    @AuthCheck(mustRole = "admin")
    @ApiOperation("获取所有接口的额度统计")
    public BaseResponse<List<CreditAnalysisVO>> getAllInterfacesCreditStats() {
        try {
            List<CreditAnalysisVO> result = userCreditMapper.getAllInterfacesCreditStats();
            return ResultUtils.success(result);
        } catch (Exception e) {
            log.error("获取接口额度统计失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取统计数据失败");
        }
    }

    @GetMapping("/credit/users/low")
    @AuthCheck(mustRole = "admin")
    @ApiOperation("获取低额度用户列表")
    public BaseResponse<List<UserCreditStatsVO>> listLowCreditUsers(
            @RequestParam(defaultValue = "100") Long threshold,
            @RequestParam(defaultValue = "20") int limit) {
        try {
            List<UserCreditStatsVO> result = userCreditMapper.listLowCreditUsers(threshold, limit);
            return ResultUtils.success(result);
        } catch (Exception e) {
            log.error("获取低额度用户列表失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取统计数据失败");
        }
    }

    // ==================== 趋势分析 ====================

    @GetMapping("/credit/trend/recent")
    @AuthCheck(mustRole = "admin")
    @ApiOperation("获取最近N天的额度操作趋势")
    public BaseResponse<List<CreditTrendVO>> getRecentCreditTrend(@RequestParam(defaultValue = "7") int days) {
        try {
            if (days <= 0 || days > 90) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "天数范围应在1-90之间");
            }
            List<CreditTrendVO> result = creditRecordMapper.getRecentCreditTrend(days);
            return ResultUtils.success(result);
        } catch (Exception e) {
            log.error("获取额度操作趋势失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取统计数据失败");
        }
    }

    @GetMapping("/credit/trend/daterange")
    @AuthCheck(mustRole = "admin")
    @ApiOperation("获取指定日期范围的额度操作趋势")
    public BaseResponse<List<CreditTrendVO>> getCreditTrendByDateRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        try {
            if (startDate == null || endDate == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "开始日期和结束日期不能为空");
            }
            if (startDate.after(endDate)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "开始日期不能晚于结束日期");
            }
            // 限制查询范围，避免查询过大的数据集
            long diffInMillies = Math.abs(endDate.getTime() - startDate.getTime());
            long diffInDays = diffInMillies / (24 * 60 * 60 * 1000);
            if (diffInDays > 365) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "查询范围不能超过365天");
            }

            List<CreditTrendVO> result = creditRecordMapper.getCreditTrendByDateRange(startDate, endDate);
            return ResultUtils.success(result);
        } catch (Exception e) {
            log.error("获取指定日期范围额度操作趋势失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取统计数据失败");
        }
    }
}
