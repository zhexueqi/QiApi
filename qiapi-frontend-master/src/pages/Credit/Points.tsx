import { PageContainer } from '@ant-design/pro-components';
import React, { useEffect, useState } from 'react';
import {
  Card,
  Row,
  Col,
  Statistic,
  Timeline,
  Button,
  message,
  Progress,
  Typography,
  Space,
  Tag,
  Descriptions,
  Alert,
} from 'antd';
import {
  getPointsBalanceUsingGET,
  initUserPointsUsingPOST,
} from '@/services/yuapi-backend/creditController';
import {
  GiftOutlined,
  TrophyOutlined,
  CalendarOutlined,
  UserAddOutlined,
  ShareAltOutlined,
  ApiOutlined,
  CheckCircleOutlined,
  ReloadOutlined,
} from '@ant-design/icons';

const { Title, Text } = Typography;

const PointsManager: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [pointBalance, setPointBalance] = useState<API.PointBalanceVO>();
  const [initLoading, setInitLoading] = useState(false);

  const loadPointBalance = async () => {
    setLoading(true);
    try {
      const res = await getPointsBalanceUsingGET();
      if (res.code === 0) {
        setPointBalance(res.data);
      } else {
        message.error(res.message || '加载积分余额失败');
      }
    } catch (error: any) {
      message.error('加载积分余额失败：' + error.message);
    } finally {
      setLoading(false);
    }
  };

  const handleInitPoints = async () => {
    setInitLoading(true);
    try {
      const res = await initUserPointsUsingPOST();
      if (res.code === 0) {
        message.success('积分账户初始化成功！');
        loadPointBalance();
      } else {
        message.error(res.message || '初始化失败');
      }
    } catch (error: any) {
      message.error('初始化失败：' + error.message);
    } finally {
      setInitLoading(false);
    }
  };

  useEffect(() => {
    loadPointBalance();
  }, []);

  const pointEarnWays = [
    {
      icon: <UserAddOutlined style={{ color: '#52c41a' }} />,
      title: '注册奖励',
      points: 100,
      description: '新用户注册即可获得',
      color: '#52c41a',
    },
    {
      icon: <CalendarOutlined style={{ color: '#1890ff' }} />,
      title: '每日签到',
      points: 10,
      description: '每日首次登录签到',
      color: '#1890ff',
    },
    {
      icon: <ApiOutlined style={{ color: '#fa8c16' }} />,
      title: 'API调用',
      points: 1,
      description: '每次成功调用API',
      color: '#fa8c16',
    },
    {
      icon: <UserAddOutlined style={{ color: '#722ed1' }} />,
      title: '邀请好友',
      points: 50,
      description: '邀请新用户注册',
      color: '#722ed1',
    },
    {
      icon: <ShareAltOutlined style={{ color: '#eb2f96' }} />,
      title: '分享推广',
      points: 20,
      description: '分享平台内容',
      color: '#eb2f96',
    },
  ];

  const usageRate = pointBalance?.totalPoints ?
    ((pointBalance.totalPoints - (pointBalance.availablePoints || 0)) / pointBalance.totalPoints * 100) : 0;

  return (
    <PageContainer
      title="积分管理"
      subTitle="查看积分余额，了解积分获取方式"
      extra={[
        <Button key="refresh" icon={<ReloadOutlined />} onClick={loadPointBalance}>
          刷新
        </Button>,
      ]}
    >
      {/* 积分账户状态检查 */}
      {pointBalance?.totalPoints === 0 && (
        <Alert
          message="积分账户未初始化"
          description="检测到您的积分账户尚未初始化，请点击下方按钮进行初始化以开始使用积分功能。"
          type="warning"
          showIcon
          action={
            <Button
              type="primary"
              loading={initLoading}
              onClick={handleInitPoints}
            >
              初始化积分账户
            </Button>
          }
          style={{ marginBottom: 24 }}
        />
      )}

      {/* 积分余额概览 */}
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={8}>
          <Card loading={loading}>
            <Statistic
              title="总积分"
              value={pointBalance?.totalPoints || 0}
              precision={0}
              valueStyle={{ color: '#3f8600' }}
              prefix={<TrophyOutlined />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card loading={loading}>
            <Statistic
              title="可用积分"
              value={pointBalance?.availablePoints || 0}
              precision={0}
              valueStyle={{ color: '#1890ff' }}
              prefix={<GiftOutlined />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card loading={loading}>
            <Statistic
              title="冻结积分"
              value={pointBalance?.frozenPoints || 0}
              precision={0}
              valueStyle={{ color: '#faad14' }}
              prefix={<CheckCircleOutlined />}
            />
          </Card>
        </Col>
      </Row>

      {/* 积分使用情况 */}
      <Card title="积分使用情况" style={{ marginBottom: 24 }}>
        <Row gutter={[16, 16]}>
          <Col xs={24} md={12}>
            <div style={{ padding: '20px 0' }}>
              <Title level={4}>积分分布</Title>
              <Progress
                type="circle"
                percent={Math.round(usageRate)}
                format={() => `${Math.round(usageRate)}%`}
                strokeColor={{
                  '0%': '#108ee9',
                  '100%': '#87d068',
                }}
                size="default"
              />
              <div style={{ marginTop: 16, textAlign: 'center' }}>
                <Text type="secondary">积分使用率</Text>
              </div>
            </div>
          </Col>
          <Col xs={24} md={12}>
            <Descriptions column={1} style={{ marginTop: 20 }}>
              <Descriptions.Item label="总积分">
                <Text strong>{pointBalance?.totalPoints || 0}</Text>
              </Descriptions.Item>
              <Descriptions.Item label="可用积分">
                <Text style={{ color: '#1890ff' }}>{pointBalance?.availablePoints || 0}</Text>
              </Descriptions.Item>
              <Descriptions.Item label="冻结积分">
                <Text style={{ color: '#faad14' }}>{pointBalance?.frozenPoints || 0}</Text>
              </Descriptions.Item>
              <Descriptions.Item label="已使用积分">
                <Text style={{ color: '#cf1322' }}>
                  {(pointBalance?.totalPoints || 0) - (pointBalance?.availablePoints || 0)}
                </Text>
              </Descriptions.Item>
            </Descriptions>
          </Col>
        </Row>
      </Card>

      {/* 积分获取方式 */}
      <Card title="积分获取方式" style={{ marginBottom: 24 }}>
        <Row gutter={[16, 16]}>
          {pointEarnWays.map((way, index) => (
            <Col xs={24} sm={12} lg={8} key={index}>
              <Card size="small" hoverable>
                <Space direction="vertical" style={{ width: '100%' }}>
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                    <Space>
                      {way.icon}
                      <Text strong>{way.title}</Text>
                    </Space>
                    <Tag color={way.color}>+{way.points}积分</Tag>
                  </div>
                  <Text type="secondary" style={{ fontSize: '12px' }}>
                    {way.description}
                  </Text>
                </Space>
              </Card>
            </Col>
          ))}
        </Row>
      </Card>
    </PageContainer>
  );
};

export default PointsManager;