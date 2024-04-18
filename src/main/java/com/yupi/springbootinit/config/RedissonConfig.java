package com.yupi.springbootinit.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName RedissonConfig
 * @Description TODO
 * @Author Dong Feng
 * @Date 18/04/2024 11:01
 */
@Configuration
@ConfigurationProperties(prefix = "spring.redis") //读取配置文件对应的配置
@Data
public class RedissonConfig {

    private Integer database;
    //地址
    private String host;
    //端口
    private Integer port;
    //密码
    private String password;
    @Bean
    public RedissonClient redissonClient(){

        Config config = new Config();
        config.useSingleServer()
                .setDatabase(database)
                .setAddress("redis://" + host + ":" + port)
                .setPassword(password);
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }


}
