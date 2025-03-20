package com.yichen.project.design.adapter;

/**
 * @author mojie
 * @date 2025/3/20 20:21
 * @description: 对应deepseek的adaptee类 适配者类 这个适配者类就是被适配的角色，
 * 使用适配器模式的是为了解决 不同的AI大模型的调用方式不同，比如智谱模型需要创建一个 Client， 这个Client会对智谱模型分发的密钥进行签名认证， 认证成功后才会调用AI接口
 */
public class DeepSeekAdaptee {
    String secrectKey;
    DeepSeekAdaptee(String secrectKey) {
        this.secrectKey = secrectKey;
        // 通过secretKey初始化 Client
    }

    public String doChat(String prompt) {
        // 调用 API 接口
        return "output";
    }
}
