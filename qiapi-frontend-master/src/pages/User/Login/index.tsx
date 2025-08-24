import Footer from '@/components/Footer';
import { getFakeCaptcha } from '@/services/ant-design-pro/login';
import {
  // AlipayCircleOutlined,
  LockOutlined,
  // MobileOutlined,
  // TaobaoCircleOutlined,
  UserOutlined,
  MailOutlined,
  // WeiboCircleOutlined,
} from '@ant-design/icons';
import {
  LoginForm,
  // ProFormCaptcha,
  ProFormCheckbox,
  ProFormText,
  ProFormCaptcha,
} from '@ant-design/pro-components';
import { history, useModel } from '@umijs/max';
import { Alert, message, Tabs, Space, Typography } from 'antd';
import React, { useState, useRef } from 'react';
import { flushSync } from 'react-dom';
import styles from './index.less';
import { userLoginUsingPOST, sendEmailCodeUsingPOST, userLoginByEmailUsingPOST } from '@/services/yuapi-backend/userController';

const { Text, Link } = Typography;

const LoginMessage: React.FC<{
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
const Login: React.FC = () => {
  const [userLoginState, setUserLoginState] = useState<API.LoginResult>({});
  const [type, setType] = useState<string>('account');
  const { initialState, setInitialState } = useModel('@@initialState');
  const formRef = useRef<any>();

  const handleSubmit = async (values: API.UserLoginRequest | API.EmailLoginRequest) => {
    try {
      let res;
      if (type === 'account') {
        // 用户名密码登录
        res = await userLoginUsingPOST({
          ...values,
        } as API.UserLoginRequest);
      } else {
        // 邮箱验证码登录
        res = await userLoginByEmailUsingPOST({
          ...values,
        } as API.EmailLoginRequest);
      }

      if (res.data) {
        // 先更新全局状态
        flushSync(() => {
          setInitialState({
            loginUser: res.data
          });
        });

        // 再跳转页面
        const urlParams = new URL(window.location.href).searchParams;
        history.push(urlParams.get('redirect') || '/');
        return;
      } else {
        message.error(res.message || '登录失败，请重试！');
      }
    } catch (error) {
      const defaultLoginFailureMessage = '登录失败，请重试！';
      console.log(error);
      message.error(defaultLoginFailureMessage);
    }
  };

  const { status, type: loginType } = userLoginState;
  return (
    <div className={styles.container}>
      <div className={styles.content}>
        <LoginForm
          formRef={formRef}
          logo={<img alt="logo" src="/logo.svg" />}
          title="QiApi开发平台"
          subTitle={'API 开放平台'}
          initialValues={{
            autoLogin: true,
          }}
          // actions={[
          //   '其他登录方式 :',
          //   <AlipayCircleOutlined key="AlipayCircleOutlined" className={styles.icon} />,
          //   <TaobaoCircleOutlined key="TaobaoCircleOutlined" className={styles.icon} />,
          //   <WeiboCircleOutlined key="WeiboCircleOutlined" className={styles.icon} />,
          // ]}
          onFinish={async (values) => {
            await handleSubmit(values as API.UserLoginRequest | API.EmailLoginRequest);
          }}
        >
          <Tabs
            activeKey={type}
            onChange={setType}
            centered
            items={[
              {
                key: 'account',
                label: '账户密码登录',
              },
              {
                key: 'email',
                label: '邮箱验证码登录',
              },
            ]}
          />

          {status === 'error' && loginType === 'account' && (
            <LoginMessage content={'错误的用户名和密码(admin/ant.design)'} />
          )}
          {type === 'account' && (
            <>
              <ProFormText
                name="userAccount"
                fieldProps={{
                  size: 'large',
                  prefix: <UserOutlined className={styles.prefixIcon} />,
                }}
                placeholder={'用户名: admin or user'}
                rules={[
                  {
                    required: true,
                    message: '用户名是必填项！',
                  },
                ]}
              />
              <ProFormText.Password
                name="userPassword"
                fieldProps={{
                  size: 'large',
                  prefix: <LockOutlined className={styles.prefixIcon} />,
                }}
                placeholder={'密码: ant.design'}
                rules={[
                  {
                    required: true,
                    message: '密码是必填项！',
                  },
                ]}
              />
            </>
          )}

          {type === 'email' && (
            <>
              <ProFormText
                fieldProps={{
                  size: 'large',
                  prefix: <MailOutlined className={styles.prefixIcon} />,
                }}
                name="email"
                placeholder={'请输入邮箱！'}
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
                placeholder={'请输入验证码！'}
                captchaTextRender={(timing, count) => {
                  if (timing) {
                    return `${count} ${'秒后重新获取'}`;
                  }
                  return '获取验证码';
                }}
                name="code"
                rules={[
                  {
                    required: true,
                    message: '验证码是必填项！',
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
            </>
          )}
          <div
            style={{
              marginBottom: 24,
            }}
          >
            <ProFormCheckbox noStyle name="autoLogin">
              自动登录
            </ProFormCheckbox>
            <Space style={{ float: 'right' }}>
              <Link
                onClick={() => {
                  history.push('/user/register');
                }}
              >
                注册账号
              </Link>
              <a>忘记密码 ?</a>
            </Space>
          </div>
        </LoginForm>
      </div>
      <Footer />
    </div>
  );
};
export default Login;