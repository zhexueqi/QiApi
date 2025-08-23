// @ts-ignore
/* eslint-disable */
import { request } from '@umijs/max';

/** 申请接口免费额度 POST /api/credit/apply-free */
export async function applyFreeCreditUsingPOST(
  body: API.CreditApplyRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseboolean>('/api/credit/apply-free', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** 积分兑换额度 POST /api/credit/exchange-points */
export async function exchangePointsUsingPOST(
  body: API.PointExchangeRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseboolean>('/api/credit/exchange-points', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** 查询用户额度余额 GET /api/credit/balance */
export async function getCreditBalanceUsingGET(options?: { [key: string]: any }) {
  return request<API.BaseResponseListCreditBalanceVO>('/api/credit/balance', {
    method: 'GET',
    ...(options || {}),
  });
}

/** 查询用户积分余额 GET /api/credit/points/balance */
export async function getPointsBalanceUsingGET(options?: { [key: string]: any }) {
  return request<API.BaseResponsePointBalanceVO>('/api/credit/points/balance', {
    method: 'GET',
    ...(options || {}),
  });
}

/** 获取额度套餐列表 GET /api/credit/packages */
export async function getCreditPackagesUsingGET(options?: { [key: string]: any }) {
  return request<API.BaseResponseListCreditPackageVO>('/api/credit/packages', {
    method: 'GET',
    ...(options || {}),
  });
}

/** 检查额度是否充足 GET /api/credit/check/{interfaceId}/{amount} */
export async function checkCreditSufficientUsingGET(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.checkCreditSufficientUsingGETParams,
  options?: { [key: string]: any },
) {
  const { interfaceId, amount } = params;
  return request<API.BaseResponseboolean>(`/api/credit/check/${interfaceId}/${amount}`, {
    method: 'GET',
    ...(options || {}),
  });
}

/** 初始化用户积分账户 POST /api/credit/points/init */
export async function initUserPointsUsingPOST(options?: { [key: string]: any }) {
  return request<API.BaseResponseboolean>('/api/credit/points/init', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    ...(options || {}),
  });
}

/** 购买额度套餐 POST /api/credit/purchase-package */
export async function purchasePackageUsingPOST(
  body: API.PackagePurchaseRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsestring>('/api/credit/purchase-package', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** 查询用户订单列表 GET /api/credit/orders */
export async function getUserOrdersUsingGET(options?: { [key: string]: any }) {
  return request<API.BaseResponseListOrderVO>('/api/credit/orders', {
    method: 'GET',
    ...(options || {}),
  });
}

/** 查询订单详情 GET /api/credit/order/{orderNo} */
export async function getOrderDetailUsingGET(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getOrderDetailUsingGETParams,
  options?: { [key: string]: any },
) {
  const { orderNo } = params;
  return request<API.BaseResponseOrderVO>(`/api/credit/order/${orderNo}`, {
    method: 'GET',
    ...(options || {}),
  });
}

/** 模拟支付成功 POST /api/credit/order/simulate-pay/{orderNo} */
export async function simulatePaymentUsingPOST(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.simulatePaymentUsingPOSTParams,
  options?: { [key: string]: any },
) {
  const { orderNo } = params;
  return request<API.BaseResponseboolean>(`/api/credit/order/simulate-pay/${orderNo}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    ...(options || {}),
  });
}

/** 取消订单 POST /api/credit/order/cancel/{orderNo} */
export async function cancelOrderUsingPOST(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.cancelOrderUsingPOSTParams,
  options?: { [key: string]: any },
) {
  const { orderNo } = params;
  return request<API.BaseResponseboolean>(`/api/credit/order/cancel/${orderNo}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    ...(options || {}),
  });
}