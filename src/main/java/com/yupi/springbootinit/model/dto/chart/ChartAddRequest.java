package com.yupi.springbootinit.model.dto.chart;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 创建请求
 *
 * @author Dong Feng
 * @from Dong Feng
 */
@Data
public class ChartAddRequest implements Serializable {



    /**
     * 分析目标
     */
    private String goal;

    /**
     * 图表数据
     */
    private String chartDate;

    /**
     * 图表类型
     */
    private String chartType;

    /**
     * 图表名称
     */
    private String name;

    private static final long serialVersionUID = 1L;
}