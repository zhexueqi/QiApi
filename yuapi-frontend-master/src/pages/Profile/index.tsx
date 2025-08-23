import { PageContainer } from '@ant-design/pro-components';
import { useModel } from '@umijs/max';
import { Card, Tabs, message } from 'antd';
import React, { useState, useEffect, useRef } from 'react';
import ProfileInfo from './components/ProfileInfo';
import PasswordChange from './components/PasswordChange';
import KeyManagement from './components/KeyManagement';
import { getLoginUserUsingGET } from '@/services/yuapi-backend/userController';

/**
 * 个人中心页面
 */
const Profile: React.FC = () => {
  const { initialState, setInitialState } = useModel('@@initialState');
  const [loading, setLoading] = useState(false);
  const [userInfo, setUserInfo] = useState<API.UserVO>();
  const isMountedRef = useRef(true); // 用于跟踪组件是否还在挂载状态

  // 加载用户信息
  const loadUserInfo = async () => {
    if (!isMountedRef.current) return;

    setLoading(true);
    try {
      const res = await getLoginUserUsingGET();
      if (res.data && isMountedRef.current) {
        setUserInfo(res.data);
        // 更新全局状态
        setInitialState((s) => ({ ...s, loginUser: res.data }));
      }
    } catch (error: any) {
      if (isMountedRef.current) {
        message.error('获取用户信息失败：' + (error.message || '未知错误'));
      }
    } finally {
      if (isMountedRef.current) {
        setLoading(false);
      }
    }
  };

  useEffect(() => {
    loadUserInfo();

    // 清理函数
    return () => {
      isMountedRef.current = false;
    };
  }, []);

  // Tab项配置
  const tabItems = [
    {
      key: 'profile',
      label: '个人资料',
      children: (
        <ProfileInfo
          userInfo={userInfo}
          loading={loading}
          onUpdateSuccess={loadUserInfo}
        />
      ),
    },
    {
      key: 'password',
      label: '修改密码',
      children: <PasswordChange />,
    },
    {
      key: 'keys',
      label: '密钥管理',
      children: <KeyManagement />,
    },
  ];

  return (
    <PageContainer
      title="个人中心"
      content="管理您的个人信息、密码和API密钥"
    >
      <Card>
        <Tabs
          defaultActiveKey="profile"
          items={tabItems}
          size="large"
        />
      </Card>
    </PageContainer>
  );
};

export default Profile;