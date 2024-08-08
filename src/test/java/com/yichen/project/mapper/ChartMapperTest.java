package com.yichen.project.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class ChartMapperTest {
    @Resource
    private ChartMapper chartMapper;

    @Test
    void queryChartData() {
        String chart_id = "1815571565220225026";
        String querySql = String.format("select * from chart_%s",chart_id);
        List<Map<String, Object>> maps = chartMapper.queryChartData(querySql);
        System.out.println(maps);
    }
}