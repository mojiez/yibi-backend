package com.yichen.project.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class RedisLimiterManagerTest {
    @Resource
    private RedisLimiterManager redisLimiterManager;

    @Test
    void doRateLimit() throws InterruptedException {
        String key = "nihao_12345";
        for (int i = 0;i<2;i++) {
            redisLimiterManager.doRateLimit(key);
        }
        Thread.sleep(1000);
        for (int i=0;i<5;i++) {
            redisLimiterManager.doRateLimit(key);
        }
    }
}