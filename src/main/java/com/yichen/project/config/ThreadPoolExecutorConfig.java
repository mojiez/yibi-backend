package com.yichen.project.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolExecutorConfig {
    // 创建一个工厂线程 这是一个接口， 要实现他定义的方法
    ThreadFactory threadFactory = new ThreadFactory() {
        private int count = 1;
        @Override
        // 每当创建新线程时， 就会调用newThread方法
        public Thread newThread(@NotNull Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setName("线程" + count);
            count++;
            return thread;
        }
    };
    @Bean
    ThreadPoolExecutor getThreadPoolExecutor() {
        // 这里ThreadPoolExecutor的参数应该是在 yml文件里面读取的最好
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2, 4, 100, TimeUnit.SECONDS, new ArrayBlockingQueue<>(4), threadFactory);
        return threadPoolExecutor;
    }
}
