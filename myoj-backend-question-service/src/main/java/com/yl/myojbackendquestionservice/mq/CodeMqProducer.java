package com.yl.myojbackendquestionservice.mq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Date: 2023/9/26 - 09 - 26 - 9:37
 * @Description: com.yl.myojbackendquestionservice
 * 消息生产者
 */
@Component
public class CodeMqProducer {


    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 生产者发送消息
     *
     * @param exchange   发送消息到指定交换机
     * @param routingKey 消息标识
     * @param message    消息内容
     */
    public void sendMessage(String exchange, String routingKey, String message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }
}
