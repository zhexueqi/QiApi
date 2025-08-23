import type { RequestOptions } from '@@/plugin-request/request';
import type { RequestConfig } from '@umijs/max';

// 与后端约定的响应数据格式
interface ResponseStructure {
  success: boolean;
  data: any;
  errorCode?: number;
  errorMessage?: string;
}

/**
 * @name 错误处理
 * pro 自带的错误处理， 可以在这里做自己的改动
 * @doc https://umijs.org/docs/max/request#配置
 */
export const requestConfig: RequestConfig = {
  // 在开发环境下使用代理，不需要设置baseURL
  // baseURL: 'http://localhost:8090',
  withCredentials: true,
  // 请求拦截器
  requestInterceptors: [
    (config: RequestOptions) => {
      // 拦截请求配置，进行个性化处理。
      // 暂时不添加token，由后端处理认证
      return { ...config };
    },
  ],
  // 响应拦截器
  responseInterceptors: [
    (response) => {
      // 拦截响应数据，进行个性化处理
      const { data } = response as unknown as ResponseStructure;
      console.log('response data:', data);

      // 检查响应是否成功
      if (data && typeof data === 'object' && data.code !== undefined && data.code !== 0) {
        const errorMsg = data.message || data.errorMessage || '请求失败';
        throw new Error(errorMsg);
      }

      return response;
    },
  ],
};
