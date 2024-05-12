package com.yupi.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.Scanner;

/**
 * @ClassName DirectProducer
 * @Description
 * @Author Dong Feng
 * @Date 07/05/2024 16:10
 */
public class DirectProducer {
    private static final String EXCHANGE_NAME = "direct_exchange";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");

            Scanner sacnner = new Scanner(System.in);
            while (sacnner.hasNext()){
                String userInput = sacnner.nextLine();
                String[] args = userInput.split(" ");
                if (args.length < 1) {
                    continue;
                }
                String message = args[0];
                String routingKey = args[1];

                channel.basicPublish(EXCHANGE_NAME, routingKey, null, message.getBytes("UTF-8"));
                System.out.println(" [x] Sent '" + message + "':'" + routingKey + "'");

            }

//            String severity = getSeverity(argv);
//            String message = getMessage(argv);


        }
    }
}
