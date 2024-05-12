package com.yupi.springbootinit.achievemq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @ClassName MqInitMain
 * @Description 用于创建测试程序用到的交换机和队列(只用在程序启动前执行一次)
 * @Author Dong Feng
 * @Date 09/05/2024 10:48
 */
public class MqInitMain {
    public static void main(String[] args) throws IOException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        try {
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            String EXCHANGE_NAME ="code_exchange";
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");

            //组建队列，随机分配
            String queueName1 = "code_queue";
            channel.queueDeclare(queueName1, true, false, false, null);
            channel.queueBind(queueName1, EXCHANGE_NAME, "my_code");
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }

    }

}
