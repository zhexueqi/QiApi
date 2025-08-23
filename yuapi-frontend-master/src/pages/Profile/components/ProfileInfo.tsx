import {
  Button,
  Form,
  Input,
  Upload,
  Avatar,
  message,
  Spin,
  Descriptions,
  Divider
} from 'antd';
import { UserOutlined, UploadOutlined } from '@ant-design/icons';
import React, { useState, useEffect } from 'react';
import { updateMyProfileUsingPOST } from '@/services/yuapi-backend/userController';

interface ProfileInfoProps {
  userInfo?: API.UserVO;
  loading: boolean;
  onUpdateSuccess: () => void;
}

const ProfileInfo: React.FC<ProfileInfoProps> = ({
  userInfo,
  loading,
  onUpdateSuccess
}) => {
  const [form] = Form.useForm();
  const [updating, setUpdating] = useState(false);
  const [avatarUrl, setAvatarUrl] = useState<string>('');

  // 初始化表单数据
  useEffect(() => {
    if (userInfo) {
      form.setFieldsValue({
        userName: userInfo.userName,
        userProfile: userInfo.userProfile,
      });
      setAvatarUrl(userInfo.userAvatar || '');
    }
  }, [userInfo, form]);

  // 处理头像上传
  const handleAvatarChange = (info: any) => {
    if (info.file.status === 'uploading') {
      return;
    }
    if (info.file.status === 'done') {
      // 这里应该从后端返回的响应中获取图片URL
      const url = info.file.response?.data?.url || '';
      setAvatarUrl(url);
      form.setFieldValue('userAvatar', url);
      message.success('头像上传成功');
    }
    if (info.file.status === 'error') {
      message.error('头像上传失败');
    }
  };

  // 提交更新
  const handleSubmit = async (values: API.UserUpdateMyRequest) => {
    setUpdating(true);
    try {
      const updateData = {
        ...values,
        userAvatar: avatarUrl,
      };

      const res = await updateMyProfileUsingPOST(updateData);
      if (res.code === 0) {
        message.success('个人信息更新成功');
        onUpdateSuccess();
      } else {
        message.error(res.message || '更新失败');
      }
    } catch (error: any) {
      message.error('更新失败：' + (error.message || '未知错误'));
    }
    setUpdating(false);
  };

  // 生成用户显示名称
  const displayName = userInfo?.userName || userInfo?.userAccount || `用户${userInfo?.id}` || '匿名用户';
  const avatarText = displayName ? displayName.charAt(0).toUpperCase() : 'U';

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: '50px' }}>
        <Spin size="large" />
      </div>
    );
  }

  return (
    <div style={{ maxWidth: 800 }}>
      {/* 用户基本信息展示 */}
      <Descriptions
        title="基本信息"
        column={2}
        style={{ marginBottom: 24 }}
      >
        <Descriptions.Item label="用户ID">{userInfo?.id}</Descriptions.Item>
        <Descriptions.Item label="用户账号">{userInfo?.userAccount}</Descriptions.Item>
        <Descriptions.Item label="用户角色">
          {userInfo?.userRole === 'admin' ? '管理员' : '普通用户'}
        </Descriptions.Item>
        <Descriptions.Item label="注册时间">
          {userInfo?.createTime ? new Date(userInfo.createTime).toLocaleString() : '-'}
        </Descriptions.Item>
      </Descriptions>

      <Divider />

      {/* 编辑表单 */}
      <h3>编辑个人资料</h3>
      <Form
        form={form}
        layout="vertical"
        onFinish={handleSubmit}
        style={{ marginTop: 16 }}
      >
        {/* 头像上传 */}
        <Form.Item label="头像">
          <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
            <Avatar
              size={80}
              src={avatarUrl}
              icon={!avatarUrl && <UserOutlined />}
              style={{
                backgroundColor: !avatarUrl ? '#1890ff' : undefined,
                color: !avatarUrl ? '#ffffff' : undefined
              }}
            >
              {!avatarUrl && avatarText}
            </Avatar>
            <Upload
              name="file"
              action="/api/upload/avatar" // 需要后端提供头像上传接口
              showUploadList={false}
              accept="image/*"
              onChange={handleAvatarChange}
            >
              <Button icon={<UploadOutlined />}>
                更换头像
              </Button>
            </Upload>
          </div>
          <div style={{ color: '#999', fontSize: 12, marginTop: 8 }}>
            支持 JPG、PNG 格式，文件大小不超过 2MB
          </div>
        </Form.Item>

        {/* 用户名 */}
        <Form.Item
          label="用户昵称"
          name="userName"
          rules={[
            { max: 50, message: '昵称长度不能超过50个字符' }
          ]}
        >
          <Input
            placeholder="请输入用户昵称"
            maxLength={50}
          />
        </Form.Item>

        {/* 个人简介 */}
        <Form.Item
          label="个人简介"
          name="userProfile"
          rules={[
            { max: 200, message: '个人简介长度不能超过200个字符' }
          ]}
        >
          <Input.TextArea
            placeholder="请输入个人简介"
            rows={4}
            maxLength={200}
            showCount
          />
        </Form.Item>

        {/* 提交按钮 */}
        <Form.Item>
          <Button
            type="primary"
            htmlType="submit"
            loading={updating}
            size="large"
          >
            保存更改
          </Button>
        </Form.Item>
      </Form>
    </div>
  );
};

export default ProfileInfo;