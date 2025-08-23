package com.qiapi.project.service;

import com.qiapi.project.model.dto.credit.PackagePurchaseRequest;
import com.qiapi.project.model.vo.OrderVO;
import com.qiapi.qiapicommon.model.entity.Order;

import java.util.List;

/**
 * 订单服务
 */
public interface OrderService {

    /**
     * 创建订单
     * @param userId 用户ID
     * @param request 购买请求
     * @return 订单号
     */
    String createOrder(Long userId, PackagePurchaseRequest request);

    /**
     * 处理订单支付
     * @param orderNo 订单号
     * @param paymentInfo 支付信息
     * @return 是否成功
     */
    boolean processPayment(String orderNo, String paymentInfo);

    /**
     * 确认订单完成
     * @param orderNo 订单号
     * @return 是否成功
     */
    boolean confirmOrder(String orderNo);

    /**
     * 取消订单
     * @param orderNo 订单号
     * @return 是否成功
     */
    boolean cancelOrder(String orderNo);

    /**
     * 查询用户订单列表
     * @param userId 用户ID
     * @return 订单列表
     */
    List<OrderVO> getUserOrders(Long userId);

    /**
     * 根据订单号查询订单
     * @param orderNo 订单号
     * @return 订单信息
     */
    OrderVO getOrderByOrderNo(String orderNo);

    /**
     * 模拟支付完成（用于演示）
     * @param orderNo 订单号
     * @return 是否成功
     */
    boolean simulatePaymentSuccess(String orderNo);
}