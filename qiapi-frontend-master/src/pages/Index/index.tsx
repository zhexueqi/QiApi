import { PageContainer } from '@ant-design/pro-components';
import React, { useEffect, useState, useRef } from 'react';
import { List, message } from 'antd';
import { listInterfaceInfoByPageUsingGET } from '@/services/yuapi-backend/interfaceInfoController';

/**
 * 主页
 * @constructor
 */
const Index: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [list, setList] = useState<API.InterfaceInfo[]>([]);
  const [total, setTotal] = useState<number>(0);
  const isMountedRef = useRef(true); // 用于跟踪组件是否还在挂载状态

  const loadData = async (current = 1, pageSize = 5) => {
    if (!isMountedRef.current) return; // 组件已卸载，不执行操作

    setLoading(true);
    try {
      const res = await listInterfaceInfoByPageUsingGET({
        current,
        pageSize,
      });

      // 检查组件是否还在挂载状态
      if (isMountedRef.current) {
        setList(res?.data?.records ?? []);
        setTotal(res?.data?.total ?? 0);
      }
    } catch (error: any) {
      if (isMountedRef.current) {
        message.error('请求失败，' + error.message);
      }
    } finally {
      if (isMountedRef.current) {
        setLoading(false);
      }
    }
  };

  useEffect(() => {
    loadData();

    // 清理函数
    return () => {
      isMountedRef.current = false;
    };
  }, []);

  return (
    <PageContainer title="在线接口开放平台">
      <List
        className="my-list"
        loading={loading}
        itemLayout="horizontal"
        dataSource={list}
        renderItem={(item) => {
          const apiLink = `/interface_info/${item.id}`;
          return (
            <List.Item actions={[<a key={item.id} href={apiLink}>查看</a>]}>
              <List.Item.Meta
                title={<a href={apiLink}>{item.name}</a>}
                description={item.description}
              />
            </List.Item>
          );
        }}
        pagination={{
          // eslint-disable-next-line @typescript-eslint/no-shadow
          showTotal(total: number) {
            return '总数：' + total;
          },
          pageSize: 5,
          total,
          onChange(page, pageSize) {
            loadData(page, pageSize);
          },
        }}
      />
    </PageContainer>
  );
};

export default Index;
