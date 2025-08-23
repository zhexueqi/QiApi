import { LogoutOutlined, SettingOutlined, UserOutlined } from '@ant-design/icons';
import { history, useModel } from '@umijs/max';
import { Avatar, Menu, Spin } from 'antd';
import type { ItemType } from 'antd/es/menu/hooks/useItems';
import { stringify } from 'querystring';
import type { MenuInfo } from 'rc-menu/lib/interface';
import React, { useCallback } from 'react';
import { flushSync } from 'react-dom';
import HeaderDropdown from '../HeaderDropdown';
import styles from './index.less';
import { userLogoutUsingPOST } from "@/services/yuapi-backend/userController";

export type GlobalHeaderRightProps = {
  menu?: boolean;
};

const AvatarDropdown: React.FC<GlobalHeaderRightProps> = ({ menu = true }) => {
  const { initialState, setInitialState } = useModel('@@initialState');

  const onMenuClick = useCallback(
    (event: MenuInfo) => {
      const { key } = event;
      if (key === 'logout') {
        flushSync(() => {
          setInitialState((s) => ({ ...s, loginUser: undefined }));
        });
        // 调用后端退出接口
        userLogoutUsingPOST().then(() => {
          // 退出成功后跳转到登录页
          const { search, pathname } = window.location;
          const urlParams = new URL(window.location.href).searchParams;
          const redirect = urlParams.get('redirect');

          if (window.location.pathname !== '/user/login' && !redirect) {
            history.replace({
              pathname: '/user/login',
              search: stringify({
                redirect: pathname + search,
              }),
            });
          } else {
            history.replace('/user/login');
          }
        }).catch(() => {
          // 即使后端调用失败，也要跳转到登录页
          history.replace('/user/login');
        });
        return;
      }
      if (key === 'center') {
        history.push('/profile');
        return;
      }
      history.push(`/account/${key}`);
    },
    [setInitialState],
  );

  const loading = (
    <span className={`${styles.action} ${styles.account}`}>
      <Spin
        size="small"
        style={{
          marginLeft: 8,
          marginRight: 8,
        }}
      />
    </span>
  );

  if (!initialState) {
    return loading;
  }

  const { loginUser } = initialState;

  // 检查用户是否登录，但允许userName为null
  if (!loginUser) {
    return loading;
  }

  const menuItems: ItemType[] = [
    ...(menu
      ? [
        {
          key: 'center',
          icon: <UserOutlined />,
          label: '个人中心',
        },
        {
          key: 'settings',
          icon: <SettingOutlined />,
          label: '个人设置',
        },
        {
          type: 'divider' as const,
        },
      ]
      : []),
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '退出登录',
    },
  ];

  const menuHeaderDropdown = (
    <Menu className={styles.menu} selectedKeys={[]} onClick={onMenuClick} items={menuItems} />
  );

  // 生成用户显示名称，优先级：userName > userAccount > id > 默认名称
  const displayName = loginUser.userName || loginUser.userAccount || `用户${loginUser.id}` || '匿名用户';

  // 生成默认头像，使用Ant Design的默认头像
  const avatarSrc = loginUser.userAvatar || undefined; // undefined时Avatar组件会显示默认头像

  // 获取显示名称的首字母，用作默认头像
  const avatarText = displayName ? displayName.charAt(0).toUpperCase() : 'U';

  return (
    <HeaderDropdown overlay={menuHeaderDropdown}>
      <span className={`${styles.action} ${styles.account}`}>
        <Avatar
          size="small"
          className={styles.avatar}
          src={avatarSrc}
          alt="avatar"
          style={{
            backgroundColor: !avatarSrc ? '#1890ff' : undefined,
            color: !avatarSrc ? '#ffffff' : undefined
          }}
        >
          {/* 如果没有头像，显示用户名的首字母 */}
          {!avatarSrc && avatarText}
        </Avatar>
        <span className={`${styles.name} anticon`}>{displayName}</span>
      </span>
    </HeaderDropdown>
  );
};

export default AvatarDropdown;
