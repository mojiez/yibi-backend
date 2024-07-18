package com.yichen.project.model.vo;

import lombok.Data;

@Data
public class BiResponse {
    private String genChart;
    private String genResult;
    // 为什么会有chartID这种东西 生成的表的id不是自己产生的吗？
    private Long ChartId;
}
