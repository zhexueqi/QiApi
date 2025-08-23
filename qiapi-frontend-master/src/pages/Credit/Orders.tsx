import { PageContainer } from '@ant-design/pro-components';
import React, { useEffect, useState } from 'react';
import {
  Card,
  Table,
  Button,
  message,
  Modal,
  Tag,
  Descriptions,
  Space,
  Tooltip,
  Badge,
  Popconfirm,
  Empty,
  Typography,
} from 'antd';
import {
  getUserOrdersUsingGET,
  getOrderDetailUsingGET,
  simulatePaymentUsingPOST,
  cancelOrderUsingPOST,
} from '@/services/yuapi-backend/creditController';
import {
  EyeOutlined,
  PayCircleOutlined,
  CloseOutlined,
  ReloadOutlined,
  ShoppingOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  ExclamationCircleOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';

const { Text, Title } = Typography;

const OrderManager: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [orders, setOrders] = useState<API.OrderVO[]>([]);
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [selectedOrder, setSelectedOrder] = useState<API.OrderVO>();
  const [actionLoading, setActionLoading] = useState<{ [key: string]: boolean }>({});

  const loadOrders = async () => {
    setLoading(true);
    try {
      const res = await getUserOrdersUsingGET();
      if (res.code === 0) {
        setOrders(res.data || []);
      } else {
        message.error(res.message || '加载订单列表失败');
      }
    } catch (error: any) {
      message.error('加载订单列表失败：' + error.message);
    } finally {
      setLoading(false);
    }
  };

  const loadOrderDetail = async (orderNo: string) => {
    try {
      const res = await getOrderDetailUsingGET({ orderNo });
      if (res.code === 0) {
        setSelectedOrder(res.data);
        setDetailModalVisible(true);
      } else {
        message.error(res.message || '加载订单详情失败');
      }
    } catch (error: any) {
      message.error('加载订单详情失败：' + error.message);
    }
  };

  const handlePayment = async (orderNo: string) => {
    setActionLoading({ ...actionLoading, [orderNo]: true });
    try {
      const res = await simulatePaymentUsingPOST({ orderNo });
      if (res.code === 0) {
        message.success('支付成功！');
        loadOrders();
      } else {
        message.error(res.message || '支付失败');
      }
    } catch (error: any) {
      message.error('支付失败：' + error.message);
    } finally {
      setActionLoading({ ...actionLoading, [orderNo]: false });
    }
  };

  const handleCancel = async (orderNo: string) => {
    setActionLoading({ ...actionLoading, [orderNo]: true });
    try {
      const res = await cancelOrderUsingPOST({ orderNo });
      if (res.code === 0) {
        message.success('订单已取消');
        loadOrders();
      } else {
        message.error(res.message || '取消订单失败');
      }
    } catch (error: any) {
      message.error('取消订单失败：' + error.message);
    } finally {
      setActionLoading({ ...actionLoading, [orderNo]: false });
    }
  };

  useEffect(() => {
    loadOrders();
  }, []);

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'PENDING':
        return 'orange';
      case 'PAID':
        return 'blue';
      case 'COMPLETED':
        return 'green';
      case 'CANCELLED':
        return 'red';
      case 'EXPIRED':
        return 'gray';
      default:
        return 'default';
    }
  };

  const getStatusText = (status: string) => {
    switch (status) {
      case 'PENDING':
        return '待支付';
      case 'PAID':
        return '已支付';
      case 'COMPLETED':
        return '已完成';
      case 'CANCELLED':
        return '已取消';
      case 'EXPIRED':
        return '已过期';
      default:
        return status;
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'PENDING':
        return <ClockCircleOutlined />;
      case 'PAID':
        return <PayCircleOutlined />;
      case 'COMPLETED':
        return <CheckCircleOutlined />;
      case 'CANCELLED':
        return <CloseOutlined />;
      case 'EXPIRED':
        return <ExclamationCircleOutlined />;
      default:
        return <ClockCircleOutlined />;
    }
  };

  const getPaymentTypeText = (type: string) => {
    switch (type) {
      case 'MONEY':
        return '现金支付';
      case 'POINTS':
        return '积分支付';
      case 'MIXED':
        return '混合支付';
      default:
        return type;
    }
  };

  const columns: ColumnsType<API.OrderVO> = [
    {
      title: '订单号',
      dataIndex: 'orderNo',
      key: 'orderNo',
      render: (text: string) => (
        <Text copyable style={{ fontSize: '12px' }}>
          {text}
        </Text>
      ),
    },
    {
      title: '套餐信息',
      key: 'package',
      render: (_, record) => (
        <div>
          <div style={{ fontWeight: 'bold' }}>{record.packageName}</div>
          <div style={{ fontSize: '12px', color: '#666' }}>
            {record.creditAmount}次调用
          </div>
        </div>
      ),
    },
    {
      title: '支付方式',
      dataIndex: 'paymentType',
      key: 'paymentType',
      render: (type: string, record) => (
        <div>
          <Tag>{getPaymentTypeText(type)}</Tag>
          <div style={{ fontSize: '12px', marginTop: 4 }}>
            {(record.pointsUsed || 0) > 0 && (
              <span style={{ color: '#fa8c16' }}>积分: {record.pointsUsed}</span>
            )}
            {(record.moneyPaid || 0) > 0 && (
              <span style={{ color: '#52c41a', marginLeft: (record.pointsUsed || 0) > 0 ? 8 : 0 }}>
                现金: ¥{record.moneyPaid}
              </span>
            )}
          </div>
        </div>
      ),
    },
    {
      title: '订单金额',
      key: 'price',
      render: (_, record) => (
        <div>
          <div style={{ fontWeight: 'bold', color: '#cf1322' }}>
            ¥{record.actualPrice}
          </div>
          {record.originalPrice !== record.actualPrice && (
            <div style={{ fontSize: '12px', textDecoration: 'line-through', color: '#999' }}>
              原价: ¥{record.originalPrice}
            </div>
          )}
        </div>
      ),
    },
    {
      title: '订单状态',
      dataIndex: 'orderStatus',
      key: 'orderStatus',
      render: (status: string) => (
        <Badge
          status={getStatusColor(status) as any}
          text={
            <Space>
              {getStatusIcon(status)}
              {getStatusText(status)}
            </Space>
          }
        />
      ),
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      render: (time: string) => (
        <div style={{ fontSize: '12px' }}>
          {time ? new Date(time).toLocaleString() : '-'}
        </div>
      ),
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Space size="small">
          <Tooltip title="查看详情">
            <Button
              type="text"
              size="small"
              icon={<EyeOutlined />}
              onClick={() => record.orderNo && loadOrderDetail(record.orderNo)}
            />
          </Tooltip>

          {record.orderStatus === 'PENDING' && (
            <>
              <Tooltip title="模拟支付">
                <Button
                  type="text"
                  size="small"
                  icon={<PayCircleOutlined />}
                  loading={actionLoading[record.orderNo || '']}
                  onClick={() => record.orderNo && handlePayment(record.orderNo)}
                  style={{ color: '#52c41a' }}
                />
              </Tooltip>
              <Popconfirm
                title="确认取消订单？"
                onConfirm={() => record.orderNo && handleCancel(record.orderNo)}
                okText="确认"
                cancelText="取消"
              >
                <Tooltip title="取消订单">
                  <Button
                    type="text"
                    size="small"
                    icon={<CloseOutlined />}
                    loading={actionLoading[record.orderNo || '']}
                    danger
                  />
                </Tooltip>
              </Popconfirm>
            </>
          )}
        </Space>
      ),
    },
  ];

  return (
    <PageContainer
      title="订单管理"
      subTitle="查看和管理您的订单"
      extra={[
        <Button key="refresh" icon={<ReloadOutlined />} onClick={loadOrders}>
          刷新
        </Button>,
      ]}
    >
      <Card>
        <Table
          dataSource={orders}
          columns={columns}
          rowKey="id"
          loading={loading}
          locale={{
            emptyText: (
              <Empty
                image={Empty.PRESENTED_IMAGE_SIMPLE}
                description="暂无订单数据"
              />
            ),
          }}
          pagination={{
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条记录`,
            pageSize: 10,
          }}
        />
      </Card>

      {/* 订单详情弹窗 */}
      <Modal
        title={
          <Space>
            <ShoppingOutlined />
            订单详情
          </Space>
        }
        open={detailModalVisible}
        onCancel={() => {
          setDetailModalVisible(false);
          setSelectedOrder(undefined);
        }}
        footer={[
          <Button key="close" onClick={() => setDetailModalVisible(false)}>
            关闭
          </Button>,
          ...(selectedOrder?.orderStatus === 'PENDING' ? [
            <Button
              key="cancel"
              danger
              loading={actionLoading[selectedOrder?.orderNo || '']}
              onClick={() => {
                if (selectedOrder?.orderNo) {
                  handleCancel(selectedOrder.orderNo);
                  setDetailModalVisible(false);
                }
              }}
            >
              取消订单
            </Button>,
            <Button
              key="pay"
              type="primary"
              loading={actionLoading[selectedOrder?.orderNo || '']}
              onClick={() => {
                if (selectedOrder?.orderNo) {
                  handlePayment(selectedOrder.orderNo);
                  setDetailModalVisible(false);
                }
              }}
            >
              模拟支付
            </Button>,
          ] : []),
        ]}
        width={700}
      >
        {selectedOrder && (
          <div>
            <Descriptions column={2} bordered>
              <Descriptions.Item label="订单号" span={2}>
                <Text copyable>{selectedOrder.orderNo}</Text>
              </Descriptions.Item>
              <Descriptions.Item label="套餐名称">
                {selectedOrder.packageName}
              </Descriptions.Item>
              <Descriptions.Item label="调用次数">
                <Text strong>{selectedOrder.creditAmount}次</Text>
              </Descriptions.Item>
              <Descriptions.Item label="原价">
                ¥{selectedOrder.originalPrice}
              </Descriptions.Item>
              <Descriptions.Item label="实际支付">
                <Text strong style={{ color: '#cf1322' }}>
                  ¥{selectedOrder.actualPrice}
                </Text>
              </Descriptions.Item>
              <Descriptions.Item label="支付方式">
                <Tag>{selectedOrder.paymentType ? getPaymentTypeText(selectedOrder.paymentType) : '-'}</Tag>
              </Descriptions.Item>
              <Descriptions.Item label="订单状态">
                <Badge
                  status={selectedOrder.orderStatus ? getStatusColor(selectedOrder.orderStatus) as any : 'default'}
                  text={
                    <Space>
                      {selectedOrder.orderStatus && getStatusIcon(selectedOrder.orderStatus)}
                      {selectedOrder.orderStatus ? getStatusText(selectedOrder.orderStatus) : '-'}
                    </Space>
                  }
                />
              </Descriptions.Item>
              {(selectedOrder.pointsUsed || 0) > 0 && (
                <Descriptions.Item label="使用积分">
                  <Text style={{ color: '#fa8c16' }}>{selectedOrder.pointsUsed}积分</Text>
                </Descriptions.Item>
              )}
              {(selectedOrder.moneyPaid || 0) > 0 && (
                <Descriptions.Item label="现金支付">
                  <Text style={{ color: '#52c41a' }}>¥{selectedOrder.moneyPaid}</Text>
                </Descriptions.Item>
              )}
              <Descriptions.Item label="创建时间">
                {selectedOrder.createTime ? new Date(selectedOrder.createTime).toLocaleString() : '-'}
              </Descriptions.Item>
              <Descriptions.Item label="支付时间">
                {selectedOrder.paymentTime ? new Date(selectedOrder.paymentTime).toLocaleString() : '-'}
              </Descriptions.Item>
              <Descriptions.Item label="完成时间">
                {selectedOrder.completionTime ? new Date(selectedOrder.completionTime).toLocaleString() : '-'}
              </Descriptions.Item>
              <Descriptions.Item label="过期时间">
                {selectedOrder.expireTime ? new Date(selectedOrder.expireTime).toLocaleString() : '-'}
              </Descriptions.Item>
            </Descriptions>
          </div>
        )}
      </Modal>
    </PageContainer>
  );
};

export default OrderManager;