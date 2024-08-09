package com.yichen.project.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yichen.project.annotation.AuthCheck;
import com.yichen.project.common.BaseResponse;
import com.yichen.project.common.DeleteRequest;
import com.yichen.project.common.ErrorCode;
import com.yichen.project.common.ResultUtils;
import com.yichen.project.constant.CommonConstant;
import com.yichen.project.constant.UserConstant;
import com.yichen.project.exception.BusinessException;
import com.yichen.project.exception.ThrowUtils;
import com.yichen.project.manager.AIManager;
import com.yichen.project.manager.RedisLimiterManager;
import com.yichen.project.model.dto.chart.*;
import com.yichen.project.model.entity.Chart;
import com.yichen.project.model.entity.User;
import com.yichen.project.model.vo.BiResponse;
import com.yichen.project.service.ChartService;
import com.yichen.project.service.UserService;
import com.yichen.project.utils.ExcelUtils;
import com.yichen.project.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 帖子接口
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@RestController
@RequestMapping("/queue")
@Slf4j
public class QueueController {
    // 直接注入一个线程池的实例
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @GetMapping("/add")
    // 将任务添加到线程池中
    public void add(String name) {
        // 使用CompletableFuture运行一个异步任务
        CompletableFuture.runAsync(() -> {
            log.info("任务执行中：" + name + "执行人：" + Thread.currentThread().getName());
            try {
                Thread.sleep(600000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, threadPoolExecutor);
    }

    @GetMapping("/get")
    // 返回线程池的状态信息
    public String get() {
        Map<String, Object> map = new HashMap<>();
        int size = threadPoolExecutor.getQueue().size();
        map.put("队列长度", size);
        // 获取线程池中已经接收的任务总数
        long taskCount = threadPoolExecutor.getTaskCount();
        map.put("任务总数", taskCount);
        // 已完成的任务数
        long completedTaskCount = threadPoolExecutor.getCompletedTaskCount();
        map.put("已完成任务数", completedTaskCount);
        // 正在执行的任务数
        int activeCount = threadPoolExecutor.getActiveCount();
        map.put("正在执行的任务数", activeCount);
        System.out.println(JSONUtil.toJsonStr(map));
        System.out.println(map.toString());
        return JSONUtil.toJsonStr(map);
    }
}
