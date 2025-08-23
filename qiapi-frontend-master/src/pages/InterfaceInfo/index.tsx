import { PageContainer } from '@ant-design/pro-components';
import React, { useEffect, useState } from 'react';
import { Button, Card, Descriptions, Form, message, Input, Spin, Divider } from 'antd';
import {
  getInterfaceInfoByIdUsingGET,
  invokeInterfaceInfoUsingPOST,
} from '@/services/yuapi-backend/interfaceInfoController';
import { useParams } from '@@/exports';
import { useModel } from '@umijs/max';

/**
 * 主页
 * @constructor
 */
const Index: React.FC = () => {
  const { initialState } = useModel('@@initialState');
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState<API.InterfaceInfo>();
  const [invokeRes, setInvokeRes] = useState<any>();
  const [invokeLoading, setInvokeLoading] = useState(false);

  const params = useParams();

  const loadData = async () => {
    if (!params.id) {
      message.error('参数不存在');
      return;
    }
    setLoading(true);
    try {
      const res = await getInterfaceInfoByIdUsingGET({
        id: Number(params.id),
      });
      setData(res.data);
    } catch (error: any) {
      message.error('请求失败，' + error.message);
    }
    setLoading(false);
  };

  useEffect(() => {
    loadData();
  }, []);

  const onFinish = async (values: any) => {
    if (!params.id) {
      message.error('接口不存在');
      return;
    }

    // 验证JSON格式
    if (values.userRequestParams) {
      try {
        JSON.parse(values.userRequestParams);
      } catch (error) {
        message.error('请求参数格式错误，请输入正确的JSON格式');
        return;
      }
    }

    // 检查用户是否登录
    if (!initialState?.loginUser?.id) {
      message.error('请先登录');
      return;
    }

    setInvokeLoading(true);
    try {
      const res = await invokeInterfaceInfoUsingPOST({
        id: Number(params.id),
        userRequestParams: values.userRequestParams,
      }, {
        headers: {
          'userId': String(initialState.loginUser.id),
        },
      });
      setInvokeRes(res.data);
      message.success('请求成功');
    } catch (error: any) {
      message.error('操作失败：' + (error.message || '未知错误'));
      setInvokeRes('请求失败：' + (error.message || '未知错误'));
    }
    setInvokeLoading(false);
  };

  return (
    <PageContainer title="查看接口文档">
      <Card>
        {data ? (
          <Descriptions title={data.name} column={1}>
            <Descriptions.Item label="接口状态">{data.status ? '开启' : '关闭'}</Descriptions.Item>
            <Descriptions.Item label="描述">{data.description}</Descriptions.Item>
            <Descriptions.Item label="请求地址">{data.url}</Descriptions.Item>
            <Descriptions.Item label="请求方法">{data.method}</Descriptions.Item>
            <Descriptions.Item label="请求参数">{data.requestParams}</Descriptions.Item>
            <Descriptions.Item label="请求头">{data.requestHeader}</Descriptions.Item>
            <Descriptions.Item label="响应头">{data.responseHeader}</Descriptions.Item>
            <Descriptions.Item label="创建时间">{data.createTime}</Descriptions.Item>
            <Descriptions.Item label="更新时间">{data.updateTime}</Descriptions.Item>
          </Descriptions>
        ) : (
          <>接口不存在</>
        )}
      </Card>
      <Divider />
      <Card title="在线测试">
        <Form name="invoke" layout="vertical" onFinish={onFinish}>
          <Form.Item
            label="请求参数"
            name="userRequestParams"
            rules={[
              {
                required: false,
                message: '请输入请求参数',
              },
            ]}
          >
            <Input.TextArea
              placeholder='{
  "name": "zhexueqi"
}'
              rows={6}
            />
          </Form.Item>
          <Form.Item wrapperCol={{ span: 16 }}>
            <Button type="primary" htmlType="submit" loading={invokeLoading}>
              调用
            </Button>
          </Form.Item>
        </Form>
      </Card>
      <Divider />
      <Card title="返回结果" loading={invokeLoading}>
        {invokeRes ? (
          <div style={{ background: '#f5f5f5', padding: '16px', borderRadius: '4px' }}>
            <pre style={{ margin: 0, whiteSpace: 'pre-wrap', wordBreak: 'break-word' }}>
              {typeof invokeRes === 'object' ? JSON.stringify(invokeRes, null, 2) : String(invokeRes)}
            </pre>
          </div>
        ) : (
          <div style={{ color: '#999', textAlign: 'center', padding: '20px' }}>
            暂无返回结果，请先调用接口
          </div>
        )}
      </Card>
    </PageContainer>
  );
};

export default Index;
