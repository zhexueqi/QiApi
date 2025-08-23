import { Button, Form, Input, message, Card } from 'antd';
import { LockOutlined } from '@ant-design/icons';
import React, { useState } from 'react';
import { request } from '@umijs/max';

const PasswordChange: React.FC = () => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);

  // 修改密码
  const handleSubmit = async (values: any) => {
    setLoading(true);
    try {
      // 调用修改密码接口
      const res = await request('/api/user/update/password', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        data: {
          oldPassword: values.oldPassword,
          newPassword: values.newPassword,
        },
      });

      if (res.code === 0) {
        message.success('密码修改成功，请重新登录');
        form.resetFields();
        // 可以选择自动跳转到登录页
        setTimeout(() => {
          window.location.href = '/user/login';
        }, 1500);
      } else {
        message.error(res.message || '密码修改失败');
      }
    } catch (error: any) {
      message.error('密码修改失败：' + (error.message || '未知错误'));
    }
    setLoading(false);
  };

  // 验证确认密码
  const validateConfirmPassword = ({ getFieldValue }: any) => ({
    validator(_: any, value: string) {
      if (!value || getFieldValue('newPassword') === value) {
        return Promise.resolve();
      }
      return Promise.reject(new Error('两次输入的密码不一致'));
    },
  });

  return (
    <div style={{ maxWidth: 500 }}>
      <Card title="修改密码" style={{ marginTop: 16 }}>
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
          autoComplete="off"
        >
          {/* 当前密码 */}
          <Form.Item
            label="当前密码"
            name="oldPassword"
            rules={[
              { required: true, message: '请输入当前密码' },
              { min: 6, message: '密码长度不能少于6位' },
            ]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="请输入当前密码"
              size="large"
            />
          </Form.Item>

          {/* 新密码 */}
          <Form.Item
            label="新密码"
            name="newPassword"
            rules={[
              { required: true, message: '请输入新密码' },
              { min: 6, message: '密码长度不能少于6位' },
              { max: 20, message: '密码长度不能超过20位' },
              {
                pattern: /^(?=.*[a-zA-Z])(?=.*\d)[a-zA-Z\d@$!%*?&]{6,}$/,
                message: '密码必须包含字母和数字，可包含特殊字符',
              },
            ]}
            hasFeedback
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="请输入新密码"
              size="large"
            />
          </Form.Item>

          {/* 确认新密码 */}
          <Form.Item
            label="确认新密码"
            name="confirmPassword"
            dependencies={['newPassword']}
            rules={[
              { required: true, message: '请确认新密码' },
              validateConfirmPassword,
            ]}
            hasFeedback
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="请再次输入新密码"
              size="large"
            />
          </Form.Item>

          {/* 密码强度提示 */}
          <div style={{
            background: '#f0f2f5',
            padding: 12,
            borderRadius: 6,
            marginBottom: 16,
            fontSize: 12,
            color: '#666'
          }}>
            <div>密码要求：</div>
            <div>• 长度6-20位</div>
            <div>• 必须包含字母和数字</div>
            <div>• 可包含特殊字符 @$!%*?&</div>
          </div>

          {/* 提交按钮 */}
          <Form.Item>
            <Button
              type="primary"
              htmlType="submit"
              loading={loading}
              size="large"
              block
            >
              修改密码
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

export default PasswordChange;