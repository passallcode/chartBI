package com.yupi.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.util.Scanner;

/**
 * @ClassName DirectProducer
 * @Description 死信队列生产者实现demo
 * @Author Dong Feng
 * @Date 07/05/2024 16:10
 */
public class DLEProducer {
    private static final String WORK_EXCHANGE_NAME = "direct2_exchange";
    private static final String DEAD_EXCHANGE_NAME = "DLE_exchange";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            //声明死信交换机
            channel.exchangeDeclare(DEAD_EXCHANGE_NAME, "direct");

            //创建队列，随机分配队列名
            String queueName1 = "boss";
            channel.queueDeclare(queueName1, true, false, false, null);
            //绑定死信队列
            channel.queueBind(queueName1, DEAD_EXCHANGE_NAME, "boss");

            //创建队列，随机分配队列名
            String queueName2 = "out";
            channel.queueDeclare(queueName2, true, false, false, null);
            //绑定死信队列
            channel.queueBind(queueName2, DEAD_EXCHANGE_NAME, "out");

            //定义了如何处理消息
            DeliverCallback deliverCallbackBoss = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                //拒绝消息
                channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false,false);
                System.out.println(" [boss] Received '" +
                        delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
            };

            //定义了如何处理消息
            DeliverCallback deliverCallbackOut = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                //拒绝消息
                channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false,false);
                System.out.println(" [out] Received '" +
                        delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
            };

            channel.basicConsume(queueName1, false, deliverCallbackBoss, consumerTag -> { });
            channel.basicConsume(queueName2, false, deliverCallbackOut, consumerTag -> { });

            Scanner sacnner = new Scanner(System.in);
            while (sacnner.hasNext()){
                String userInput = sacnner.nextLine();
                String[] args = userInput.split(" ");
                if (args.length < 1) {
                    continue;
                }
                String message = args[0];
                String routingKey = args[1];

                channel.basicPublish(WORK_EXCHANGE_NAME, routingKey, null, message.getBytes("UTF-8"));
                System.out.println(" [x] Sent '" + message + "':'" + routingKey + "'");

            }
        }
    }
}
