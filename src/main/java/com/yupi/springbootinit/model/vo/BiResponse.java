package com.yupi.springbootinit.model.vo;

import lombok.Data;

/**
 * @ClassName BiResponse
 * @Description TODO BI返回结果
 * @Author Dong Feng
 * @Date 11/04/2024 16:28
 */
@Data
public class BiResponse {

    private String genChart;
    private String genResult;
    private Long chartId;


}
