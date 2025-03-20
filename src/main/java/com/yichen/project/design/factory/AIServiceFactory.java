package com.yichen.project.design.factory;

import com.yichen.project.design.adapter.AIServiceTarget;
import com.yichen.project.design.config.APIConfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author mojie
 * @date 2025/3/20 21:40
 * @description:读取配置文件，指定模型，比如deepseek，根据key找到对应的类全路径，根据反射创建对象，传递APIConfig
 */
public class AIServiceFactory {
    private static Properties factoryMappings = new Properties();

    static {
        // 加载配置文件
        try (InputStream input = AIServiceFactory.class
                .getClassLoader().getResourceAsStream("factories.properties")) {
            factoryMappings.load(input);
        } catch (IOException ex) {
            throw new ExceptionInInitializerError("Failed to load factory mappings");
        }
    }


    // 使用工厂方法模式， 封装适配器的创建过程
    public static AIServiceTarget getAiService(String type, APIConfig config) {
        String factoryClassName = factoryMappings.getProperty(type);
        if (factoryClassName == null) {
            throw new IllegalArgumentException("Unsupported model type: " + type);
        }
        try {
            // 使用反射创建工厂实例
            Class<?> factoryClass = Class.forName(factoryClassName);
            // 使用反射创建工厂示例
//            return (AIServiceFactory) factoryClass.getDeclaredConstructor().newInstance();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to create factory instance for type: " + type, ex);
        }
        return null;
    }
}
