//package com.yl.myojbackendjudgeservice.rabbitmq;
//
//import cn.hutool.core.util.StrUtil;
//import cn.hutool.json.JSONUtil;
//import com.rabbitmq.client.Channel;
//import com.yl.myojbackendcommon.common.ErrorCode;
//import com.yl.myojbackendcommon.exception.BusinessException;
//import com.yl.myojbackendjudgeservice.judge.JudgeService;
//import com.yl.myojbackendmodel.codesandbox.JudgeInfo;
//import com.yl.myojbackendmodel.entity.Question;
//import com.yl.myojbackendmodel.entity.QuestionSubmit;
//import com.yl.myojbackendmodel.enums.JudgeInfoMessageEnum;
//import com.yl.myojbackendmodel.enums.QuestionSubmitStatusEnum;
//import com.yl.myojbackendserviceclient.service.QuestionFeignClient;
//import lombok.SneakyThrows;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.amqp.support.AmqpHeaders;
//import org.springframework.messaging.handler.annotation.Header;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.Resource;
//import java.io.IOException;
//
//import static com.yl.myojbackendcommon.constant.MqConstant.CODE_QUEUE;
//
///**
// * @Date: 2023/9/26 - 09 - 26 - 9:38
// * @Description: com.yl.myojbackendjudgeservice.rabbitmq】
// * 消息消费者
// */
//@Component
//@Slf4j
//public class CodeMqConsumer {
//
//    @Resource
//    private JudgeService judgeService;
//
//    @Resource
//    private QuestionFeignClient questionFeignClient;
//
//
//    /**
//     * 指定程序监听的消息队列和确认机制
//     *
//     * @param message
//     * @param channel
//     * @param deliveryTag
//     */
//    @SneakyThrows
//    @RabbitListener(queues = {CODE_QUEUE}, ackMode = "MANUAL", concurrency = "2")
//    private void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
//        log.info("接收到消息 ： {}", message);
//        Long questionSubmitId = Long.parseLong(message);
//
//        if (StrUtil.isBlank(message)) {
//            // 消息为空，则拒绝消息（不重试），进入死信队列
//            channel.basicNack(deliveryTag, false, false);
//            throw new BusinessException(ErrorCode.NULL_ERROR, "消息为空");
//        }
//
//        try {
//            judgeService.doJudge(questionSubmitId);
//            QuestionSubmit questionSubmit = questionFeignClient.getQuestionSubmitById(questionSubmitId);
//            //判题状态不为成功，说明判题出现了异常
//            if (!questionSubmit.getStatus().equals(QuestionSubmitStatusEnum.SUCCEED.getValue())) {
//                channel.basicNack(deliveryTag, false, false);
//                throw new BusinessException(ErrorCode.OPERATION_ERROR, "判题失败");
//            }
//
//            // 设置通过数
//            Long questionId = questionSubmit.getQuestionId();
//            log.info("题目id:" + questionId);
//
//            Question question = questionFeignClient.getQuestionById(questionId);
//            Integer acceptedNum = question.getAcceptedNum();
//            Integer submitNum = question.getSubmitNum()+1;
//            //根据判题的信息判断是否ac
//            String judgeInfoStr = questionSubmit.getJudgeInfo();
//            JudgeInfo judgeInfo = JSONUtil.toBean(judgeInfoStr, JudgeInfo.class);
//            String judgeInfoMessage = judgeInfo.getMessage();
//            if (JudgeInfoMessageEnum.ACCEPTED.getText().equals(judgeInfoMessage)){
//                acceptedNum+=1;
//            }
//
//            Question updateQuestion = new Question();
//            updateQuestion.setId(questionId);
//            updateQuestion.setSubmitNum(submitNum);
//            updateQuestion.setAcceptedNum(acceptedNum);
//            boolean save = questionFeignClient.updateQuestionById(updateQuestion);
//            if (!save){
//                throw  new BusinessException(ErrorCode.OPERATION_ERROR,"保存失败");
//            }
//            // 手动确认消息
//            channel.basicAck(deliveryTag, false);
//        } catch (IOException e) {
//            // 消息为空，则拒绝消息，进入死信队列
//            channel.basicNack(deliveryTag, false, false);
//            throw new RuntimeException(e);
//        }
//    }
//}
