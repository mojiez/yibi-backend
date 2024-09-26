package com.yichen.project.bizmq;

import com.yichen.project.constant.MqInitContant;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class BiMessageProducerTest {
    @Resource
    private BiMessageProducer biMessageProducer;
    @Test
    public void sendMessage(){
        biMessageProducer.sendMessage(MqInitContant.EXCHANGE_NAME.getName(), MqInitContant.ROUTING_KEY.getName(), "woshinima");
    }
}