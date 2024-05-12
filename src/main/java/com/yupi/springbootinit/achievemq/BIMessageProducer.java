package com.yupi.springbootinit.achievemq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @ClassName TestMessageProducer
 * @Description 发送消息
 * @Author Dong Feng
 * @Date 09/05/2024 10:49
 */
@Component
public class BIMessageProducer {
    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendMessage( String message) {
        rabbitTemplate.convertAndSend(BIConstant.BI_EXCHANGE, BIConstant.BI_ROUTING_KEY, message);
    }
}
