package com.yupi.springbootinit.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.achievemq.BIMessageProducer;
import com.yupi.springbootinit.annotation.AuthCheck;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.DeleteRequest;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.constant.CommonConstant;
import com.yupi.springbootinit.constant.FileConstant;
import com.yupi.springbootinit.constant.UserConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.exception.ThrowUtils;
import com.yupi.springbootinit.manager.AiManager;
import com.yupi.springbootinit.manager.RedisLimitManager;
import com.yupi.springbootinit.model.dto.chart.*;
import com.yupi.springbootinit.model.dto.file.UploadFileRequest;
import com.yupi.springbootinit.model.dto.post.PostQueryRequest;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.model.entity.Post;
import com.yupi.springbootinit.model.entity.User;
import com.yupi.springbootinit.model.enums.FileUploadBizEnum;
import com.yupi.springbootinit.model.vo.BiResponse;
import com.yupi.springbootinit.service.ChartService;
import com.yupi.springbootinit.service.UserService;
import com.yupi.springbootinit.utils.ExcelUtils;
import com.yupi.springbootinit.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.cms.PasswordRecipientInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import static org.bouncycastle.asn1.x500.style.RFC4519Style.title;

/**
 * 帖子接口
 *
 * @author Dong Feng
 * @from Dong Feng
 */
@RestController
@RequestMapping("/chart")
@Slf4j
public class ChartController {

    @Resource
    private ChartService chartService;

    @Resource
    private UserService userService;

