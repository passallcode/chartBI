package com.yupi.springbootinit.manager;

import io.github.briqt.spark4j.SparkClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName SparkConfig
 * @Description
 * @Author Dong Feng
 * @Date 12/05/2024 10:27
 */
@Configuration
@ConfigurationProperties(prefix = "xunfei.client")
@Data
public class SparkConfig {
    private String appid;
    private String apiSecret;
    private String apiKey;

    @Bean
    public SparkClient sparkClient() {
        SparkClient sparkClient = new SparkClient();
        sparkClient.apiKey = apiKey;
        sparkClient.apiSecret = apiSecret;
        sparkClient.appid = appid;
        return sparkClient;
    }

}
