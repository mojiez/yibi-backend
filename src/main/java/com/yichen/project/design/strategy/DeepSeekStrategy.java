package com.yichen.project.design.strategy;

import com.yichen.project.design.adapter.AIServiceTarget;
import com.yichen.project.design.adapter.DeepSeekAdapter;

/**
 * @author mojie
 * @date 2025/3/20 22:07
 * @description:
 */
public class DeepSeekStrategy implements AIServiceStrategy{

    @Override
    public AIServiceTarget createService() {
        return new DeepSeekAdapter();
    }
}
