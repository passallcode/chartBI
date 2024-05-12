package com.yupi.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.util.HashMap;
import java.util.Map;


/**
 * @ClassName DirectConsumer
 * @Description 死信队列消费者实现demo
 * @Author Dong Feng
 * @Date 07/05/2024 16:09
 */
public class DLEConsumer {
    private static final String WORK_EXCHANGE_NAME = "direct2_exchange";
    private static final String DEAD_EXCHANGE_NAME = "DLE_exchange";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(WORK_EXCHANGE_NAME, "direct");

        //指定死信队列参数
        Map<String,Object> args = new HashMap<>();
        //指定绑定的交换机
        args.put("x-dead-letter-exchange", DEAD_EXCHANGE_NAME);
        //指定绑定的死信队列
        args.put("x-dead-letter-routing-key", "boss");

        String queueName1 = "e1";
        //声明普通队列
        channel.queueDeclare(queueName1, true, false, false, args);
        channel.queueBind(queueName1, WORK_EXCHANGE_NAME, "e1");

        //指定死信队列参数
        Map<String,Object> args2 = new HashMap<>();
        args2.put("x-dead-letter-exchange", DEAD_EXCHANGE_NAME);
        args2.put("x-dead-letter-routing-key", "out");

        String queueName2 = "e2";
        //声明普通队列
        channel.queueDeclare(queueName2, true, false, false, args2);
        channel.queueBind(queueName2, WORK_EXCHANGE_NAME, "e2");

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback1 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            //拒绝消息
            channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false,false);
            System.out.println(" [e1] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            //拒绝消息
            channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false,false);
            System.out.println(" [e2] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        channel.basicConsume(queueName1, false, deliverCallback1, consumerTag -> { });
        channel.basicConsume(queueName2, false, deliverCallback2, consumerTag -> { });
    }
}
