package com.yupi.springbootinit.controller;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * @ClassName ThreadTestController
 * @Description
 * @Author Dong Feng
 * @Date 20/04/2024 10:45
 */
@RestController
@RequestMapping("/queue")
@Slf4j
@Profile({"dev","local"})
public class ThreadTestController {
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;
    @GetMapping("/add")
    public void add(String name){
        //执行一个没有返回值的任务
        CompletableFuture.runAsync(() -> {
            System.out.println("执行任务中"+name+"线程名"+Thread.currentThread().getName());
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        },threadPoolExecutor);

    }

    @GetMapping("/get")
    public String get(){
        Map<String,Object> map = new HashMap<>();
        map.put("队列长度",threadPoolExecutor.getQueue().size());
        map.put("任务总数",threadPoolExecutor.getTaskCount());
        map.put("已完成任务数",threadPoolExecutor.getCompletedTaskCount());
        map.put("正在工作的线程数",threadPoolExecutor.getActiveCount());

        return JSONUtil.toJsonStr(map);
    }
}