    @Resource
    private AiManager aiManager;
    @Resource
    private RedisLimitManager redisLimitManager;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private BIMessageProducer biMessageProducer;

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
        /*List<String> tags = chartUpdateRequest.getTags();
        if (tags != null) {
            chart.setTags(JSONUtil.toJsonStr(tags));
        }
        // 参数校验
        chartService.validChart(chart, false);*/
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
    public BaseResponse<Chart> getChartById(long id, HttpServletRequest request) {
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
    /*@PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Chart>> listChartByPage(@RequestBody ChartQueryRequest chartQueryRequest) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                chartService.getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }*/

    /**
     * 分页获取列表（封装类）
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<Chart>> listChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
            HttpServletRequest request) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
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
    @PostMapping("/my/list/page")
    public BaseResponse<Page<Chart>> listMyChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
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
     * 分页搜索（从 ES 查询，封装类）
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    /*@PostMapping("/search/page/vo")
    public BaseResponse<Page<ChartVO>> searchChartVOByPage(@RequestBody ChartQueryRequest chartQueryRequest,
            HttpServletRequest request) {
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.searchFromEs(chartQueryRequest);
        return ResultUtils.success(chartService.getChartVOPage(chartPage, request));
    }*/

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
        /*List<String> tags = chartEditRequest.getTags();
        if (tags != null) {
            chart.setTags(JSONUtil.toJsonStr(tags));
        }
        // 参数校验
        chartService.validChart(chart, false);*/
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
    private QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        if (chartQueryRequest == null) {
            return queryWrapper;
        }

        Long id = chartQueryRequest.getId();
        String goal = chartQueryRequest.getGoal();
        String name = chartQueryRequest.getName();
        String chartType = chartQueryRequest.getChartType();
        Long userId = chartQueryRequest.getUserId();
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();

        queryWrapper.eq(id != null && id > 0, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(goal), "goal", goal);
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.eq(StringUtils.isNotBlank(chartType), "chartType", chartType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete",false);

        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }


    /**
     * 文件上传，智能分析（同步）
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen")
    public BaseResponse<BiResponse> genChartByAi(@RequestPart("file") MultipartFile multipartFile,
                                             GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {

        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();

        //获取当前登录的用户
        User loginUser = userService.getLoginUser(request);

        //校验参数
        ThrowUtils.throwIf(StringUtils.isBlank(goal),ErrorCode.PARAMS_ERROR,"目标为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length()>100,ErrorCode.PARAMS_ERROR,"名称过长");

        //校验文件
        long size = multipartFile.getSize();
        String orginalFileName = multipartFile.getOriginalFilename();
        //校验文件大小
        final long one_MB= 1024*1024L;
        ThrowUtils.throwIf(size>one_MB,ErrorCode.PARAMS_ERROR,"文件超过1M");
        //校验文件后缀
        String suffix = FileUtil.getSuffix(orginalFileName);
        //定义允许的文件后缀
        final List<String> validFileSuffix = Arrays.asList("xlsx","xls");
        ThrowUtils.throwIf(!validFileSuffix.contains(suffix),ErrorCode.PARAMS_ERROR,"文件后缀不合法");

        //限流判断,每个用户一个限流器
        redisLimitManager.doRateLimit("genChartByAi_"+loginUser.getId());

        //用户输入
        StringBuffer userInput = new StringBuffer();
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)){
            userGoal +="，请使用"+chartType;
        }
        userInput.append("分析需求").append("\n");
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据").append("\n");
        //压缩后的数据
        String result = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(result).append("\n");

        //long modelId = CommonConstant.BI_MODEL_ID;
//        Long modelId = CommonConstant.BI_MODEL_ID;
//        String results = aiManager.doChart(modelId,userInput.toString());
        String results = aiManager.doChat(userInput.toString());
        //分割数据
        String[] splits = results.split("#####");
        if (splits.length<3){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"AI 响应异常");
        }
        String genChart =splits[1].trim();
        String genResult = splits[2].trim();
        //插入到数据库
        Chart chart = new Chart();
        chart.setGoal(goal);
        chart.setName(name);
        chart.setChartDate(result);
        chart.setChartType(chartType);
        chart.setGenChart(genChart);
        chart.setStatus("succeed");
        chart.setGenResult(genResult);
        chart.setUserId(loginUser.getId());
        Boolean saveResults=chartService.save(chart);
        ThrowUtils.throwIf(!saveResults,ErrorCode.SYSTEM_ERROR,"图表插入失败");

        BiResponse biResponse = new BiResponse();
        biResponse.setGenChart(genChart);
        biResponse.setGenResult(genResult);
        biResponse.setChartId(chart.getId());

        return ResultUtils.success(biResponse);

    }

    /**
     * 文件上传，智能分析（异步）
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/async")
    public BaseResponse<BiResponse> genChartByAiAsync(@RequestPart("file") MultipartFile multipartFile,
                                                      GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {

        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();

        //获取当前登录的用户
        User loginUser = userService.getLoginUser(request);

        //校验参数
        ThrowUtils.throwIf(StringUtils.isBlank(goal),ErrorCode.PARAMS_ERROR,"目标为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length()>100,ErrorCode.PARAMS_ERROR,"名称过长");

        //校验文件
        long size = multipartFile.getSize();
        String orginalFileName = multipartFile.getOriginalFilename();
        //校验文件大小
        final long one_MB= 1024*1024L;
        ThrowUtils.throwIf(size>one_MB,ErrorCode.PARAMS_ERROR,"文件超过1M");
        //校验文件后缀
        String suffix = FileUtil.getSuffix(orginalFileName);
        //定义允许的文件后缀
        final List<String> validFileSuffix = Arrays.asList("xlsx","xls");
        ThrowUtils.throwIf(!validFileSuffix.contains(suffix),ErrorCode.PARAMS_ERROR,"文件后缀不合法");

        //限流判断,每个用户一个限流器
        redisLimitManager.doRateLimit("genChartByAi_"+loginUser.getId());

        //用户输入
        StringBuffer userInput = new StringBuffer();
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)){
            userGoal +="，请使用"+chartType;
        }
        userInput.append("分析需求").append("\n");
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据").append("\n");
        //压缩后的数据
        String result = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(result).append("\n");

        //插入到数据库
        Chart chart = new Chart();
        chart.setGoal(goal);
        chart.setName(name);
        chart.setChartDate(result);
        chart.setChartType(chartType);
        chart.setStatus("wait");  //TODO 枚举
        chart.setUserId(loginUser.getId());
        Boolean saveResults=chartService.save(chart);
        ThrowUtils.throwIf(!saveResults,ErrorCode.SYSTEM_ERROR,"图表插入失败");

        //队列满后抛出异常
        try {
            //提交任务到线程池
            CompletableFuture.runAsync(() -> {

                //先修改图表任务状态为 “执行中”。等执行成功后，修改为 “已完成”、保存执行结果；执行失败后，状态修改为 “失败”，记录任务失败信息。
                Chart chartUpdate = new Chart();
                chartUpdate.setId(chart.getId());
                chartUpdate.setStatus("running");
                boolean updateById = chartService.updateById(chartUpdate);
                if (!updateById){
                    handleUpdateChartFail(chart.getId(),"图表状态修改失败");
                    return;
                }

                //调用AI生成数据
                String results = aiManager.doChat(userInput.toString());

                //分割数据
                String[] splits = results.split("#####");
                if (splits.length<3){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR,"AI 响应异常");
                }
                String genChart =splits[1].trim();
                String genResult = splits[2].trim();

                Chart chartUpdateSuccess = new Chart();
                chartUpdateSuccess.setId(chart.getId());
                chartUpdateSuccess.setGenChart(genChart);
                chartUpdateSuccess.setGenResult(genResult);
                chartUpdateSuccess.setStatus("succeed");
                boolean update = chartService.updateById(chartUpdateSuccess);
                if (!update){
                    handleUpdateChartFail(chart.getId(),"图表状态修改失败");

                }
            },threadPoolExecutor);
        }catch (Exception e){
            throw new BusinessException(ErrorCode.QUEUE_IS_FULL_ERROR, "队列已满，请稍后再试");
        }

        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(chart.getId());
        return ResultUtils.success(biResponse);

    }

    @PostMapping("/gen/async/mq")
    public BaseResponse<BiResponse> genChartByAiAsyncMQ(@RequestPart("file") MultipartFile multipartFile,
                                                      GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {

        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();

        //获取当前登录的用户
        User loginUser = userService.getLoginUser(request);

        //校验参数
        ThrowUtils.throwIf(StringUtils.isBlank(goal),ErrorCode.PARAMS_ERROR,"目标为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length()>100,ErrorCode.PARAMS_ERROR,"名称过长");

        //校验文件
        long size = multipartFile.getSize();
        String orginalFileName = multipartFile.getOriginalFilename();
        //校验文件大小
        final long one_MB= 1024*1024L;
        ThrowUtils.throwIf(size>one_MB,ErrorCode.PARAMS_ERROR,"文件超过1M");
        //校验文件后缀
        String suffix = FileUtil.getSuffix(orginalFileName);
        //定义允许的文件后缀
        final List<String> validFileSuffix = Arrays.asList("xlsx","xls");
        ThrowUtils.throwIf(!validFileSuffix.contains(suffix),ErrorCode.PARAMS_ERROR,"文件后缀不合法");

        //限流判断,每个用户一个限流器
        redisLimitManager.doRateLimit("genChartByAi_"+loginUser.getId());

        //用户输入
        StringBuffer userInput = new StringBuffer();
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)){
            userGoal +="，请使用"+chartType;
        }
        userInput.append("分析需求").append("\n");
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据").append("\n");
        //压缩后的数据
        String result = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(result).append("\n");


        //插入到数据库
        Chart chart = new Chart();
        chart.setGoal(goal);
        chart.setName(name);
        chart.setChartDate(result);
        chart.setChartType(chartType);
        chart.setStatus("wait");  //TODO 枚举
        chart.setUserId(loginUser.getId());
        Boolean saveResults=chartService.save(chart);
        ThrowUtils.throwIf(!saveResults,ErrorCode.SYSTEM_ERROR,"图表插入失败");

        Long newChartId = chart.getId();

        //发送消息到消息队列.传图表id
        biMessageProducer.sendMessage(String.valueOf(newChartId));

        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(newChartId);
        return ResultUtils.success(biResponse);

    }

    private void handleUpdateChartFail(Long chartId,String execMessage){
        Chart chartUpdate = new Chart();
        chartUpdate.setId(chartId);
        chartUpdate.setStatus("failed");
        chartUpdate.setExecMessage(execMessage);
        boolean updateById = chartService.updateById(chartUpdate);
        if (!updateById){
            log.error("图表状态修改失败"+chartId+"，execMessage："+execMessage);
        }
    }


}
