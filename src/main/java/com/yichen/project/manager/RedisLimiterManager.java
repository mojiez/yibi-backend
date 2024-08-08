package com.yichen.project.manager;

import com.yichen.project.common.ErrorCode;
import com.yichen.project.exception.BusinessException;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
@Service
public class RedisLimiterManager {
    // spring里是根据什么来注入的 盲猜是名字
    @Resource
    private RedissonClient redissonClient;
    /**
     * 限流操作
     * @param key 区分不同的限流器，单位时间内 对于某个方法的请求 每个用户 不能超过n次
     */
    public void doRateLimit(String key) {
        // 创建限流器 每秒最多访问2次 （每秒产生两个令牌）
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        rateLimiter.trySetRate(RateType.OVERALL, 2, 1, RateIntervalUnit.SECONDS);

        // 每当一个操作过来，请求一个令牌
        boolean isAcquired = rateLimiter.tryAcquire(1);
        if (!isAcquired) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST,"请求次数过多");
        }
        System.out.println("请求成功");
    }
}
