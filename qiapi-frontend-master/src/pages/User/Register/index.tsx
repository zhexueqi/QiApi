import Footer from '@/components/Footer';
import {
  LockOutlined,
  UserOutlined,
  MailOutlined,
} from '@ant-design/icons';
import {
  LoginForm,
  ProFormText,
  ProFormCaptcha,
  ProFormCheckbox,
} from '@ant-design/pro-components';
import { history, useModel } from '@umijs/max';
import { Alert, message, Tabs, Space, Typography, Button } from 'antd';
import React, { useState, useRef } from 'react';
import styles from './index.less';
import { sendEmailCodeUsingPOST, userRegisterByEmailUsingPOST } from '@/services/yuapi-backend/userController';

const { Text, Link } = Typography;

const RegisterMessage: React.FC<{
  content: string;
}> = ({ content }) => {
  return (
    <Alert
      style={{
        marginBottom: 24,
      }}
      message={content}
      type="error"
      showIcon
    />
  );
};

const Register: React.FC = () => {
  const [registerState, setRegisterState] = useState<{
    status?: string;
    type?: string;
  }>({});
  const [type, setType] = useState<string>('account');
  const { initialState, setInitialState } = useModel('@@initialState');
  const formRef = useRef<any>();

  const handleSubmit = async (values: API.EmailRegisterRequest) => {
    try {
      // 验证密码一致性
      if (values.userPassword !== values.checkPassword) {
        message.error('两次输入的密码不一致');
        return;
      }

      // 验证必填字段
      if (!values.email) {
        message.error('请输入邮箱');
        return;
      }

      if (!values.code) {
        message.error('请输入验证码');
        return;
      }

      if (!values.userPassword) {
        message.error('请输入密码');
        return;
      }

      if (!values.checkPassword) {
        message.error('请确认密码');
        return;
      }

      // 注册
      const res = await userRegisterByEmailUsingPOST({
        ...values,
      });

      if (res.code === 0) {
        message.success('注册成功！');
        // 跳转到登录页面
        history.push('/user/login');
        return;
      } else {
        message.error(res.message || '注册失败，请重试！');
      }
    } catch (error) {
      const defaultRegisterFailureMessage = '注册失败，请重试！';
      console.log(error);
      message.error(defaultRegisterFailureMessage);
    }
  };

  const { status, type: registerType } = registerState;

  return (
    <div className={styles.container}>
      <div className={styles.content}>
        <LoginForm
          formRef={formRef}
          logo={<img alt="logo" src="/logo.svg" />}
          title="QiApi开发平台"
          subTitle={'API 开放平台 - 用户注册'}
          initialValues={{
            autoLogin: true,
          }}
          submitter={{
            searchConfig: {
              submitText: '注册',
            },
          }}
          onFinish={async (values) => {
            await handleSubmit(values as API.EmailRegisterRequest);
          }}
        >
          <Tabs
            activeKey={type}
            onChange={setType}
            centered
            items={[
              {
                key: 'account',
                label: '邮箱注册',
              },
            ]}
          />

          {status === 'error' && registerType === 'account' && (
            <RegisterMessage content={'注册失败，请检查输入信息'} />
          )}

          {type === 'account' && (
            <>
              <ProFormText
                name="email"
                fieldProps={{
                  size: 'large',
                  prefix: <MailOutlined className={styles.prefixIcon} />,
                }}
                placeholder={'请输入邮箱地址'}
                rules={[
                  {
                    required: true,
                    message: '邮箱是必填项！',
                  },
                  {
                    type: 'email',
                    message: '请输入正确的邮箱格式！',
                  },
                ]}
              />
              <ProFormCaptcha
                fieldProps={{
                  size: 'large',
                  prefix: <LockOutlined className={styles.prefixIcon} />,
                }}
                captchaProps={{
                  size: 'large',
                }}
                placeholder={'请输入验证码'}
                captchaTextRender={(timing, count) => {
                  if (timing) {
                    return `${count} 秒后重新获取`;
                  }
                  return '获取验证码';
                }}
                name="code"
                rules={[
                  {
                    required: true,
                    message: '请输入验证码！',
                  },
                ]}
                onGetCaptcha={async (email) => {
                  // 如果onGetCaptcha没有正确获取到email值，则手动从表单获取
                  let emailValue = email;
                  if (!emailValue) {
                    // 通过表单实例获取邮箱值
                    const formValues = formRef.current?.getFieldValue('email');
                    emailValue = formValues;
                  }

                  if (!emailValue) {
                    message.error('请先输入邮箱地址');
                    throw new Error('请先输入邮箱地址');
                  }

                  const result = await sendEmailCodeUsingPOST({ email: emailValue });
                  if (result.code === 0) {
                    message.success('验证码发送成功！');
                  } else {
                    message.error(result.message || '验证码发送失败');
                    throw new Error('验证码发送失败');
                  }
                }}
              />
              <ProFormText
                name="userAccount"
                fieldProps={{
                  size: 'large',
                  prefix: <UserOutlined className={styles.prefixIcon} />,
                }}
                placeholder={'请输入用户名（可选，至少4位）'}
                rules={[
                  {
                    min: 4,
                    message: '用户名至少4位！',
                  },
                ]}
              />
              <ProFormText.Password
                name="userPassword"
                fieldProps={{
                  size: 'large',
                  prefix: <LockOutlined className={styles.prefixIcon} />,
                }}
                placeholder={'请输入密码（至少8位）'}
                rules={[
                  {
                    required: true,
                    message: '密码是必填项！',
                  },
                  {
                    min: 8,
                    message: '密码至少8位！',
                  },
                ]}
              />
              <ProFormText.Password
                name="checkPassword"
                fieldProps={{
                  size: 'large',
                  prefix: <LockOutlined className={styles.prefixIcon} />,
                }}
                placeholder={'请再次输入密码'}
                rules={[
                  {
                    required: true,
                    message: '确认密码是必填项！',
                  },
                ]}
              />
            </>
          )}

          <div
            style={{
              marginBottom: 24,
              textAlign: 'center',
            }}
          >
            <Space>
              <Text type="secondary">已有账号？</Text>
              <Link
                onClick={() => {
                  history.push('/user/login');
                }}
              >
                立即登录
              </Link>
            </Space>
          </div>
        </LoginForm>
      </div>
      <Footer />
    </div>
  );
};

export default Register;