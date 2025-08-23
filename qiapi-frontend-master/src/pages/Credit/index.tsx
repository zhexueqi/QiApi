import { PageContainer } from '@ant-design/pro-components';
import React, { useEffect, useState } from 'react';
import {
  Card,
  Table,
  Button,
  message,
  Modal,
  Form,
  InputNumber,
  Select,
  Tag,
  Descriptions,
  Row,
  Col,
  Statistic,
  Space,
  Tooltip,
} from 'antd';
import {
  getCreditBalanceUsingGET,
  applyFreeCreditUsingPOST,
  exchangePointsUsingPOST,
  getPointsBalanceUsingGET,
} from '@/services/yuapi-backend/creditController';
import { listInterfaceInfoByPageUsingGET } from '@/services/yuapi-backend/interfaceInfoController';
import { InfoCircleOutlined, GiftOutlined, SwapOutlined, ReloadOutlined } from '@ant-design/icons';

const { Option } = Select;

/**
 * 额度管理页面
 */
const CreditManager: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [creditList, setCreditList] = useState<API.CreditBalanceVO[]>([]);
  const [pointBalance, setPointBalance] = useState<API.PointBalanceVO>();
  const [interfaceList, setInterfaceList] = useState<API.InterfaceInfo[]>([]);
  const [applyModalVisible, setApplyModalVisible] = useState(false);
  const [exchangeModalVisible, setExchangeModalVisible] = useState(false);
  const [selectedInterface, setSelectedInterface] = useState<number>();
  const [applyForm] = Form.useForm();
  const [exchangeForm] = Form.useForm();

  // 加载数据
  const loadData = async () => {
    setLoading(true);
    try {
      // 并行加载数据
      const [creditRes, pointRes, interfaceRes] = await Promise.all([
        getCreditBalanceUsingGET(),
        getPointsBalanceUsingGET(),
        listInterfaceInfoByPageUsingGET({ current: 1, pageSize: 20 }),
      ]);

      if (creditRes.code === 0) {
        setCreditList(creditRes.data || []);
      }
      if (pointRes.code === 0) {
        setPointBalance(pointRes.data);
      }
      if (interfaceRes.code === 0) {
        setInterfaceList(interfaceRes.data?.records || []);
      }
    } catch (error: any) {
      message.error('加载数据失败：' + error.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  // 申请免费额度
  const handleApplyFree = async (values: any) => {
    try {
      const res = await applyFreeCreditUsingPOST({
        interfaceId: values.interfaceId,
      });
      if (res.code === 0) {
        message.success('申请成功！已为您添加100次免费额度');
        setApplyModalVisible(false);
        applyForm.resetFields();
        loadData();
      } else {
        message.error(res.message || '申请失败');
      }
    } catch (error: any) {
      message.error('申请失败：' + error.message);
    }
  };

  // 积分兑换额度
  const handleExchangePoints = async (values: any) => {
    try {
      const res = await exchangePointsUsingPOST({
        interfaceId: values.interfaceId,
        pointAmount: values.pointAmount,
      });
      if (res.code === 0) {
        message.success(`兑换成功！消耗${values.pointAmount}积分，获得${values.pointAmount / 10}次调用额度`);
        setExchangeModalVisible(false);
        exchangeForm.resetFields();
        loadData();
      } else {
        message.error(res.message || '兑换失败');
      }
    } catch (error: any) {
      message.error('兑换失败：' + error.message);
    }
  };

  // 表格列定义
  const columns = [
    {
      title: '接口名称',
      dataIndex: 'interfaceName',
      key: 'interfaceName',
      render: (text: string) => <strong>{text}</strong>,
    },
    {
      title: '总额度',
      dataIndex: 'totalCredit',
      key: 'totalCredit',
      render: (text: number) => (
        <Statistic
          value={text}
          valueStyle={{ fontSize: '14px' }}
          suffix="次"
        />
      ),
    },
    {
      title: '已使用',
      dataIndex: 'usedCredit',
      key: 'usedCredit',
      render: (text: number) => (
        <Statistic
          value={text}
          valueStyle={{ fontSize: '14px', color: '#cf1322' }}
          suffix="次"
        />
      ),
    },
    {
      title: '剩余额度',
      dataIndex: 'remainingCredit',
      key: 'remainingCredit',
      render: (text: number) => (
        <Statistic
          value={text}
          valueStyle={{
            fontSize: '14px',
            color: text > 50 ? '#3f8600' : text > 10 ? '#fa8c16' : '#cf1322'
          }}
          suffix="次"
        />
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: number) => (
        <Tag color={status === 1 ? 'green' : 'red'}>
          {status === 1 ? '正常' : '禁用'}
        </Tag>
      ),
    },
    {
      title: '免费额度',
      dataIndex: 'freeApplied',
      key: 'freeApplied',
      render: (applied: boolean, record: API.CreditBalanceVO) => (
        applied && record.interfaceId !== -1 ? (
          <Tag color="blue">已申请</Tag>
        ) : (
          <Button
            type="text"
            size="small"
          /**onClick={() => {
            setSelectedInterface(record.interfaceId);
            applyForm.setFieldsValue({ interfaceId: record.interfaceId });
            setApplyModalVisible(true);
          }}*/
          >
            通用额度
          </Button>
        )
      ),
    },
    {
      title: '有效期',
      dataIndex: 'expireTime',
      key: 'expireTime',
      render: (time: string) => time || '永久有效',
    },
  ];

  return (
    <PageContainer
      title="额度管理"
      subTitle="管理您的API调用额度"
      extra={[
        <Button key="refresh" icon={<ReloadOutlined />} onClick={loadData}>
          刷新
        </Button>,
      ]}
    >
      {/* 积分余额卡片 */}
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={12} lg={8}>
          <Card>
            <Statistic
              title="总积分"
              value={pointBalance?.totalPoints || 0}
              precision={0}
              valueStyle={{ color: '#3f8600' }}
              prefix={<GiftOutlined />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={8}>
          <Card>
            <Statistic
              title="可用积分"
              value={pointBalance?.availablePoints || 0}
              precision={0}
              valueStyle={{ color: '#1890ff' }}
              prefix={<SwapOutlined />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={8}>
          <Card>
            <Statistic
              title="冻结积分"
              value={pointBalance?.frozenPoints || 0}
              precision={0}
              valueStyle={{ color: '#faad14' }}
              prefix={<InfoCircleOutlined />}
            />
          </Card>
        </Col>
      </Row>

      {/* 操作按钮 */}
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col>
          <Button
            type="primary"
            icon={<GiftOutlined />}
            onClick={() => setApplyModalVisible(true)}
          >
            申请免费额度
          </Button>
        </Col>
        <Col>
          <Button
            icon={<SwapOutlined />}
            onClick={() => setExchangeModalVisible(true)}
          >
            积分兑换额度
          </Button>
        </Col>
      </Row>

      {/* 额度余额表格 */}
      <Card title="额度余额" loading={loading}>
        <Table
          dataSource={creditList}
          columns={columns}
          rowKey="interfaceId"
          pagination={{
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条记录`,
          }}
        />
      </Card>

      {/* 申请免费额度弹窗 */}
      <Modal
        title="申请免费额度"
        open={applyModalVisible}
        onCancel={() => {
          setApplyModalVisible(false);
          applyForm.resetFields();
        }}
        footer={null}
      >
        <Form
          form={applyForm}
          onFinish={handleApplyFree}
          layout="vertical"
        >
          <Form.Item
            name="interfaceId"
            label="选择接口"
            rules={[{ required: true, message: '请选择要申请额度的接口' }]}
          >
            <Select placeholder="请选择接口">
              {interfaceList
                .filter(item => !creditList.find(credit => credit.interfaceId === item.id)?.freeApplied)
                .map(item => (
                  <Option key={item.id} value={item.id}>
                    {item.name}
                  </Option>
                ))
              }
            </Select>
          </Form.Item>

          <div style={{ background: '#f0f9ff', padding: 16, borderRadius: 6, marginBottom: 16 }}>
            <Descriptions column={1} size="small">
              <Descriptions.Item label="免费额度">
                <strong>100次</strong>
              </Descriptions.Item>
              <Descriptions.Item label="申请条件">
                每个接口仅可申请一次
              </Descriptions.Item>
              <Descriptions.Item label="有效期">
                永久有效
              </Descriptions.Item>
            </Descriptions>
          </div>

          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">
                申请免费额度
              </Button>
              <Button
                onClick={() => {
                  setApplyModalVisible(false);
                  applyForm.resetFields();
                }}
              >
                取消
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      {/* 积分兑换额度弹窗 */}
      <Modal
        title="积分兑换额度"
        open={exchangeModalVisible}
        onCancel={() => {
          setExchangeModalVisible(false);
          exchangeForm.resetFields();
        }}
        footer={null}
      >
        <Form
          form={exchangeForm}
          onFinish={handleExchangePoints}
          layout="vertical"
        >
          <Form.Item
            name="interfaceId"
            label="选择接口"
            rules={[{ required: true, message: '请选择要兑换额度的接口' }]}
          >
            <Select placeholder="请选择接口">
              {interfaceList.map(item => (
                <Option key={item.id} value={item.id}>
                  {item.name}
                </Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item
            name="pointAmount"
            label={
              <Space>
                积分数量
                <Tooltip title="10积分 = 1次调用">
                  <InfoCircleOutlined />
                </Tooltip>
              </Space>
            }
            rules={[
              { required: true, message: '请输入要兑换的积分数量' },
              {
                validator: (_, value) => {
                  if (value && value % 10 !== 0) {
                    return Promise.reject(new Error('积分数量必须是10的倍数'));
                  }
                  if (value && value > (pointBalance?.availablePoints || 0)) {
                    return Promise.reject(new Error('积分不足'));
                  }
                  return Promise.resolve();
                },
              },
            ]}
          >
            <InputNumber
              min={10}
              step={10}
              max={pointBalance?.availablePoints || 0}
              style={{ width: '100%' }}
              placeholder="请输入积分数量（10的倍数）"
              formatter={(value) => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
              parser={(value) => Number(value!.replace(/\$\s?|(,*)/g, ''))}
            />
          </Form.Item>

          <div style={{ background: '#f6ffed', padding: 16, borderRadius: 6, marginBottom: 16 }}>
            <Descriptions column={1} size="small">
              <Descriptions.Item label="兑换比例">
                10积分 = 1次调用
              </Descriptions.Item>
              <Descriptions.Item label="可用积分">
                {pointBalance?.availablePoints || 0}积分
              </Descriptions.Item>
              <Descriptions.Item label="最多可兑换">
                {Math.floor((pointBalance?.availablePoints || 0) / 10)}次调用
              </Descriptions.Item>
            </Descriptions>
          </div>

          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">
                确认兑换
              </Button>
              <Button
                onClick={() => {
                  setExchangeModalVisible(false);
                  exchangeForm.resetFields();
                }}
              >
                取消
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </PageContainer>
  );
};

export default CreditManager;