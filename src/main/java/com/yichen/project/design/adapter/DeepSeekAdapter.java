package com.yichen.project.design.adapter;

/**
 * @author mojie
 * @date 2025/3/20 21:17
 * @description:
 */
public class DeepSeekAdapter implements AIServiceTarget{
    private DeepSeekAdaptee deepSeekAdaptee;
    @Override
    public String generateChart(String prompt) {
        return deepSeekAdaptee.doChat("prompt");
    }

    @Override
    public String generateConversation(String prompt) {
        return deepSeekAdaptee.doChat("prompt");
    }
}
