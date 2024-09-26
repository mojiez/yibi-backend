package com.yichen.project.controller;

import cn.hutool.core.collection.CollUtil;
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
import com.yichen.project.constant.FileConstant;
import com.yichen.project.constant.UserConstant;
import com.yichen.project.exception.BusinessException;
import com.yichen.project.exception.ThrowUtils;
import com.yichen.project.manager.AIManager;
import com.yichen.project.manager.RedisLimiterManager;
import com.yichen.project.model.dto.chart.*;
import com.yichen.project.model.dto.file.UploadFileRequest;
import com.yichen.project.model.dto.post.PostQueryRequest;
import com.yichen.project.model.entity.Chart;
import com.yichen.project.model.entity.Post;
import com.yichen.project.model.entity.User;
import com.yichen.project.model.enums.FileUploadBizEnum;
import com.yichen.project.model.vo.BiResponse;
import com.yichen.project.service.ChartService;
import com.yichen.project.service.UserService;
import com.yichen.project.utils.ExcelUtils;
import com.yichen.project.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 帖子接口
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@RestController
@RequestMapping("/chart")
@Slf4j
public class ChartController {
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;
    @Resource
    private RedisLimiterManager redisLimiterManager;
    @Resource
    private AIManager aiManager;
    @Resource
    private ChartService chartService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建
     *
     * @param chartAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addChart(@RequestBody ChartAddRequest chartAddRequest, HttpServletRequest request) {
        if (chartAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartAddRequest, chart);
        User loginUser = userService.getLoginUser(request);
        chart.setUserId(loginUser.getId());
        boolean result = chartService.save(chart);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newChartId = chart.getId();
        return ResultUtils.success(newChartId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldChart.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = chartService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param chartUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest) {
        if (chartUpdateRequest == null || chartUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartUpdateRequest, chart);
        long id = chartUpdateRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Chart> getChartVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = chartService.getById(id);
        if (chart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(chart);
    }

    /**
     * 分页获取列表（仅管理员）
     *
     * @param chartQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Chart>> listChartByPage(@RequestBody ChartQueryRequest chartQueryRequest) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<Chart>> listChartVOByPage(@RequestBody ChartQueryRequest chartQueryRequest,
            HttpServletRequest request) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 获取分页结果，通过getQueryWrapper得到一个QueryWrapper的条件来进行筛选
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<Chart>> listMyChartVOByPage(@RequestBody ChartQueryRequest chartQueryRequest,
            HttpServletRequest request) {
        if (chartQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        chartQueryRequest.setUserId(loginUser.getId());
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    // endregion


    /**
     * 编辑（用户）
     *
     * @param chartEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editChart(@RequestBody ChartEditRequest chartEditRequest, HttpServletRequest request) {
        if (chartEditRequest == null || chartEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartEditRequest, chart);

        User loginUser = userService.getLoginUser(request);
        long id = chartEditRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldChart.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 获取查询包装类
     *
     * @param chartQueryRequest
     * @return
     */
    public QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        if (chartQueryRequest == null) {
            return queryWrapper;
        }

        Long id = chartQueryRequest.getId();
        String name = chartQueryRequest.getName();
        String goal = chartQueryRequest.getGoal();
        String chartType = chartQueryRequest.getChartType();
        long userId = chartQueryRequest.getUserId();
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();

        // 拼接查询条件
        queryWrapper.eq(id!=null&&id>0, "id",id);
        queryWrapper.eq(StringUtils.isNotBlank(goal),"goal",goal);
        queryWrapper.like(StringUtils.isNotBlank(name),"name",name);
        queryWrapper.eq(StringUtils.isNotBlank(chartType),"chartType",chartType);
        queryWrapper.eq(userId>0,"userId",userId);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 智能分析同步
     *
     * @param multipartFile
     * @param genChartByAIRequest
     * @param request
     * @return
     */
    @PostMapping("/gen")
    public BaseResponse<BiResponse> genChartByAI(@RequestPart("file") MultipartFile multipartFile,
                                             GenChartByAIRequest genChartByAIRequest, HttpServletRequest request) {

        String name = genChartByAIRequest.getName();
        String goal = genChartByAIRequest.getGoal();
        String chartType = genChartByAIRequest.getChartType();
        // 校验
        ThrowUtils.throwIf(StringUtils.isBlank(goal),ErrorCode.PARAMS_ERROR,"目标为空");
        ThrowUtils.throwIf(StringUtils.isBlank(name),ErrorCode.PARAMS_ERROR,"名称为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length()>100,ErrorCode.PARAMS_ERROR,"名称过长");

        // 校验文件
        // 拿到用户请求的文件
        // 取到原始文件大小
        long size = multipartFile.getSize();
        // 取到原始文件名
        String originalFilename = multipartFile.getOriginalFilename();
        // 定义一个常量表示 1MB
        final long ONE_MB = 1024 * 1024L;
        // 如果文件太大 大于1MB 则抛出异常
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件超过1MB");

        // 校验文件后缀
        /**
         * 利用FileUtils工具类来获取文件后缀
         */
        String suffix = FileUtil.getSuffix(originalFilename);
        // 定义合法的后缀列表
        final List<String> validSuffixList = Arrays.asList("xlsx","jpg");
        // 如果后缀不是规定的，抛出异常
        ThrowUtils.throwIf(!validSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀错误");

        // 限流判断 每个用户，对于每种方法的限流
        User loginUser = userService.getLoginUser(request);
        redisLimiterManager.doRateLimit("genChartByAI_"+loginUser.getId());
        // 拼接用户输入
        // 指定图表类型
        if (StringUtils.isNotBlank(chartType)) {
            goal = goal + ",请使用:" + chartType;
        }
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求: ").append("\n").append(goal).append("\n");

        // 得到压缩后的数据
        String result = ExcelUtils.excelToCsv(multipartFile);
        userInput.append("原始数据: ").append("\n").append(result).append("\n");
        String AiResponse = aiManager.doChat(userInput.toString());
        // 将返回结果进行拆分
        String[] splits = AiResponse.split("【【【【【");
        if (splits.length < 3) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"AI响应结果错误");
        }
        String genChart = splits[1].trim();
        String genResult = splits[2].trim();

        // 将结果保存到数据库中
        Chart chart = new Chart();
        chart.setGoal(goal);
        chart.setChartData(result);
        chart.setChartType(chartType);
        chart.setName(name);
        chart.setGenChart(genChart);
        chart.setGenResult(genResult);

        chart.setUserId(loginUser.getId());

        // 保存到数据库中
        boolean save = chartService.save(chart);
        ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR, "保存图表到数据库失败");

        // 组合BiResponse
        BiResponse biResponse = new BiResponse();
        biResponse.setGenChart(genChart);
        biResponse.setGenResult(genResult);
        biResponse.setChartId(chart.getId());
        return ResultUtils.success(biResponse);
    }

    /**
     * 智能分析异步
     *
     * @param multipartFile
     * @param genChartByAIRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/async")
    public BaseResponse<BiResponse> genChartByAIAsync(@RequestPart("file") MultipartFile multipartFile,
                                                      GenChartByAIRequest genChartByAIRequest, HttpServletRequest request) {

        String name = genChartByAIRequest.getName();
        String goal = genChartByAIRequest.getGoal();
        String chartType = genChartByAIRequest.getChartType();
        // 校验
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标为空");
        ThrowUtils.throwIf(StringUtils.isBlank(name), ErrorCode.PARAMS_ERROR, "名称为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "名称过长");

        // 校验文件
        // 拿到用户请求的文件
        // 取到原始文件大小
        long size = multipartFile.getSize();
        // 取到原始文件名
        String originalFilename = multipartFile.getOriginalFilename();
        // 定义一个常量表示 1MB
        final long ONE_MB = 1024 * 1024L;
        // 如果文件太大 大于1MB 则抛出异常
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件超过1MB");

        // 校验文件后缀
        /**
         * 利用FileUtils工具类来获取文件后缀
         */
        String suffix = FileUtil.getSuffix(originalFilename);
        // 定义合法的后缀列表
        final List<String> validSuffixList = Arrays.asList("xlsx", "jpg");
        // 如果后缀不是规定的，抛出异常
        ThrowUtils.throwIf(!validSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀错误");

        // 限流判断 每个用户，对于每种方法的限流
        User loginUser = userService.getLoginUser(request);
        redisLimiterManager.doRateLimit("genChartByAI_" + loginUser.getId());

        // 将结果保存到数据库中
        Chart chart = new Chart();
        chart.setGoal(goal);
        chart.setChartType(chartType);
        chart.setName(name);
        chart.setUserId(loginUser.getId());
        chart.setStatus("wait");
        boolean save = chartService.save(chart);
        ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR, "保存图表到数据库失败");

        // 拼接用户输入
        // 指定图表类型
        if (StringUtils.isNotBlank(chartType)) {
            goal = goal + ",请使用:" + chartType;
        }
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求: ").append("\n").append(goal).append("\n");

        // 得到压缩后的数据
        String result = ExcelUtils.excelToCsv(multipartFile);
        userInput.append("原始数据: ").append("\n").append(result).append("\n");

        // 异步发送请求
        /*
        CompletableFuture.runAsync(() -> {
            // 先修改任务状态为执行中， 等执行成功后 修改为已完成
            Chart updateChart = new Chart();
            updateChart.setId(chart.getId());
            updateChart.setStatus("running");
            boolean b = chartService.updateById(updateChart);
            if (!b) {
                handleChartUpdateError(updateChart.getId(), "调用AI前，更新图表为running失败");
                return;
            }
            String AiResponse = aiManager.doChat(userInput.toString());
            // 将返回结果进行拆分
            String[] splits = AiResponse.split("【【【【【");
            if (splits.length < 3) {
                // todo handleChartUpdateError
                handleChartUpdateError(updateChart.getId(), "调用AI后，得到的结果有问题");
                return;
            }
            String genChart = splits[1].trim();
            String genResult = splits[2].trim();
            // 调用AI得到结果，再更新一次
            Chart updateChartResult = new Chart();
            updateChartResult.setId(chart.getId());
            updateChartResult.setGenChart(genChart);
            updateChartResult.setGenResult(genResult);
            updateChartResult.setStatus("succeed");
            boolean b1 = chartService.updateById(updateChartResult);
            // 操作失败 更新图表状态为失败
            if (!b1) {
                handleChartUpdateError(updateChartResult.getId(), "得到结果后更新图表失败");
                return;
            }
        }, threadPoolExecutor);
        */

        // 发送到消息队列中 调用producer

        // 组合BiResponse
        BiResponse biResponse = new BiResponse();
//        biResponse.setGenChart(genChart);
//        biResponse.setGenResult(genResult);
        biResponse.setChartId(chart.getId());
        return ResultUtils.success(biResponse);

    }

