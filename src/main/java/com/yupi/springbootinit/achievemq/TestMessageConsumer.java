package com.yupi.springbootinit.achievemq;

import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * @ClassName TestMessageConsumer
 * @Description 测试接收消息
 * @Author Dong Feng
 * @Date 09/05/2024 10:49
 */
@Component
@Slf4j
public class TestMessageConsumer {
    @SneakyThrows
    @RabbitListener(queues = {"code_queue"},ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel,@Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag){
        log.info("receiveMessage message={}",message);
        channel.basicAck(deliveryTag,false);
    }


}
