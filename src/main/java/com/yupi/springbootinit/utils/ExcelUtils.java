package com.yupi.springbootinit.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.StrBuilder;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;


import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName ExcelUtils
 * @Description TODO Excel相关工具类
 * @Author Dong Feng
 * @Date 10/04/2024 13:11
 */

@Slf4j
public class ExcelUtils {
    public void doImport() throws FileNotFoundException {
        File file = ResourceUtils.getFile("classpath:test_excel.xlsx");
        List<Map<Integer, String>> list = EasyExcel.read(file)
                .excelType(ExcelTypeEnum.XLSX)
                .sheet()
                .headRowNumber(0)
                .doReadSync();
        System.out.println(list);
    }

    /**
     * excel转换成csv
     * @param multipartFile
     * @return
     */
    public static String excelToCsv(MultipartFile multipartFile){
        //读取数据
        List<Map<Integer,String>> list = null;
        try {
            list = EasyExcel.read(multipartFile.getInputStream())
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet()
                    .headRowNumber(0)
                    .doReadSync();
        } catch (IOException e) {
            log.error("表格转化成csv失败");
            throw new RuntimeException(e);
        }

        if (CollUtil.isEmpty(list)){
            return "";
        }

        //转换CSV
        StrBuilder strBuilder = new StrBuilder();
        //读取表头
        LinkedHashMap<Integer,String> headerMap = (LinkedHashMap)list.get(0);
        //过滤调为空的数据
        List<String> headerList = headerMap.values().stream().filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());
        strBuilder.append(StringUtils.join(headerList,",")).append("\n");
        //逐行读取数据
        for (int i = 1; i < list.size(); i++) {
            LinkedHashMap<Integer,String> dateMap = (LinkedHashMap)list.get(i);
            List<String> dateList = dateMap.values().stream().filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());
            strBuilder.append(StringUtils.join(dateList,",")).append("\n");

        }
        

        System.out.println(list);
        return strBuilder.toString();

    }
}
