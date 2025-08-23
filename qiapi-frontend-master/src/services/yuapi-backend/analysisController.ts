// @ts-ignore
/* eslint-disable */
import { request } from '@umijs/max';

// ==================== 传统调用次数统计 ====================

/** listTopInvokeInterfaceInfo GET /api/analysis/top/interface/invoke */
export async function listTopInvokeInterfaceInfoUsingGET(options?: { [key: string]: any }) {
  return request<API.BaseResponseListInterfaceInfoVO>('/api/analysis/top/interface/invoke', {
    method: 'GET',
    ...(options || {}),
  });
}

// ==================== 额度系统统计 ====================

/** 获取接口额度消费排行榜 GET /api/analysis/credit/top/interfaces */
export async function listTopCreditConsumedInterfacesUsingGET(params?: {
  /** 返回数量限制 */
  limit?: number;
}, options?: { [key: string]: any }) {
  return request<API.BaseResponseListCreditAnalysisVO>('/api/analysis/credit/top/interfaces', {
    method: 'GET',
    params: {
      limit: 10,
      ...params,
    },
    ...(options || {}),
  });
}

/** 获取用户额度消费排行榜 GET /api/analysis/credit/top/users */
export async function listTopCreditConsumingUsersUsingGET(params?: {
  /** 返回数量限制 */
  limit?: number;
}, options?: { [key: string]: any }) {
  return request<API.BaseResponseListUserCreditStatsVO>('/api/analysis/credit/top/users', {
    method: 'GET',
    params: {
      limit: 10,
      ...params,
    },
    ...(options || {}),
  });
}

/** 获取所有接口的额度统计 GET /api/analysis/credit/interfaces/all */
export async function getAllInterfacesCreditStatsUsingGET(options?: { [key: string]: any }) {
  return request<API.BaseResponseListCreditAnalysisVO>('/api/analysis/credit/interfaces/all', {
    method: 'GET',
    ...(options || {}),
  });
}

/** 获取低额度用户列表 GET /api/analysis/credit/users/low */
export async function listLowCreditUsersUsingGET(params?: {
  /** 剩余额度阈值 */
  threshold?: number;
  /** 返回数量限制 */
  limit?: number;
}, options?: { [key: string]: any }) {
  return request<API.BaseResponseListUserCreditStatsVO>('/api/analysis/credit/users/low', {
    method: 'GET',
    params: {
      threshold: 100,
      limit: 20,
      ...params,
    },
    ...(options || {}),
  });
}

// ==================== 趋势分析 ====================

/** 获取最近N天的额度操作趋势 GET /api/analysis/credit/trend/recent */
export async function getRecentCreditTrendUsingGET(params?: {
  /** 天数，范围1-90 */
  days?: number;
}, options?: { [key: string]: any }) {
  return request<API.BaseResponseListCreditTrendVO>('/api/analysis/credit/trend/recent', {
    method: 'GET',
    params: {
      days: 7,
      ...params,
    },
    ...(options || {}),
  });
}

/** 获取指定日期范围的额度操作趋势 GET /api/analysis/credit/trend/daterange */
export async function getCreditTrendByDateRangeUsingGET(params: {
  /** 开始日期，格式: yyyy-MM-dd */
  startDate: string;
  /** 结束日期，格式: yyyy-MM-dd */
  endDate: string;
}, options?: { [key: string]: any }) {
  return request<API.BaseResponseListCreditTrendVO>('/api/analysis/credit/trend/daterange', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}
