import { PageContainer } from '@ant-design/pro-components';
import '@umijs/max';
import React, { useEffect, useState } from 'react';
import ReactECharts from 'echarts-for-react';
import {
  Row,
  Col,
  Card,
  Statistic,
  DatePicker,
  Select,
  Button,
  message,
  Table,
  Tag,
  Space,
  Alert,
  Spin,
  Divider,
  Typography,
} from 'antd';
import {
  ReloadOutlined,
  BarChartOutlined,
  UserOutlined,
  ApiOutlined,
  WarningOutlined,
  RiseOutlined,
} from '@ant-design/icons';
import {
  listTopInvokeInterfaceInfoUsingGET,
  listTopCreditConsumedInterfacesUsingGET,
  listTopCreditConsumingUsersUsingGET,
  getAllInterfacesCreditStatsUsingGET,
  listLowCreditUsersUsingGET,
  getRecentCreditTrendUsingGET,
  getCreditTrendByDateRangeUsingGET,
} from '@/services/yuapi-backend/analysisController';
import type { RangePickerProps } from 'antd/es/date-picker';
import moment from 'moment';

const { RangePicker } = DatePicker;
const { Option } = Select;
const { Title, Text } = Typography;

/**
 * 接口分析 - 多维度数据分析仪表板
 * @constructor
 */
