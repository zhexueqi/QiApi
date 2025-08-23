import { PageContainer } from '@ant-design/pro-components';
import React, { useEffect, useState } from 'react';
import {
  Card,
  Row,
  Col,
  Button,
  message,
  Modal,
  Form,
  Radio,
  InputNumber,
  Typography,
  Space,
  Tag,
  Descriptions,
  Badge,
  Divider,
  Tooltip,
  Alert,
} from 'antd';
import {
  getCreditPackagesUsingGET,
  purchasePackageUsingPOST,
  getPointsBalanceUsingGET,
} from '@/services/yuapi-backend/creditController';
import {
  ShoppingCartOutlined,
  CrownOutlined,
  GiftOutlined,
  InfoCircleOutlined,
  CheckOutlined,
  ReloadOutlined,
  FireOutlined,
} from '@ant-design/icons';

const { Title, Text, Paragraph } = Typography;

const PackagePurchase: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [packages, setPackages] = useState<API.CreditPackageVO[]>([]);
  const [pointBalance, setPointBalance] = useState<API.PointBalanceVO>();
  const [purchaseModalVisible, setPurchaseModalVisible] = useState(false);
  const [selectedPackage, setSelectedPackage] = useState<API.CreditPackageVO>();
  const [purchaseLoading, setPurchaseLoading] = useState(false);
  const [form] = Form.useForm();

  const loadData = async () => {
    setLoading(true);
    try {
      const [packagesRes, pointRes] = await Promise.all([
        getCreditPackagesUsingGET(),
        getPointsBalanceUsingGET(),
      ]);

      if (packagesRes.code === 0) {
        setPackages(packagesRes.data || []);
      }
      if (pointRes.code === 0) {
        setPointBalance(pointRes.data);
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

  const handlePurchase = (pkg: API.CreditPackageVO) => {
    setSelectedPackage(pkg);
    form.setFieldsValue({
      paymentType: 'POINTS',
      pointsUsed: pkg.pointsPrice,
    });
    setPurchaseModalVisible(true);
  };

  const handleSubmitPurchase = async (values: any) => {
    if (!selectedPackage) return;

    setPurchaseLoading(true);
    try {
      const res = await purchasePackageUsingPOST({
        packageId: selectedPackage.id,
        paymentType: values.paymentType,
        pointsUsed: values.paymentType === 'MONEY' ? 0 : values.pointsUsed,
      });

      if (res.code === 0) {
        message.success('购买成功！订单号：' + res.data);
        setPurchaseModalVisible(false);
        form.resetFields();
        loadData();
      } else {
        message.error(res.message || '购买失败');
      }
    } catch (error: any) {
      message.error('购买失败：' + error.message);
    } finally {
      setPurchaseLoading(false);
    }
  };

  const getPackageIcon = (packageType: string) => {
    switch (packageType) {
      case 'BASIC':
        return <GiftOutlined />;
      case 'STANDARD':
        return <ShoppingCartOutlined />;
      case 'PROFESSIONAL':
        return <CrownOutlined />;
      case 'ENTERPRISE':
        return <FireOutlined />;
      default:
        return <GiftOutlined />;
    }
  };

  const getPackageColor = (packageType: string) => {
    switch (packageType) {
      case 'BASIC':
        return '#52c41a';
      case 'STANDARD':
        return '#1890ff';
      case 'PROFESSIONAL':
        return '#722ed1';
      case 'ENTERPRISE':
        return '#fa8c16';
      default:
        return '#1890ff';
    }
  };

  return (
    <PageContainer
      title="套餐购买"
      subTitle="选择适合您的额度套餐"
      extra={[
        <Button key="refresh" icon={<ReloadOutlined />} onClick={loadData}>
          刷新
        </Button>,
      ]}
    >
      {/* 当前积分余额 */}
      <Alert
        message="当前积分余额"
        description={`可用积分：${pointBalance?.availablePoints || 0} | 冻结积分：${pointBalance?.frozenPoints || 0}`}
        type="info"
        showIcon
        style={{ marginBottom: 24 }}
      />

      {/* 套餐列表 */}
      <Row gutter={[16, 16]}>
        {packages.map((pkg) => (
          <Col xs={24} sm={12} lg={8} xl={6} key={pkg.id}>
            <Badge.Ribbon
              text={pkg.isRecommended ? "推荐" : ""}
              color={pkg.isRecommended ? "red" : ""}
              style={{ display: pkg.isRecommended ? 'block' : 'none' }}
            >
              <Card
                hoverable
                style={{ height: '100%' }}
                actions={[
                  <Button
                    type={pkg.isRecommended ? "primary" : "default"}
                    icon={<ShoppingCartOutlined />}
                    onClick={() => handlePurchase(pkg)}
                    style={{
                      backgroundColor: pkg.isRecommended ? getPackageColor(pkg.packageType || '') : undefined
                    }}
                  >
                    立即购买
                  </Button>,
                ]}
              >
                <div style={{ textAlign: 'center', marginBottom: 16 }}>
                  <div
                    style={{
                      fontSize: '48px',
                      color: getPackageColor(pkg.packageType || ''),
                      marginBottom: 8
                    }}
                  >
                    {getPackageIcon(pkg.packageType || '')}
                  </div>
                  <Title level={4} style={{ margin: 0 }}>
                    {pkg.packageName}
                  </Title>
                  <Text type="secondary">{pkg.packageType}</Text>
                </div>

                <div style={{ textAlign: 'center', marginBottom: 16 }}>
                  <div style={{ marginBottom: 8 }}>
                    <Text style={{ fontSize: '32px', fontWeight: 'bold', color: getPackageColor(pkg.packageType || '') }}>
                      ¥{pkg.price}
                    </Text>
                  </div>
                  <div>
                    <Tag color="blue">{pkg.creditAmount}次调用</Tag>
                    <Tag color="green">{pkg.pointsPrice}积分</Tag>
                  </div>
                </div>

                <Divider />

                <Space direction="vertical" style={{ width: '100%' }} size="small">
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Text>调用次数：</Text>
                    <Text strong>{pkg.creditAmount}次</Text>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Text>有效期：</Text>
                    <Text>{pkg.validityDays ? `${pkg.validityDays}天` : '永久'}</Text>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Text>积分价格：</Text>
                    <Text strong style={{ color: '#fa8c16' }}>{pkg.pointsPrice}积分</Text>
                  </div>
                </Space>

                {pkg.description && (
                  <div style={{ marginTop: 16 }}>
                    <Paragraph
                      type="secondary"
                      style={{ fontSize: '12px', margin: 0 }}
                      ellipsis={{ rows: 2 }}
                    >
                      {pkg.description}
                    </Paragraph>
                  </div>
                )}
              </Card>
            </Badge.Ribbon>
          </Col>
        ))}
      </Row>

      {/* 购买弹窗 */}
      <Modal
        title={`购买套餐 - ${selectedPackage?.packageName}`}
        open={purchaseModalVisible}
        onCancel={() => {
          setPurchaseModalVisible(false);
          form.resetFields();
        }}
        footer={null}
        width={600}
      >
        {selectedPackage && (
          <div>
            {/* 套餐信息 */}
            <Card size="small" style={{ marginBottom: 16 }}>
              <Descriptions column={2} size="small">
                <Descriptions.Item label="套餐名称">
                  <Text strong>{selectedPackage.packageName}</Text>
                </Descriptions.Item>
                <Descriptions.Item label="调用次数">
                  <Text strong>{selectedPackage.creditAmount}次</Text>
                </Descriptions.Item>
                <Descriptions.Item label="原价">
                  <Text strong>¥{selectedPackage.price}</Text>
                </Descriptions.Item>
                <Descriptions.Item label="积分价格">
                  <Text strong style={{ color: '#fa8c16' }}>{selectedPackage.pointsPrice}积分</Text>
                </Descriptions.Item>
                <Descriptions.Item label="有效期" span={2}>
                  <Text>{selectedPackage.validityDays ? `${selectedPackage.validityDays}天` : '永久有效'}</Text>
                </Descriptions.Item>
              </Descriptions>
            </Card>

            {/* 支付方式选择 */}
            <Form
              form={form}
              onFinish={handleSubmitPurchase}
              layout="vertical"
            >
              <Form.Item
                name="paymentType"
                label="支付方式"
                rules={[{ required: true, message: '请选择支付方式' }]}
              >
                <Radio.Group>
                  <Space direction="vertical">
                    <Radio value="POINTS">
                      <Space>
                        积分支付
                        <Tag color="orange">{selectedPackage.pointsPrice}积分</Tag>
                        <Tooltip title="使用积分支付，无需现金">
                          <InfoCircleOutlined />
                        </Tooltip>
                      </Space>
                    </Radio>
                    <Radio value="MONEY">
                      <Space>
                        现金支付
                        <Tag color="green">¥{selectedPackage.price}</Tag>
                        <Tooltip title="使用现金支付">
                          <InfoCircleOutlined />
                        </Tooltip>
                      </Space>
                    </Radio>
                    <Radio value="MIXED">
                      <Space>
                        混合支付
                        <Tooltip title="积分+现金混合支付">
                          <InfoCircleOutlined />
                        </Tooltip>
                      </Space>
                    </Radio>
                  </Space>
                </Radio.Group>
              </Form.Item>

              {/* 购买须知 */}
              <Alert
                message="购买须知"
                description={
                  <ul style={{ margin: 0, paddingLeft: 16 }}>
                    <li>购买后额度将立即到账</li>
                    <li>积分支付将立即扣除相应积分</li>
                    <li>套餐有效期内可随时使用</li>
                    <li>如有问题请联系客服</li>
                  </ul>
                }
                type="info"
                style={{ marginBottom: 16 }}
              />

              {/* 提交按钮 */}
              <Form.Item>
                <Space>
                  <Button
                    type="primary"
                    htmlType="submit"
                    loading={purchaseLoading}
                    icon={<CheckOutlined />}
                    size="large"
                  >
                    确认购买
                  </Button>
                  <Button
                    onClick={() => {
                      setPurchaseModalVisible(false);
                      form.resetFields();
                    }}
                    size="large"
                  >
                    取消
                  </Button>
                </Space>
              </Form.Item>
            </Form>
          </div>
        )}
      </Modal>
    </PageContainer>
  );
};

export default PackagePurchase;