    @PostMapping("/gen/async/mq")
    public BaseResponse<BiResponse> genChartByAIAsyncMQ(@RequestPart("file") MultipartFile multipartFile,
                                                      GenChartByAIRequest genChartByAIRequest, HttpServletRequest request) {

        String name = genChartByAIRequest.getName();
        String goal = genChartByAIRequest.getGoal();
        String chartType = genChartByAIRequest.getChartType();
        // 校验
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标为空");
        ThrowUtils.throwIf(StringUtils.isBlank(name), ErrorCode.PARAMS_ERROR, "名称为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "名称过长");

        // 校验文件
        // 拿到用户请求的文件
        // 取到原始文件大小
        long size = multipartFile.getSize();
        // 取到原始文件名
        String originalFilename = multipartFile.getOriginalFilename();
        // 定义一个常量表示 1MB
        final long ONE_MB = 1024 * 1024L;
        // 如果文件太大 大于1MB 则抛出异常
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件超过1MB");

        // 校验文件后缀
        /**
         * 利用FileUtils工具类来获取文件后缀
         */
        String suffix = FileUtil.getSuffix(originalFilename);
        // 定义合法的后缀列表
        final List<String> validSuffixList = Arrays.asList("xlsx", "jpg");
        // 如果后缀不是规定的，抛出异常
        ThrowUtils.throwIf(!validSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀错误");

        // 限流判断 每个用户，对于每种方法的限流
        User loginUser = userService.getLoginUser(request);
        redisLimiterManager.doRateLimit("genChartByAI_" + loginUser.getId());

        // 将结果保存到数据库中
        Chart chart = new Chart();
        chart.setGoal(goal);
        chart.setChartType(chartType);
        chart.setName(name);
        chart.setUserId(loginUser.getId());
        chart.setStatus("wait");
        boolean save = chartService.save(chart);
        ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR, "保存图表到数据库失败");

        // 拼接用户输入
        // 指定图表类型
        if (StringUtils.isNotBlank(chartType)) {
            goal = goal + ",请使用:" + chartType;
        }
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求: ").append("\n").append(goal).append("\n");

        // 得到压缩后的数据
        String result = ExcelUtils.excelToCsv(multipartFile);
        userInput.append("原始数据: ").append("\n").append(result).append("\n");

        CompletableFuture.runAsync(() -> {
            // 先修改任务状态为执行中， 等执行成功后 修改为已完成
            Chart updateChart = new Chart();
            updateChart.setId(chart.getId());
            updateChart.setStatus("running");
            boolean b = chartService.updateById(updateChart);
            if (!b) {
                handleChartUpdateError(updateChart.getId(), "调用AI前，更新图表为running失败");
                return;
            }
            String AiResponse = aiManager.doChat(userInput.toString());
            // 将返回结果进行拆分
            String[] splits = AiResponse.split("【【【【【");
            if (splits.length < 3) {
                // todo handleChartUpdateError
                handleChartUpdateError(updateChart.getId(), "调用AI后，得到的结果有问题");
                return;
            }
            String genChart = splits[1].trim();
            String genResult = splits[2].trim();
            // 调用AI得到结果，再更新一次
            Chart updateChartResult = new Chart();
            updateChartResult.setId(chart.getId());
            updateChartResult.setGenChart(genChart);
            updateChartResult.setGenResult(genResult);
            updateChartResult.setStatus("succeed");
            boolean b1 = chartService.updateById(updateChartResult);
            // 操作失败 更新图表状态为失败
            if (!b1) {
                handleChartUpdateError(updateChartResult.getId(), "得到结果后更新图表失败");
                return;
            }
        }, threadPoolExecutor);

        // 组合BiResponse
        BiResponse biResponse = new BiResponse();
//        biResponse.setGenChart(genChart);
//        biResponse.setGenResult(genResult);
        biResponse.setChartId(chart.getId());
        return ResultUtils.success(biResponse);

    }

    // 操作失败 更新图表状态为失败
    private void handleChartUpdateError(long chartId, String execMessage) {
        Chart updateChartErrorResult = new Chart();
        updateChartErrorResult.setId(chartId);
        updateChartErrorResult.setStatus("failed");
        updateChartErrorResult.setExecMessage(execMessage);
        boolean b = chartService.updateById(updateChartErrorResult);
        if (!b) {
            log.error("更新图表失败状态 失败," + chartId + "," + execMessage);
        }
    }
}
