package com.yupi.springbootinit.manager;


import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @ClassName RedisLimitManager
 * @Description TODO 专门提供RedisLimit 限流基础服务 (通用的)
 * @Author Dong Feng
 * @Date 18/04/2024 13:09
 */
@Service
public class RedisLimitManager {

    @Resource
    private RedissonClient redissonClient;
    public void doRateLimit(String key){
        //创建一个 名为rateLimiter的限流器，每秒可以访问两次
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        rateLimiter.trySetRate(RateType.OVERALL,2,1, RateIntervalUnit.SECONDS);
        //每当一个操作来了，请求令牌
        boolean b = rateLimiter.tryAcquire(1);
        if (!b){
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST);
        }
    }

}
