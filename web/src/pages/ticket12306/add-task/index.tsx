import { Card, Steps } from 'antd';
import React, { Component } from 'react';

import { PageHeaderWrapper } from '@ant-design/pro-layout';
import { connect } from 'dva';
import { StateType } from './model';
import Step1 from './components/Step1';
import Step2 from './components/Step2';
import Step3 from './components/Step3';
import styles from './style.less';

const { Step } = Steps;

interface AddTaskProps {
  current: StateType['current'];
}

@connect(({ ticket12306AndaddTask }: { ticket12306AndaddTask: StateType }) => ({
  current: ticket12306AndaddTask.current,
}))
class AddTask extends Component<AddTaskProps> {
  getCurrentStep() {
    const { current } = this.props;
    switch (current) {
      case 'info':
        return 0;
      case 'confirm':
        return 1;
      case 'result':
        return 2;
      default:
        return 0;
    }
  }

  render() {
    const currentStep = this.getCurrentStep();
    let stepComponent;
    if (currentStep === 1) {
      stepComponent = <Step2 />;
    } else if (currentStep === 2) {
      stepComponent = <Step3 />;
    } else {
      stepComponent = <Step1 />;
    }
    return (
      <PageHeaderWrapper content="选择一个12306账户，新建一个抢票任务，提交到云端">
        <Card bordered={false}>
          <>
            <Steps current={currentStep} className={styles.steps} type="navigation">
              <Step title="选择账户" />
              <Step title="填写抢票信息" />
              <Step title="提交" />
            </Steps>
            {stepComponent}
          </>
        </Card>
      </PageHeaderWrapper>
    );
  }
}

export default AddTask;
