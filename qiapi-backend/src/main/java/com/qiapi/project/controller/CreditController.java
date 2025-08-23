package com.qiapi.project.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qiapi.project.annotation.AuthCheck;
import com.qiapi.project.common.BaseResponse;
import com.qiapi.project.common.ErrorCode;
import com.qiapi.project.common.ResultUtils;
import com.qiapi.project.constant.UserConstant;
import com.qiapi.project.exception.BusinessException;
import com.qiapi.project.model.dto.credit.CreditApplyRequest;
import com.qiapi.project.model.dto.credit.PointExchangeRequest;
import com.qiapi.project.model.dto.credit.PackagePurchaseRequest;
import com.qiapi.project.model.vo.CreditBalanceVO;
import com.qiapi.project.model.vo.CreditPackageVO;
import com.qiapi.project.model.vo.PointBalanceVO;
import com.qiapi.project.model.vo.OrderVO;
import com.qiapi.project.model.vo.OrderVO;
import com.qiapi.project.service.OrderService;
import com.qiapi.project.service.PointService;
import com.qiapi.project.service.UserService;
import com.qiapi.qiapicommon.model.entity.User;
import com.qiapi.qiapicommon.model.entity.CreditPackage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 额度管理接口
 */
@RestController
@RequestMapping("/credit")
@Slf4j
public class CreditController {

    @Resource
    private CreditService creditService;

    @Resource
    private OrderService orderService;

    @Resource
    private PointService pointService;

    @Resource
    private UserService userService;

    @Resource
    private com.qiapi.project.service.CreditPackageService creditPackageService;

