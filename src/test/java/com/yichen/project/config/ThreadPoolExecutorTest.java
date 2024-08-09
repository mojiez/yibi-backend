package com.yichen.project.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class ThreadPoolExecutorTest {
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;
    @Test
    public void testTreadPool() throws InterruptedException {
        // 主线程阻塞，等待异步操作完成，和直接使用同步的区别：
        // 同步代码中，如果一个任务耗时比较长，整个程序的执行会被阻塞

//        /**
//         * 同步操作
//         */
//        int result = performTask(); //这是一个耗时操作
//        System.out.println(result); // 必须等 performTask 完成后才能执行

        // 异步代码 在等待任务结果时，可以使用主线程去做其他事情：
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            // 模拟文件下载
            try {
                Thread.sleep(5000);
                System.out.println("file downloaded");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, threadPoolExecutor);
        // 主线程去做其他事情
        System.out.println("稍等，文件正在下载");
        for (int i=0;i<3;i++) {
            System.out.println("Doing other work " + i);
        }
        // 等待文件下载完成
        future.join(); // 主线程在此处阻塞
        System.out.println("运行结束，下载完成");
    }
    public int performTask() throws InterruptedException {
        Thread.sleep(10000);
        return 1;
    }
}