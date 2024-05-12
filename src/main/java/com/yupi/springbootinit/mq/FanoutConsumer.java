package com.yupi.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

/**
 * @ClassName FanoutConsumer
 * @Description
 * @Author Dong Feng
 * @Date 29/04/2024 16:06
 */
public class FanoutConsumer {
    private static final String EXCHANGE_NAME = "fanout_exchange";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel1 = connection.createChannel();
        Channel channel2 = connection.createChannel();

        channel1.exchangeDeclare(EXCHANGE_NAME, "fanout");
        //创建队列、分配队列名
        String queueName="server1";
        channel1.queueDeclare(queueName, true, false, false, null);
//        String queueName = channel1.queueDeclare().getQueue();
        channel1.queueBind(queueName, EXCHANGE_NAME, "");

        String queueName2="server2";
        channel2.queueDeclare(queueName2, true, false, false, null);
//        String queueName = channel1.queueDeclare().getQueue();
        channel2.queueBind(queueName2, EXCHANGE_NAME, "");

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received '" + message + "'");
        };

        DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x2] Received '" + message + "'");
        };
        channel1.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
        channel2.basicConsume(queueName2, true, deliverCallback2, consumerTag -> { });
    }

}