    /**
     * 申请接口免费额度
     */
    @PostMapping("/apply-free")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<Boolean> applyCreditLimit(@RequestBody CreditApplyRequest request, 
                                                 HttpServletRequest httpRequest) {
        // 参数校验
        if (request == null || request.getInterfaceId() == null || request.getInterfaceId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 获取当前用户
        User loginUser = userService.getLoginUser(httpRequest);

        // 申请免费额度
        boolean result = creditService.applyCreditLimit(loginUser.getId(), request.getInterfaceId());

        return ResultUtils.success(result);
    }

    /**
     * 使用积分兑换额度
     */
    @PostMapping("/exchange-points")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<Boolean> exchangePointsForCredit(@RequestBody PointExchangeRequest request, 
                                                        HttpServletRequest httpRequest) {
        // 参数校验
        if (request == null || request.getPointAmount() == null || request.getPointAmount() <= 0 || 
            request.getInterfaceId() == null || request.getInterfaceId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 获取当前用户
        User loginUser = userService.getLoginUser(httpRequest);

        // 积分兑换额度
        boolean result = creditService.exchangePointsForCredit(
            loginUser.getId(), 
            request.getInterfaceId(),
            request.getPointAmount()
        );

        return ResultUtils.success(result);
    }

    /**
     * 查询用户额度余额
     */
    @GetMapping("/balance")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<List<CreditBalanceVO>> getCreditBalance(HttpServletRequest httpRequest) {
        // 获取当前用户
        User loginUser = userService.getLoginUser(httpRequest);

        // 查询用户所有接口的额度余额
        List<CreditBalanceVO> balances = creditService.getCreditBalance(loginUser.getId());

        return ResultUtils.success(balances);
    }

    /**
     * 查询用户积分余额
     */
    @GetMapping("/points/balance")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<PointBalanceVO> getPointBalance(HttpServletRequest httpRequest) {
        // 获取当前用户
        User loginUser = userService.getLoginUser(httpRequest);

        // 查询用户积分余额
        PointBalanceVO balance = pointService.getPointBalance(loginUser.getId());

        return ResultUtils.success(balance);
    }

    /**
     * 获取所有额度套餐
     */
    @GetMapping("/packages")
    public BaseResponse<List<CreditPackageVO>> getCreditPackages() {
        // 查询上架的套餐
        QueryWrapper<CreditPackage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1)
                   .eq("is_delete", 0)
                   .orderByAsc("sort_order");
        
        List<CreditPackage> packages = creditPackageService.list(queryWrapper);
        
        List<CreditPackageVO> packageVOs = packages.stream().map(creditPackage -> {
            CreditPackageVO vo = new CreditPackageVO();
            BeanUtils.copyProperties(creditPackage, vo);
            vo.setIsRecommended(creditPackage.getIsRecommended() == 1);
            return vo;
        }).collect(Collectors.toList());

        return ResultUtils.success(packageVOs);
    }

    /**
     * 检查用户接口额度是否充足
     */
    @GetMapping("/check/{interfaceId}/{amount}")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<Boolean> checkCreditSufficient(@PathVariable("interfaceId") Long interfaceId, 
                                                      @PathVariable("amount") Long amount,
                                                      HttpServletRequest httpRequest) {
        // 参数校验
        if (interfaceId == null || interfaceId <= 0 || amount == null || amount <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 获取当前用户
        User loginUser = userService.getLoginUser(httpRequest);

        // 检查额度是否充足
        boolean sufficient = creditService.checkCreditSufficient(loginUser.getId(), interfaceId, amount);

        return ResultUtils.success(sufficient);
    }

    /**
     * 初始化用户积分账户（注册时调用）
     */
    @PostMapping("/points/init")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<Boolean> initUserPoints(HttpServletRequest httpRequest) {
        // 获取当前用户
        User loginUser = userService.getLoginUser(httpRequest);

        // 初始化积分账户
        boolean result = pointService.initUserPoints(loginUser.getId());

        return ResultUtils.success(result);
    }

    /**
     * 购买额度套餐
     */
    @PostMapping("/purchase-package")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<String> purchaseCreditPackage(@RequestBody PackagePurchaseRequest request, 
                                                     HttpServletRequest httpRequest) {
        // 参数校验
        if (request == null || request.getPackageId() == null || request.getPackageId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 获取当前用户
        User loginUser = userService.getLoginUser(httpRequest);

        // 创建订单
        String orderNo = orderService.createOrder(loginUser.getId(), request);

        return ResultUtils.success(orderNo);
    }

    /**
     * 查询用户订单列表
     */
    @GetMapping("/orders")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<List<OrderVO>> getUserOrders(HttpServletRequest httpRequest) {
        // 获取当前用户
        User loginUser = userService.getLoginUser(httpRequest);

        // 查询订单列表
        List<OrderVO> orders = orderService.getUserOrders(loginUser.getId());

        return ResultUtils.success(orders);
    }

    /**
     * 根据订单号查询订单详情
     */
    @GetMapping("/order/{orderNo}")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<OrderVO> getOrderDetail(@PathVariable("orderNo") String orderNo, 
                                               HttpServletRequest httpRequest) {
        // 参数校验
        if (orderNo == null || orderNo.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 获取当前用户（确保用户只能查看自己的订单）
        User loginUser = userService.getLoginUser(httpRequest);

        // 查询订单详情
        OrderVO order = orderService.getOrderByOrderNo(orderNo);
        
        // 检查订单是否属于当前用户
        if (!order.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权查看此订单");
        }

        return ResultUtils.success(order);
    }

    /**
     * 模拟支付成功（用于演示）
     */
    @PostMapping("/order/simulate-pay/{orderNo}")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<Boolean> simulatePayment(@PathVariable("orderNo") String orderNo, 
                                               HttpServletRequest httpRequest) {
        // 参数校验
        if (orderNo == null || orderNo.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 获取当前用户
        User loginUser = userService.getLoginUser(httpRequest);

        // 先检查订单是否属于当前用户
        OrderVO order = orderService.getOrderByOrderNo(orderNo);
        if (!order.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权操作此订单");
        }

        // 模拟支付成功
        boolean result = orderService.simulatePaymentSuccess(orderNo);

        return ResultUtils.success(result);
    }

    /**
     * 取消订单
     */
    @PostMapping("/order/cancel/{orderNo}")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<Boolean> cancelOrder(@PathVariable("orderNo") String orderNo, 
                                           HttpServletRequest httpRequest) {
        // 参数校验
        if (orderNo == null || orderNo.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 获取当前用户
        User loginUser = userService.getLoginUser(httpRequest);

        // 先检查订单是否属于当前用户
        OrderVO order = orderService.getOrderByOrderNo(orderNo);
        if (!order.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权操作此订单");
        }

        // 取消订单
        boolean result = orderService.cancelOrder(orderNo);

        return ResultUtils.success(result);
    }
}