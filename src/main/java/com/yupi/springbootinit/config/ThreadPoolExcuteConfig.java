package com.yupi.springbootinit.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName ThreadPoolExcuteConfig
 * @Description  自定义线程池
 * @Author Dong Feng
 * @Date 20/04/2024 09:45
 */
@Configuration
public class ThreadPoolExcuteConfig {


    @Bean
    public ThreadPoolExecutor threadPoolExecutor(){
        //自定义线程工厂
        ThreadFactory threadFactory = new ThreadFactory(){

            private int count =1;
            @Override
            public Thread newThread(@NotNull Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("线程"+count);
                count++;
                return thread;
            }
        };


        /*
        corePoolSize（核心线程数 => 正式员工数）：正常情况下，我们的系统应该能同时工作的线程数（随时就绪的状态）
        maximumPoolSize（最大线程数 => 哪怕任务再多，你也最多招这些人）：极限情况下，我们的线程池最多有多少个线程？
        keepAliveTime（空闲线程存活时间）：非核心线程在没有任务的情况下，过多久要删除（理解为开除临时工），从而释放无用的线程资源。
        TimeUnit unit（空闲线程存活时间的单位）：分钟、秒
        workQueue（工作队列）：用于存放给线程执行的任务，存在一个队列的长度（一定要设置，不要说队列长度无限，因为也会占用资源）
        threadFactory（线程工厂）：控制每个线程的生成、线程的属性（比如线程名）
        RejectedExecutionHandler（拒绝策略）：任务队列满的时候，我们采取什么措施，比如抛异常、不抛异常、自定义策略
        */
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2,
                4,
                100,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(10000),
                threadFactory);


        return threadPoolExecutor;

    }




}
