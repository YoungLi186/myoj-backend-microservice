package com.yl.myojbackendjudgeservice.mq;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.yl.myojbackendcommon.common.ErrorCode;
import com.yl.myojbackendcommon.exception.BusinessException;
import com.yl.myojbackendjudgeservice.judge.JudgeService;
import com.yl.myojbackendmodel.codesandbox.JudgeInfo;
import com.yl.myojbackendmodel.entity.Question;
import com.yl.myojbackendmodel.entity.QuestionSubmit;
import com.yl.myojbackendmodel.enums.JudgeInfoMessageEnum;
import com.yl.myojbackendmodel.enums.QuestionSubmitStatusEnum;
import com.yl.myojbackendserviceclient.service.QuestionFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Component
public class MQConsumerService1 {

    @Resource
    private JudgeService judgeService;

    @Resource
    private QuestionFeignClient questionFeignClient;

    // topic需要和生产者的topic一致，consumerGroup属性是必须指定的，内容可以随意
    // selectorExpression的意思指的就是tag，默认为“*”，不设置的话会监听所有消息

    private static final String topic = "QUESTION";
    @Service
    @RocketMQMessageListener(topic = topic, selectorExpression = "*", consumerGroup = "Con_Group_One")
    public class ConsumerSend implements RocketMQListener<String> {
        // 监听到消息就会执行此方法
        @Override
        public void onMessage(String message) {
            log.info("接收到消息 ： {}", message);
            Long questionSubmitId = Long.parseLong(message);

            if (StrUtil.isBlank(message)) {
                // 消息为空，则拒绝消息（不重试），进入死信队列
                //channel.basicNack(deliveryTag, false, false);
                return;
            }

            judgeService.doJudge(questionSubmitId);
            QuestionSubmit questionSubmit = questionFeignClient.getQuestionSubmitById(questionSubmitId);
            //判题状态不为成功，说明判题出现了异常
            if (!questionSubmit.getStatus().equals(QuestionSubmitStatusEnum.SUCCEED.getValue())) {
                //channel.basicNack(deliveryTag, false, false);
                //throw new BusinessException(ErrorCode.OPERATION_ERROR, "判题失败");
                //判题失败直接返回
                return;
            }

            // 设置通过数和提交数
            Long questionId = questionSubmit.getQuestionId();
            log.info("题目id:" + questionId);

            Question question = questionFeignClient.getQuestionById(questionId);
            Integer acceptedNum = question.getAcceptedNum();
            Integer submitNum = question.getSubmitNum()+1;
            //根据判题的信息判断是否ac
            String judgeInfoStr = questionSubmit.getJudgeInfo();
            JudgeInfo judgeInfo = JSONUtil.toBean(judgeInfoStr, JudgeInfo.class);
            String judgeInfoMessage = judgeInfo.getMessage();
            if (JudgeInfoMessageEnum.ACCEPTED.getText().equals(judgeInfoMessage)){
                acceptedNum+=1;
            }
            Question updateQuestion = new Question();
            updateQuestion.setId(questionId);
            updateQuestion.setSubmitNum(submitNum);
            updateQuestion.setAcceptedNum(acceptedNum);
            boolean save = questionFeignClient.updateQuestionById(updateQuestion);
            if (!save){
                throw  new BusinessException(ErrorCode.OPERATION_ERROR,"保存失败");
            }
            // 手动确认消息
            //channel.basicAck(deliveryTag, false);
        }
    }

    // 注意：这个ConsumerSend2和上面ConsumerSend在没有添加tag做区分时，不能共存，
    // 不然生产者发送一条消息，这两个都会去消费，如果类型不同会有一个报错，所以实际运用中最好加上tag，写这只是让你看知道就行
//    @Service
//    @RocketMQMessageListener(topic = "RLT_TEST_TOPIC", consumerGroup = "Con_Group_Two")
//    public class ConsumerSend2 implements RocketMQListener<String> {
//        @Override
//        public void onMessage(String str) {
//            log.info("监听到消息：str={}", str);
//        }
//    }

//	// MessageExt：是一个消息接收通配符，不管发送的是String还是对象，都可接收，当然也可以像上面明确指定类型（我建议还是指定类型较方便）
//    @Service
//    @RocketMQMessageListener(topic = "RLT_TEST_TOPIC", selectorExpression = "tag2", consumerGroup = "Con_Group_Three")
//    public class Consumer implements RocketMQListener<MessageExt> {
//        @Override
//        public void onMessage(MessageExt messageExt) {
//            byte[] body = messageExt.getBody();
//            String msg = new String(body);
//            log.info("监听到消息：msg={}", msg);
//        }
//    }

}
