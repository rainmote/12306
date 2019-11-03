import { Button, Card, Icon, List, Typography } from 'antd';
import React, { Component } from 'react';

import { Dispatch } from 'redux';
import { PageHeaderWrapper } from '@ant-design/pro-layout';
import { connect } from 'dva';
import { StateType } from './model';
import { CardListItemDataType } from './data.d';
import styles from './style.less';

const { Paragraph } = Typography;

interface AccountManagementProps {
  ticket12306AndaccountManagement: StateType;
  dispatch: Dispatch<any>;
  loading: boolean;
}
interface AccountManagementState {
  visible: boolean;
  done: boolean;
  current?: Partial<CardListItemDataType>;
}

@connect(
  ({
    ticket12306AndaccountManagement,
    loading,
  }: {
    ticket12306AndaccountManagement: StateType;
    loading: {
      models: { [key: string]: boolean };
    };
  }) => ({
    ticket12306AndaccountManagement,
    loading: loading.models.ticket12306AndaccountManagement,
  }),
)
class AccountManagement extends Component<AccountManagementProps, AccountManagementState> {
  componentDidMount() {
    const { dispatch } = this.props;
    dispatch({
      type: 'ticket12306AndaccountManagement/fetch',
      payload: {
        count: 8,
      },
    });
  }

  render() {
    const {
      ticket12306AndaccountManagement: { list },
      loading,
    } = this.props;

    const content = (
      <div className={styles.pageHeaderContent}>
        <p>登陆12306账号，同步联系人</p>

        {/*         <div className={styles.contentLink}>
          <a>
            <img alt="" src="https://gw.alipayobjects.com/zos/rmsportal/MjEImQtenlyueSmVEfUD.svg" />{' '}
            快速开始
          </a>
          <a>
            <img alt="" src="https://gw.alipayobjects.com/zos/rmsportal/NbuDUAuBlIApFuDvWiND.svg" />{' '}
            产品简介
          </a>
          <a>
            <img alt="" src="https://gw.alipayobjects.com/zos/rmsportal/ohOEPSYdDTNnyMbGuyLb.svg" />{' '}
            产品文档
          </a>
        </div> */}
      </div>
    );

    const extraContent = (
      <div></div>
      // <div className={styles.extraImg}>
      //   <img
      //     alt="这是一个标题"
      //     src="https://gw.alipayobjects.com/zos/rmsportal/RzwpdLnhmvDJToTdfDPe.png"
      //   />
      // </div>
    );
    const nullData: Partial<CardListItemDataType> = {};
    return (
      <PageHeaderWrapper content={content} extraContent={extraContent}>
        <div className={styles.cardList}>
          <List<Partial<CardListItemDataType>>
            rowKey="id"
            loading={loading}
            grid={{ gutter: 24, lg: 3, md: 2, sm: 1, xs: 1 }}
            dataSource={[nullData, ...list]}
            renderItem={item => {
              if (item && item.id) {
                return (
                  <List.Item key={item.id}>
                    <Card
                      hoverable
                      className={styles.card}
                      actions={[<a key="option1">操作一</a>, <a key="option2">操作二</a>]}
                    >
                      <Card.Meta
                        avatar={<img alt="" className={styles.cardAvatar} src={item.avatar} />}
                        title={<a>{item.title}</a>}
                        description={
                          <Paragraph className={styles.item} ellipsis={{ rows: 3 }}>
                            {item.description}
                          </Paragraph>
                        }
                      />
                    </Card>
                  </List.Item>
                );
              }
              return (
                <List.Item>
                  <Button type="dashed" className={styles.newButton}>
                    <Icon type="plus" /> 新增账户
                  </Button>
                </List.Item>
              );
            }}
          />
        </div>
      </PageHeaderWrapper>
    );
  }
}

export default AccountManagement;
