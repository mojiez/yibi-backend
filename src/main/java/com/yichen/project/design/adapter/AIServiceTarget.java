package com.yichen.project.design.adapter;

/**
 * @author mojie
 * @date 2025/3/20 20:13
 * @description: AI服务接口（适配器模式的目标接口） Target接口 定义客户要用的规范接口
 */
public interface AIServiceTarget {
    String generateChart(String prompt);

    String generateConversation(String prompt);
}
