package com.yichen.project.utils;

import cn.hutool.core.collection.CollUtil;
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
 * Excel相关工具类
 */
@Slf4j
public class ExcelUtils {
    /**
     * excel 转 csv
     * @param multipartFile
     * @return
     */
    public static String excelToCsv(MultipartFile multipartFile){
//        // 读取本地文件
//        File file = null;
//        try {
//            file = ResourceUtils.getFile("classpath:网站数据.xlsx");
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        }
//        List<Map<Integer, String>> list = EasyExcel.read(file)
//                .excelType(ExcelTypeEnum.XLSX)
//                .sheet()
//                .headRowNumber(0)
//                .doReadSync();

        // 读取用户上传的multipartfFle
        List<Map<Integer, String>> list = null;
        try {
            list = EasyExcel.read(multipartFile.getInputStream())
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet()
                    .headRowNumber(0)
                    .doReadSync();
        } catch (IOException e) {
            log.error("表格处理错误");
            throw new RuntimeException(e);
        }
        if(CollUtil.isEmpty(list)) return "";
        // 转换为csv
        // 读取表头
//        LinkedHashMap<Integer,String> headerMap = new LinkedHashMap<>(list.get(0));
        LinkedHashMap<Integer,String> headerMap = (LinkedHashMap<Integer, String>)list.get(0);

        // values()方法返回包含全部value值的Collection视图（不包含key值）
        List<String> headerList = headerMap.values().stream().filter(value -> {
            return ObjectUtils.isNotEmpty(value);
        }).collect(Collectors.toList());
//        System.out.println(headerList);

        // 转化为csv
        StringBuilder stringBuilder = new StringBuilder();
//        stringBuilder.append(headerList).append("\n");
        stringBuilder.append(StringUtils.join(headerList,",")).append("\n");
        // 读取剩余数据
        for (int i=1;i<list.size();i++) {
            LinkedHashMap<Integer,String> dataMap = (LinkedHashMap<Integer, String>) list.get(i);
            List<String> dataList = dataMap.values().stream().filter(value -> {
                return ObjectUtils.isNotEmpty(value);
            }).collect(Collectors.toList());
//            stringBuilder.append(dataList).append("\n");
//            System.out.println(dataList);
            stringBuilder.append(StringUtils.join(dataList,",")).append("\n");
        }
        System.out.println(stringBuilder);
        return "123";
    }
}