const InterfaceAnalysis: React.FC = () => {
  // ==================== 数据转换工具函数 ====================

  /** 将字符串数值转换为数字 */
  const toNumber = (value: any): number => {
    if (typeof value === 'string') {
      return parseInt(value, 10) || 0;
    }
    return value || 0;
  };

  /** 将字符串数值转换为浮点数 */
  const toFloat = (value: any): number => {
    if (typeof value === 'string') {
      return parseFloat(value) || 0;
    }
    return value || 0;
  };

  // ==================== 状态管理 ====================
  const [loading, setLoading] = useState(false);
  const [refreshing, setRefreshing] = useState(false);

  // 传统调用次数数据
  const [topInvokeData, setTopInvokeData] = useState<API.InterfaceInfoVO[]>([]);

  // 额度系统数据
  const [topCreditInterfaces, setTopCreditInterfaces] = useState<API.CreditAnalysisVO[]>([]);
  const [topCreditUsers, setTopCreditUsers] = useState<API.UserCreditStatsVO[]>([]);
  const [allInterfaceStats, setAllInterfaceStats] = useState<API.CreditAnalysisVO[]>([]);
  const [lowCreditUsers, setLowCreditUsers] = useState<API.UserCreditStatsVO[]>([]);

  // 趋势数据
  const [trendData, setTrendData] = useState<API.CreditTrendVO[]>([]);
  const [trendDays, setTrendDays] = useState(7);
  const [dateRange, setDateRange] = useState<[moment.Moment, moment.Moment] | null>(null);

  // 配置参数
  const [rankingLimit, setRankingLimit] = useState(10);
  const [lowCreditThreshold, setLowCreditThreshold] = useState(100);

  // ==================== 数据加载函数 ====================

  /** 加载传统调用次数数据 */
  const loadTopInvokeData = async () => {
    try {
      const res = await listTopInvokeInterfaceInfoUsingGET();
      if (res.data) {
        setTopInvokeData(res.data);
      }
    } catch (error: any) {
      message.error('加载接口调用统计失败：' + error.message);
    }
  };

  /** 加载额度系统数据 */
  const loadCreditData = async () => {
    try {
      const [interfacesRes, usersRes, allStatsRes, lowUsersRes] = await Promise.all([
        listTopCreditConsumedInterfacesUsingGET({ limit: rankingLimit }),
        listTopCreditConsumingUsersUsingGET({ limit: rankingLimit }),
        getAllInterfacesCreditStatsUsingGET(),
        listLowCreditUsersUsingGET({ threshold: lowCreditThreshold, limit: 20 }),
      ]);

      if (interfacesRes.data) setTopCreditInterfaces(interfacesRes.data);
      if (usersRes.data) setTopCreditUsers(usersRes.data);
      if (allStatsRes.data) setAllInterfaceStats(allStatsRes.data);
      if (lowUsersRes.data) setLowCreditUsers(lowUsersRes.data);
    } catch (error: any) {
      message.error('加载额度统计失败：' + error.message);
    }
  };

  /** 加载趋势数据 */
  const loadTrendData = async () => {
    try {
      let res;
      if (dateRange && dateRange.length === 2) {
        // 使用日期范围查询
        res = await getCreditTrendByDateRangeUsingGET({
          startDate: dateRange[0].format('YYYY-MM-DD'),
          endDate: dateRange[1].format('YYYY-MM-DD'),
        });
      } else {
        // 使用最近N天查询
        res = await getRecentCreditTrendUsingGET({ days: trendDays });
      }

      if (res.data) {
        setTrendData(res.data);
      }
    } catch (error: any) {
      message.error('加载趋势数据失败：' + error.message);
    }
  };

  /** 加载所有数据 */
  const loadAllData = async () => {
    setLoading(true);
    try {
      await Promise.all([
        loadTopInvokeData(),
        loadCreditData(),
        loadTrendData(),
      ]);
    } finally {
      setLoading(false);
    }
  };

  /** 刷新数据 */
  const refreshData = async () => {
    setRefreshing(true);
    await loadAllData();
    setRefreshing(false);
    message.success('数据刷新成功');
  };

  // ==================== 副作用处理 ====================

  useEffect(() => {
    loadAllData();
  }, []);

  // 当配置参数变化时重新加载数据
  useEffect(() => {
    if (!loading) {
      loadCreditData();
    }
  }, [rankingLimit, lowCreditThreshold]);

  // 当趋势查询条件变化时重新加载数据
  useEffect(() => {
    if (!loading) {
      loadTrendData();
    }
  }, [trendDays, dateRange]);

  // ==================== 图表配置 ====================

  /** 传统调用次数饼图配置 */
  const getInvokePieOption = () => {
    const chartData = topInvokeData.map(item => ({
      value: item.totalNum,
      name: item.name,
    }));

    return {
      title: {
        text: '接口调用次数TOP3',
        left: 'center',
        textStyle: { fontSize: 16 },
      },
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)',
      },
      legend: {
        orient: 'vertical',
        left: 'left',
        top: 'middle',
      },
      series: [
        {
          name: '调用次数',
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['60%', '50%'],
          data: chartData,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)',
            },
          },
          label: {
            show: true,
            formatter: '{b}: {c}',
          },
        },
      ],
    };
  };

  /** 额度消费柱状图配置 */
  const getCreditBarOption = () => {
    const interfaceNames = topCreditInterfaces.map(item => item.interfaceName || '');
    const consumedData = topCreditInterfaces.map(item => toNumber(item.totalCreditConsumed));
    const userCounts = topCreditInterfaces.map(item => toNumber(item.userCount));

    return {
      title: {
        text: '接口额度消费排行榜',
        left: 'center',
        textStyle: { fontSize: 16 },
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'cross' },
      },
      legend: {
        data: ['消费额度', '用户数量'],
        top: 30,
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '10%',
        top: '20%',
        containLabel: true,
      },
      xAxis: {
        type: 'category',
        data: interfaceNames,
        axisLabel: {
          interval: 0,
          rotate: 45,
        },
      },
      yAxis: [
        {
          type: 'value',
          name: '消费额度',
          position: 'left',
        },
        {
          type: 'value',
          name: '用户数量',
          position: 'right',
        },
      ],
      series: [
        {
          name: '消费额度',
          type: 'bar',
          yAxisIndex: 0,
          data: consumedData,
          itemStyle: { color: '#1890ff' },
        },
        {
          name: '用户数量',
          type: 'line',
          yAxisIndex: 1,
          data: userCounts,
          itemStyle: { color: '#52c41a' },
        },
      ],
    };
  };

  /** 趋势分析图配置 */
  const getTrendOption = () => {
    const dates = trendData.map(item => item.date || '');
    const consumed = trendData.map(item => toNumber(item.dailyConsumed));
    const recharged = trendData.map(item => toNumber(item.dailyRecharged));
    const activeUsers = trendData.map(item => toNumber(item.activeUsers));
    const newUsers = trendData.map(item => toNumber(item.newUsers));

    return {
      title: {
        text: '额度使用趋势分析',
        left: 'center',
        textStyle: { fontSize: 16 },
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'cross' },
      },
      legend: {
        data: ['消费额度', '充值额度', '活跃用户', '新增用户'],
        top: 30,
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '10%',
        top: '20%',
        containLabel: true,
      },
      xAxis: {
        type: 'category',
        data: dates,
      },
      yAxis: [
        {
          type: 'value',
          name: '额度',
          position: 'left',
        },
        {
          type: 'value',
          name: '用户数',
          position: 'right',
        },
      ],
      series: [
        {
          name: '消费额度',
          type: 'bar',
          yAxisIndex: 0,
          data: consumed,
          itemStyle: { color: '#ff4d4f' },
        },
        {
          name: '充值额度',
          type: 'bar',
          yAxisIndex: 0,
          data: recharged,
          itemStyle: { color: '#52c41a' },
        },
        {
          name: '活跃用户',
          type: 'line',
          yAxisIndex: 1,
          data: activeUsers,
          itemStyle: { color: '#1890ff' },
        },
        {
          name: '新增用户',
          type: 'line',
          yAxisIndex: 1,
          data: newUsers,
          itemStyle: { color: '#722ed1' },
        },
      ],
    };
  };

  /** 用户消费雷达图配置 */
  const getUserRadarOption = () => {
    if (topCreditUsers.length === 0) return {};

    const maxConsumed = Math.max(...topCreditUsers.map(u => toNumber(u.totalConsumedCredit)));
    const maxRemaining = Math.max(...topCreditUsers.map(u => toNumber(u.totalRemainingCredit)));
    const maxInterfaces = Math.max(...topCreditUsers.map(u => toNumber(u.activeInterfaceCount)));
    const maxAvg = Math.max(...topCreditUsers.map(u => toFloat(u.avgCreditPerInterface)));

    const indicator = [
      { name: '总消费额度', max: maxConsumed },
      { name: '剩余额度', max: maxRemaining },
      { name: '活跃接口数', max: maxInterfaces },
      { name: '平均消费', max: maxAvg },
    ];

    const seriesData = topCreditUsers.slice(0, 5).map(user => ({
      value: [
        toNumber(user.totalConsumedCredit),
        toNumber(user.totalRemainingCredit),
        toNumber(user.activeInterfaceCount),
        toFloat(user.avgCreditPerInterface),
      ],
      name: user.userName || '未知用户',
    }));

    return {
      title: {
        text: 'TOP5用户消费分析',
        left: 'center',
        textStyle: { fontSize: 16 },
      },
      tooltip: {},
      legend: {
        data: seriesData.map(item => item.name),
        bottom: 10,
      },
      radar: {
        indicator,
        center: ['50%', '50%'],
        radius: '60%',
      },
      series: [
        {
          name: '用户消费分析',
          type: 'radar',
          data: seriesData,
        },
      ],
    };
  };

  // ==================== 表格配置 ====================

  /** 低额度用户表格列配置 */
  const lowCreditColumns = [
    {
      title: '用户名',
      dataIndex: 'userName',
      key: 'userName',
      render: (text: string) => <Text strong>{text}</Text>,
    },
    {
      title: '剩余额度',
      dataIndex: 'totalRemainingCredit',
      key: 'totalRemainingCredit',
      render: (value: number | string) => {
        const numValue = toNumber(value);
        return (
          <Tag color={numValue < 50 ? 'red' : numValue < 100 ? 'orange' : 'green'}>
            {numValue}
          </Tag>
        );
      },
      sorter: (a: API.UserCreditStatsVO, b: API.UserCreditStatsVO) => {
        return toNumber(a.totalRemainingCredit) - toNumber(b.totalRemainingCredit);
      },
    },
    {
      title: '总消费',
      dataIndex: 'totalConsumedCredit',
      key: 'totalConsumedCredit',
      render: (value: number | string) => toNumber(value),
    },
    {
      title: '活跃接口',
      dataIndex: 'activeInterfaceCount',
      key: 'activeInterfaceCount',
      render: (value: number | string) => toNumber(value),
    },
    {
      title: '平均消费',
      dataIndex: 'avgCreditPerInterface',
      key: 'avgCreditPerInterface',
      render: (value: number | string) => toFloat(value).toFixed(2),
    },
  ];

  // ==================== 统计数据计算 ====================

  const totalInterfaces = allInterfaceStats.length;
  const totalCreditConsumed = allInterfaceStats.reduce((sum, item) => {
    return sum + toNumber(item.totalCreditConsumed);
  }, 0);
  const totalUsers = topCreditUsers.length;
  const avgCreditPerInterface = totalInterfaces > 0 ? (totalCreditConsumed / totalInterfaces).toFixed(2) : '0';

  // ==================== 渲染组件 ====================

  return (
    <PageContainer
      header={{
        title: '接口分析',
        subTitle: '多维度数据分析仪表板',
        extra: [
          <Button
            key="refresh"
            type="primary"
            icon={<ReloadOutlined />}
            loading={refreshing}
            onClick={refreshData}
          >
            刷新数据
          </Button>,
        ],
      }}
    >
      <Spin spinning={loading}>
        {/* 概览统计卡片 */}
        <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
          <Col xs={24} sm={12} md={6}>
            <Card>
              <Statistic
                title="接口总数"
                value={totalInterfaces}
                prefix={<ApiOutlined />}
                valueStyle={{ color: '#1890ff' }}
              />
            </Card>
          </Col>
          <Col xs={24} sm={12} md={6}>
            <Card>
              <Statistic
                title="总消费额度"
                value={totalCreditConsumed}
                prefix={<RiseOutlined />}
                valueStyle={{ color: '#cf1322' }}
              />
            </Card>
          </Col>
          <Col xs={24} sm={12} md={6}>
            <Card>
              <Statistic
                title="活跃用户"
                value={totalUsers}
                prefix={<UserOutlined />}
                valueStyle={{ color: '#52c41a' }}
              />
            </Card>
          </Col>
          <Col xs={24} sm={12} md={6}>
            <Card>
              <Statistic
                title="平均消费"
                value={avgCreditPerInterface}
                prefix={<BarChartOutlined />}
                valueStyle={{ color: '#722ed1' }}
              />
            </Card>
          </Col>
        </Row>

        {/* 控制面板 */}
        <Card style={{ marginBottom: 24 }}>
          <Row gutter={[16, 16]} align="middle">
            <Col>
              <Text strong>排行榜数量：</Text>
              <Select
                value={rankingLimit}
                onChange={setRankingLimit}
                style={{ width: 80, marginLeft: 8 }}
              >
                <Option value={5}>5</Option>
                <Option value={10}>10</Option>
                <Option value={15}>15</Option>
                <Option value={20}>20</Option>
              </Select>
            </Col>
            <Col>
              <Text strong>低额度阈值：</Text>
              <Select
                value={lowCreditThreshold}
                onChange={setLowCreditThreshold}
                style={{ width: 80, marginLeft: 8 }}
              >
                <Option value={50}>50</Option>
                <Option value={100}>100</Option>
                <Option value={200}>200</Option>
                <Option value={500}>500</Option>
              </Select>
            </Col>
            <Col>
              <Text strong>趋势天数：</Text>
              <Select
                value={trendDays}
                onChange={(value) => {
                  setTrendDays(value);
                  setDateRange(null);
                }}
                style={{ width: 80, marginLeft: 8 }}
              >
                <Option value={7}>7天</Option>
                <Option value={15}>15天</Option>
                <Option value={30}>30天</Option>
                <Option value={90}>90天</Option>
              </Select>
            </Col>
            <Col>
              <Text strong>自定义日期：</Text>
              <RangePicker
                value={dateRange}
                onChange={(dates) => {
                  setDateRange(dates as [moment.Moment, moment.Moment]);
                  if (!dates) {
                    setTrendDays(7);
                  }
                }}
                style={{ marginLeft: 8 }}
                format="YYYY-MM-DD"
              />
            </Col>
          </Row>
        </Card>

        {/* 第一行图表 */}
        <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
          <Col xs={24} lg={12}>
            <Card title="传统调用统计" extra={<ApiOutlined />}>
              <ReactECharts option={getInvokePieOption()} style={{ height: 350 }} />
            </Card>
          </Col>
          <Col xs={24} lg={12}>
            <Card title="额度消费排行" extra={<BarChartOutlined />}>
              <ReactECharts option={getCreditBarOption()} style={{ height: 350 }} />
            </Card>
          </Col>
        </Row>

        {/* 第二行图表 */}
        <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
          <Col xs={24} lg={16}>
            <Card title="趋势分析" extra={<RiseOutlined />}>
              <ReactECharts option={getTrendOption()} style={{ height: 400 }} />
            </Card>
          </Col>
          <Col xs={24} lg={8}>
            <Card title="用户消费分析" extra={<UserOutlined />}>
              <ReactECharts option={getUserRadarOption()} style={{ height: 400 }} />
            </Card>
          </Col>
        </Row>

        {/* 低额度用户预警 */}
        {lowCreditUsers.length > 0 && (
          <Card
            title={(
              <Space>
                <WarningOutlined style={{ color: '#ff4d4f' }} />
                <span>低额度用户预警</span>
                <Tag color="red">{lowCreditUsers.length}个用户</Tag>
              </Space>
            )}
            style={{ marginBottom: 24 }}
          >
            <Alert
              message="以下用户剩余额度较低，建议及时关注"
              type="warning"
              showIcon
              style={{ marginBottom: 16 }}
            />
            <Table
              columns={lowCreditColumns}
              dataSource={lowCreditUsers}
              rowKey="userId"
              size="small"
              pagination={{ pageSize: 10 }}
            />
          </Card>
        )}
      </Spin>
    </PageContainer>
  );
};

export default InterfaceAnalysis;
