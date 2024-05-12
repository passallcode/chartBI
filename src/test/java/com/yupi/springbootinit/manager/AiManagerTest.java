package com.yupi.springbootinit.manager;

import io.github.briqt.spark4j.SparkClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class AiManagerTest {

    @Resource
    private AiManager aiManager;

    private final String userInput =
            "分析需求：\n" +
                    "分析网站用户的增长情况\n" +
                    "请使用：折线图\n" +
                    "原始数据：\n" +
                    "日期，用户数\n" +
                    "1号,10 \n" +
                    "2号,20\n" +
                    "3号,30";

    @Test
    void doChat() {
        String result = aiManager.doChat(userInput);
        System.out.println(result);
    }
}