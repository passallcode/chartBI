package com.yupi.springbootinit.achievemq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @ClassName TestMessageProducer
 * @Description 测试发送消息
 * @Author Dong Feng
 * @Date 09/05/2024 10:49
 */
@Component
public class TestMessageProducer {
    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendMessage(String exchange, String routingKey, String message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }
}
