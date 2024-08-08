package com.yichen.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yichen.project.model.entity.Chart;

import java.util.List;
import java.util.Map;

/**
 * @Entity generator.domain.Chart
 */
public interface ChartMapper extends BaseMapper<Chart> {
    /**
     * 声明方法 id和xml里面的id保持一致
     * @param querySql
     * @return
     */
    List<Map<String, Object>> queryChartData(String querySql);
}




