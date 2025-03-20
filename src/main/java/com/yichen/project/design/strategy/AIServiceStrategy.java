package com.yichen.project.design.strategy;

import com.yichen.project.design.adapter.AIServiceTarget;

/**
 * @author mojie
 * @date 2025/3/20 22:07
 * @description:
 */
public interface AIServiceStrategy {
    AIServiceTarget createService();
}
