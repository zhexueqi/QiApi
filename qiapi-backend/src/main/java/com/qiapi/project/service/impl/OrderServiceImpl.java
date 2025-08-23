package com.qiapi.project.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiapi.project.common.ErrorCode;
import com.qiapi.project.exception.BusinessException;
import com.qiapi.project.mapper.OrderMapper;
import com.qiapi.project.model.dto.credit.PackagePurchaseRequest;
import com.qiapi.project.model.vo.OrderVO;
import com.qiapi.project.service.CreditPackageService;
import com.qiapi.project.service.CreditService;
import com.qiapi.project.service.OrderService;
import com.qiapi.project.service.PointService;
import com.qiapi.qiapicommon.model.entity.CreditPackage;
import com.qiapi.qiapicommon.model.entity.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单服务实现
 */
@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Resource
    private CreditPackageService creditPackageService;

    @Resource
    private PointService pointService;

    @Resource
    private CreditService creditService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createOrder(Long userId, PackagePurchaseRequest request) {
        if (userId == null || request == null || request.getPackageId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 查询套餐信息
        CreditPackage creditPackage = creditPackageService.getById(request.getPackageId());
        if (creditPackage == null || creditPackage.getStatus() != 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "套餐不存在或已下架");
        }

        // 生成订单号
        String orderNo = "ORDER_" + IdUtil.getSnowflakeNextIdStr();

        // 计算支付金额和积分使用
        BigDecimal actualPrice = creditPackage.getPrice();
        Long pointsUsed = 0L;
        BigDecimal moneyPaid = actualPrice;

        String paymentType = request.getPaymentType();
        if (paymentType == null) {
            paymentType = "MONEY";
        }

        // 处理不同支付方式
        switch (paymentType) {
            case "POINTS":
                // 纯积分支付
                if (creditPackage.getPointsPrice() == null) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "该套餐不支持积分支付");
                }
                pointsUsed = creditPackage.getPointsPrice();
                moneyPaid = BigDecimal.ZERO;
                
                // 检查积分是否充足
                if (!pointService.checkPointsSufficient(userId, pointsUsed)) {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "积分不足");
                }
                break;
                
            case "MIXED":
                // 混合支付
                if (request.getPointsUsed() == null || request.getPointsUsed() <= 0) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "混合支付必须指定积分使用数量");
                }
                pointsUsed = request.getPointsUsed();
                
                // 检查积分是否充足
                if (!pointService.checkPointsSufficient(userId, pointsUsed)) {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "积分不足");
                }
                
                // 计算折扣后的金额（假设100积分抵扣1元）
                BigDecimal pointsDiscount = new BigDecimal(pointsUsed).divide(new BigDecimal(100));
                moneyPaid = actualPrice.subtract(pointsDiscount);
                if (moneyPaid.compareTo(BigDecimal.ZERO) < 0) {
                    moneyPaid = BigDecimal.ZERO;
                }
                break;
                
            case "MONEY":
            default:
                // 纯现金支付
                pointsUsed = 0L;
                moneyPaid = actualPrice;
                break;
        }

        // 创建订单
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setPackageId(request.getPackageId());
        order.setPackageName(creditPackage.getPackageName());
        order.setCreditAmount(creditPackage.getCreditAmount());
        order.setOriginalPrice(creditPackage.getPrice());
        order.setActualPrice(actualPrice);
        order.setPaymentType(paymentType);
        order.setPointsUsed(pointsUsed);
        order.setMoneyPaid(moneyPaid);
        order.setOrderStatus("PENDING");
        
        // 设置订单过期时间（30分钟后）
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 30);
        order.setExpireTime(calendar.getTime());
        
        order.setCreateTime(new Date());
        order.setUpdateTime(new Date());
        order.setIsDelete(0);

        boolean saveResult = this.save(order);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "订单创建失败");
        }

        log.info("订单创建成功，订单号：{}，用户ID：{}，套餐ID：{}", orderNo, userId, request.getPackageId());
        return orderNo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean processPayment(String orderNo, String paymentInfo) {
        if (orderNo == null || paymentInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 查询订单
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("orderNo", orderNo).eq("isDelete", 0);
        Order order = this.getOne(queryWrapper);

        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "订单不存在");
        }

        if (!"PENDING".equals(order.getOrderStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "订单状态异常");
        }

        // 检查订单是否过期
        if (order.getExpireTime().before(new Date())) {
            // 自动取消过期订单
            cancelOrder(orderNo);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "订单已过期");
        }

        // 处理积分扣减
        if (order.getPointsUsed() > 0) {
            boolean pointsDeducted = pointService.spendPoints(order.getUserId(), order.getPointsUsed(), "购买额度套餐");
            if (!pointsDeducted) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "积分扣减失败");
            }
        }

        // 更新订单状态为已支付
        UpdateWrapper<Order> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("orderNo", orderNo)
                    .set("orderStatus", "PAID")
                    .set("paymentTime", new Date())
                    .set("updateTime", new Date());

        boolean updateResult = this.update(updateWrapper);
        if (updateResult) {
            log.info("订单支付成功，订单号：{}", orderNo);
            
            // 自动确认订单
            confirmOrder(orderNo);
        }

        return updateResult;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmOrder(String orderNo) {
        if (orderNo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 查询订单
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("orderNo", orderNo).eq("isDelete", 0);
        Order order = this.getOne(queryWrapper);

        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "订单不存在");
        }

        if (!"PAID".equals(order.getOrderStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "订单状态异常，只有已支付订单才能确认");
        }

        try {
            // 为用户充值额度（这里需要知道是为哪个接口充值，暂时使用接口ID=1作为通用额度）
            // 可以考虑将通用额度设计为一个特殊的接口ID
            Long defaultInterfaceId = -1L;
            boolean rechargeResult = creditService.rechargeCredit(
                order.getUserId(), 
                defaultInterfaceId, 
                order.getCreditAmount(), 
                "购买套餐：" + order.getPackageName()
            );

            if (!rechargeResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "额度充值失败");
            }

            // 更新订单状态为已完成
            UpdateWrapper<Order> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("orderNo", orderNo)
                        .set("orderStatus", "COMPLETED")
                        .set("completionTime", new Date())
                        .set("updateTime", new Date());

            boolean updateResult = this.update(updateWrapper);
            if (updateResult) {
                log.info("订单确认成功，订单号：{}，用户获得额度：{}", orderNo, order.getCreditAmount());
            }

            return updateResult;

        } catch (Exception e) {
            log.error("订单确认失败，订单号：{}", orderNo, e);
            
            // 如果额度充值失败，需要退还积分
            if (order.getPointsUsed() > 0) {
                pointService.earnPoints(order.getUserId(), order.getPointsUsed(), "订单确认失败退还积分");
            }
            
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "订单确认失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(String orderNo) {
        if (orderNo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 查询订单
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("orderNo", orderNo).eq("isDelete", 0);
        Order order = this.getOne(queryWrapper);

        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "订单不存在");
        }

        if ("COMPLETED".equals(order.getOrderStatus()) || "CANCELLED".equals(order.getOrderStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "订单状态异常，无法取消");
        }

        // 如果已支付，需要退还积分
        if ("PAID".equals(order.getOrderStatus()) && order.getPointsUsed() > 0) {
            pointService.earnPoints(order.getUserId(), order.getPointsUsed(), "订单取消退还积分");
        }

        // 更新订单状态为已取消
        UpdateWrapper<Order> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("orderNo", orderNo)
                    .set("orderStatus", "CANCELLED")
                    .set("updateTime", new Date());

        boolean updateResult = this.update(updateWrapper);
        if (updateResult) {
            log.info("订单取消成功，订单号：{}", orderNo);
        }

        return updateResult;
    }

    @Override
    public List<OrderVO> getUserOrders(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId)
                   .eq("isDelete", 0)
                   .orderByDesc("createTime");

        List<Order> orders = this.list(queryWrapper);
        return orders.stream().map(this::convertToOrderVO).collect(Collectors.toList());
    }

    @Override
    public OrderVO getOrderByOrderNo(String orderNo) {
        if (orderNo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("orderNo", orderNo).eq("isDelete", 0);
        Order order = this.getOne(queryWrapper);

        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "订单不存在");
        }

        return convertToOrderVO(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean simulatePaymentSuccess(String orderNo) {
        // 模拟支付成功（用于演示和测试）
        return processPayment(orderNo, "SIMULATE_SUCCESS");
    }

    /**
     * 转换为OrderVO
     */
    private OrderVO convertToOrderVO(Order order) {
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order, orderVO);
        return orderVO;
    }
}