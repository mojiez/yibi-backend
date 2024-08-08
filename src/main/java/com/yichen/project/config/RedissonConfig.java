package com.yichen.project.config;

import co.elastic.clients.elasticsearch.nodes.Ingest;
import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissonConfig {
    private Integer database;
    private String host;
    private Integer port;

    // 使用Bean注解的方法 会被Spring容器调用，将其返回值注册为Bean 因此Spring启动时，会自动创建一个RedissonClient对象
    @Bean
    public RedissonClient getRedissonClient() {
        // 1.创建配置对象
        Config config = new Config();
        config.useSingleServer()
                .setDatabase(database)
                .setAddress("redis://"+host+":"+port);

        // 2.创建Redisson实例
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
}
