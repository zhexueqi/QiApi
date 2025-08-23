import {
  Button,
  Card,
  Input,
  message,
  Modal,
  Space,
  Typography,
  Alert,
  Descriptions,
  Spin
} from 'antd';
import {
  KeyOutlined,
  CopyOutlined,
  ReloadOutlined,
  EyeOutlined,
  EyeInvisibleOutlined
} from '@ant-design/icons';
import React, { useState, useEffect } from 'react';
import {
  generateKeysUsingPOST,
  regenerateKeysUsingPOST,
  getKeysUsingGET
} from '@/services/yuapi-backend/userController';

const { Text, Paragraph } = Typography;

const KeyManagement: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [keyInfo, setKeyInfo] = useState<API.KeyInfo>();
  const [showSecretKey, setShowSecretKey] = useState(false);
  const [regenerateModalVisible, setRegenerateModalVisible] = useState(false);
  const [keyDisplayModalVisible, setKeyDisplayModalVisible] = useState(false);
  const [newGeneratedKeys, setNewGeneratedKeys] = useState<API.KeyInfo>();

  // 加载密钥信息
  const loadKeyInfo = async () => {
    setLoading(true);
    try {
      const res = await getKeysUsingGET();
      if (res.code === 0) {
        setKeyInfo(res.data);
      } else {
        // 如果没有密钥，keyInfo为空
        setKeyInfo(undefined);
      }
    } catch (error: any) {
      console.log('获取密钥失败：', error.message);
      setKeyInfo(undefined);
    }
    setLoading(false);
  };

  useEffect(() => {
    loadKeyInfo();
  }, []);

  // 生成密钥
  const handleGenerateKeys = async () => {
    setLoading(true);
    try {
      const res = await generateKeysUsingPOST();
      if (res.code === 0) {
        setNewGeneratedKeys(res.data);
        setKeyDisplayModalVisible(true); // 显示密钥展示弹窗
        message.success('API密钥生成成功');
      } else {
        message.error(res.message || '密钥生成失败');
      }
    } catch (error: any) {
      message.error('密钥生成失败：' + (error.message || '未知错误'));
    }
    setLoading(false);
  };

  // 重新生成密钥
  const handleRegenerateKeys = async () => {
    setLoading(true);
    try {
      const res = await regenerateKeysUsingPOST();
      if (res.code === 0) {
        setNewGeneratedKeys(res.data);
        setKeyDisplayModalVisible(true); // 显示密钥展示弹窗
        setRegenerateModalVisible(false);
        message.success('API密钥重新生成成功，旧密钥已失效');
      } else {
        message.error(res.message || '密钥重新生成失败');
      }
    } catch (error: any) {
      message.error('密钥重新生成失败：' + (error.message || '未知错误'));
    }
    setLoading(false);
  };

  // 复制到剪贴板
  const copyToClipboard = (text: string, label: string) => {
    navigator.clipboard.writeText(text).then(() => {
      message.success(`${label}已复制到剪贴板`);
    }).catch(() => {
      message.error('复制失败，请手动复制');
    });
  };

  // 复制格式化的密钥信息
  const copyFormattedKeys = (keys: API.KeyInfo) => {
    const formattedText = `accessKey：${keys.accessKey}\nsecretKey：${keys.secretKey}`;
    navigator.clipboard.writeText(formattedText).then(() => {
      message.success('密钥信息已复制到剪贴板');
    }).catch(() => {
      message.error('复制失败，请手动复制');
    });
  };

  // 确认已保存密钥
  const handleKeySaved = async () => {
    setKeyDisplayModalVisible(false);
    setNewGeneratedKeys(undefined);
    // 重新获取最新的密钥信息
    await loadKeyInfo();
    // 默认显示密钥（因为用户刚刚保存了密钥）
    setShowSecretKey(true);
    message.success('密钥信息已更新');
  };

  // 获取显示的SecretKey
  const getDisplaySecretKey = () => {
    if (!keyInfo?.secretKey) return '';
    if (showSecretKey) {
      return keyInfo.secretKey;
    }
    // 显示掩码
    const key = keyInfo.secretKey;
    if (key.length <= 8) return key;
    return key.substring(0, 4) + '*'.repeat(key.length - 8) + key.substring(key.length - 4);
  };

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: '50px' }}>
        <Spin size="large" />
      </div>
    );
  }

  return (
    <div style={{ maxWidth: 800 }}>
      <Alert
        type="info"
        message="API密钥说明"
        description="API密钥用于调用开放接口时的身份验证。AccessKey用于标识身份，SecretKey用于签名验证。请妥善保管您的密钥，不要泄露给他人。"
        style={{ marginBottom: 24 }}
        showIcon
      />

      {!keyInfo?.hasKeys ? (
        // 没有密钥时的界面
        <Card
          title={
            <Space>
              <KeyOutlined />
              生成API密钥
            </Space>
          }
          style={{ textAlign: 'center' }}
        >
          <div style={{ padding: '40px 20px' }}>
            <KeyOutlined style={{ fontSize: 64, color: '#1890ff', marginBottom: 16 }} />
            <p style={{ fontSize: 16, marginBottom: 24 }}>
              您还没有API密钥，请先生成密钥对来调用开放接口
            </p>
            <Button
              type="primary"
              size="large"
              icon={<KeyOutlined />}
              onClick={handleGenerateKeys}
              loading={loading}
            >
              生成API密钥
            </Button>
          </div>
        </Card>
      ) : (
        // 已有密钥时的界面
        <Card
          title={
            <Space>
              <KeyOutlined />
              API密钥管理
            </Space>
          }
          extra={
            <Button
              icon={<ReloadOutlined />}
              onClick={() => setRegenerateModalVisible(true)}
              danger
            >
              重新生成
            </Button>
          }
        >
          <Descriptions column={1} style={{ marginBottom: 24 }}>
            <Descriptions.Item label="生成时间">
              {keyInfo?.generateTime || '-'}
            </Descriptions.Item>
          </Descriptions>

          {/* AccessKey */}
          <div style={{ marginBottom: 24 }}>
            <Text strong>Access Key：</Text>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginTop: 8 }}>
              <Input
                value={keyInfo?.accessKey || ''}
                readOnly
                style={{ fontFamily: 'monospace' }}
              />
              <Button
                icon={<CopyOutlined />}
                onClick={() => copyToClipboard(keyInfo?.accessKey || '', 'Access Key')}
              >
                复制
              </Button>
            </div>
          </div>

          {/* SecretKey */}
          <div style={{ marginBottom: 24 }}>
            <Text strong>Secret Key：</Text>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginTop: 8 }}>
              <Input
                value={getDisplaySecretKey()}
                readOnly
                style={{ fontFamily: 'monospace' }}
                type={showSecretKey ? 'text' : 'password'}
              />
              <Button
                icon={showSecretKey ? <EyeInvisibleOutlined /> : <EyeOutlined />}
                onClick={() => setShowSecretKey(!showSecretKey)}
              >
                {showSecretKey ? '隐藏' : '显示'}
              </Button>
              <Button
                icon={<CopyOutlined />}
                onClick={() => copyToClipboard(keyInfo?.secretKey || '', 'Secret Key')}
              >
                复制
              </Button>
            </div>
          </div>

          {/* 使用说明 */}
          <Alert
            type="warning"
            message="安全提示"
            description={
              <div>
                <p>• 请妥善保管您的Secret Key，不要在客户端代码中硬编码</p>
                <p>• 定期更换密钥以提高安全性</p>
                <p>• 如果密钥泄露，请立即重新生成</p>
                <p>• 重新生成密钥后，旧密钥将立即失效</p>
              </div>
            }
            style={{ marginTop: 16 }}
          />
        </Card>
      )}

      {/* 重新生成确认弹窗 */}
      <Modal
        title="重新生成API密钥"
        open={regenerateModalVisible}
        onOk={handleRegenerateKeys}
        onCancel={() => setRegenerateModalVisible(false)}
        okText="确认重新生成"
        cancelText="取消"
        okButtonProps={{ danger: true, loading }}
      >
        <Alert
          type="warning"
          message="警告"
          description="重新生成密钥后，当前密钥将立即失效，所有使用旧密钥的应用将无法正常调用API。请确保您已做好相应准备。"
          style={{ marginBottom: 16 }}
          showIcon
        />
        <p>您确定要重新生成API密钥吗？</p>
      </Modal>

      {/* 密钥展示弹窗 */}
      <Modal
        title="密钥生成成功"
        open={keyDisplayModalVisible}
        onCancel={() => setKeyDisplayModalVisible(false)}
        footer={[
          <Button
            key="copy"
            type="primary"
            icon={<CopyOutlined />}
            onClick={() => newGeneratedKeys && copyFormattedKeys(newGeneratedKeys)}
            style={{ marginRight: 8 }}
          >
            复制密钥信息
          </Button>,
          <Button key="confirm" type="primary" onClick={handleKeySaved}>
            我已保存密钥
          </Button>
        ]}
        width={600}
        maskClosable={false}
        closable={false}
      >
        <Alert
          type="success"
          message="密钥生成成功！"
          description="请立即复制并保存您的密钥信息。关闭此弹窗后，出于安全考虑，完整的Secret Key将不再完整显示。"
          style={{ marginBottom: 24 }}
          showIcon
        />

        {newGeneratedKeys && (
          <div style={{ backgroundColor: '#f5f5f5', padding: 16, borderRadius: 6, marginBottom: 16 }}>
            <div style={{ marginBottom: 16 }}>
              <Text strong>Access Key：</Text>
              <div style={{ marginTop: 8 }}>
                <Input
                  value={newGeneratedKeys.accessKey}
                  readOnly
                  style={{ fontFamily: 'monospace' }}
                />
              </div>
            </div>

            <div>
              <Text strong>Secret Key：</Text>
              <div style={{ marginTop: 8 }}>
                <Input
                  value={newGeneratedKeys.secretKey}
                  readOnly
                  style={{ fontFamily: 'monospace' }}
                />
              </div>
            </div>
          </div>
        )}

        <Alert
          type="info"
          message="格式化复制格式"
          description={
            <div>
              <p>点击"复制密钥信息"按钮，将按以下格式复制到剪贴板：</p>
              <Paragraph
                code
                style={{ margin: 0, backgroundColor: '#fafafa', padding: 8, borderRadius: 4 }}
              >
                {newGeneratedKeys ? `accessKey：${newGeneratedKeys.accessKey}\nsecretKey：${newGeneratedKeys.secretKey}` : 'accessKey：xxx\nsecretKey：xxx'}
              </Paragraph>
            </div>
          }
        />

        <Alert
          type="warning"
          message="重要提示"
          description="请确保您已将密钥信息保存到安全的地方。确认保存后，Secret Key将以掩码形式显示，只有您点击显示按钮才能查看完整内容。"
          style={{ marginTop: 16 }}
        />
      </Modal>
    </div>
  );
};

export default KeyManagement;