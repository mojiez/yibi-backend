package com.yichen.project.constant;

public enum MqInitContant {
    EXCHANGE_NAME("code_exchange"),QUEUE_NAME("code_queue"),ROUTING_KEY("xiao_zhang");
    private String name;

    private MqInitContant(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
