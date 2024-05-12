package com.yupi.springbootinit.achievemq;

import com.rabbitmq.client.Channel;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.constant.CommonConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.manager.AiManager;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.service.ChartService;
import com.yupi.springbootinit.utils.ExcelUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @ClassName TestMessageConsumer
 * @Description 接收消息
 * @Author Dong Feng
 * @Date 09/05/2024 10:49
 */
@Component
@Slf4j
public class BIMessageConsumer {

    @Resource
    private ChartService chartService;

    @Resource
    private AiManager aiManager;



    @SneakyThrows
    @RabbitListener(queues = {BIConstant.BI_QUEUE}, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("receiveMessage message={}", message);

        if (StringUtils.isBlank(message)) {
            //消息拒绝 、是否放回队列
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "消息为空");
        }
        Long chartId = Long.parseLong(message);
        Chart chart = chartService.getById(chartId);

        if (chart == null) {
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "图表数据为空");
        }

        //先修改图表任务状态为 “执行中”。等执行成功后，修改为 “已完成”、保存执行结果；执行失败后，状态修改为 “失败”，记录任务失败信息。
        Chart chartUpdate = new Chart();
        chartUpdate.setId(chart.getId());
        chartUpdate.setStatus("running");
        boolean updateById = chartService.updateById(chartUpdate);
        if (!updateById) {
            channel.basicNack(deliveryTag, false, false);
            handleUpdateChartFail(chart.getId(), "图表状态修改失败");
            return;
        }

        //调用AI生成数据
//        String results = aiManager.doChart(CommonConstant.BI_MODEL_ID,buildUserInput(chart));
        String results = aiManager.doChat(buildUserInput(chart));
        if (results == null) {
            channel.basicNack(deliveryTag, false, false);
            handleUpdateChartFail(chart.getId(), "AI 执行失败");

        }
        //分割数据
        String[] splits = results.split("#####");
        log.info("splits={}", splits);


        if (splits.length < 3) {
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 响应异常");
        }


        String genChart = splits[1].trim();
        String genResult = splits[2].trim();


        Chart chartUpdateSuccess = new Chart();
        chartUpdateSuccess.setId(chart.getId());
        chartUpdateSuccess.setGenChart(genChart);
        chartUpdateSuccess.setGenResult(genResult);
        chartUpdateSuccess.setStatus("succeed");
        boolean update = chartService.updateById(chartUpdateSuccess);
        if (!update) {
            channel.basicNack(deliveryTag, false, false);
            handleUpdateChartFail(chart.getId(), "图表状态修改失败");
        }

        //手动确认消息
        channel.basicAck(deliveryTag, false);
    }

    /**
     * 构建用户输入
     *
     * @return String
     */
    private String buildUserInput(Chart chart) {

        String goal = chart.getGoal();
        String chartType = chart.getChartType();
        String result = chart.getChartDate();
        //用户输入
        StringBuffer userInput = new StringBuffer();
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += "，请使用" + chartType;
        }
        userInput.append("分析需求").append("\n");
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据").append("\n");
        userInput.append(result).append("\n");

        return userInput.toString();
    }

    private void handleUpdateChartFail(Long chartId, String execMessage) {
        Chart chartUpdate = new Chart();
        chartUpdate.setId(chartId);
        chartUpdate.setStatus("failed");
        chartUpdate.setExecMessage(execMessage);
        boolean updateById = chartService.updateById(chartUpdate);
        if (!updateById) {
            log.error("图表状态修改失败" + chartId + "，execMessage：" + execMessage);
        }
    }


}
