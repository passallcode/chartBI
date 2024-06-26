package com.yupi.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName SingleRece
 * @Description 消费者  消息接收者
 * @Author Dong Feng
 * @Date 29/04/2024 14:17
 */
public class TTLConsumer {

    private final static String QUEUE_NAME = "ttl_queue";

    public static void main(String[] argv) throws Exception {
        //创建链接工厂
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        //建立连接，创建频道
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        //创建消息队列
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("x-message-ttl", 5000);
        channel.queueDeclare(QUEUE_NAME, false, false, false, args);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
        //定义了如何处理消息
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received '" + message + "'");
        };
        //消费消息会持续阻塞
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {
        });
    }

}
