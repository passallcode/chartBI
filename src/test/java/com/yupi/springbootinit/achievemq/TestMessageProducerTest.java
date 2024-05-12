package com.yupi.springbootinit.achievemq;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class TestMessageProducerTest {

    @Resource
    private TestMessageProducer testMessageProducer;
    @Test
    void sendMessage() {
        testMessageProducer.sendMessage("code_exchange","my_code","hello world");
    }
}