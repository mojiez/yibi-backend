package com.yichen.project.bizmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.yichen.project.constant.MqInitContant;

/**
 * 用于创建测试程序用到的交换机和队列（只用在程序启动前执行一次）
 */
public class MqInit {

    public static void main(String[] args) {
        try {
            // 创建连接工厂
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            // 创建连接
            Connection connection = factory.newConnection();
            // 创建通道
            Channel channel = connection.createChannel();

            try {
                // 定义交换机的名称为"code_exchange"
                String EXCHANGE_NAME = MqInitContant.EXCHANGE_NAME.getName();
                // 声明交换机，指定交换机类型为 direct
                channel.exchangeDeclare(EXCHANGE_NAME, "direct");

                // 创建队列，随机分配一个队列名称
                String queueName = MqInitContant.QUEUE_NAME.getName();
                // 声明队列，设置队列持久化、非独占、非自动删除，并传入额外的参数为 null
                channel.queueDeclare(queueName, true, false, false, null);
                // 将队列绑定到交换机，指定路由键为 "my_routingKey"
                channel.queueBind(queueName, EXCHANGE_NAME, MqInitContant.ROUTING_KEY.getName());
            } catch (Exception e) {
                // 异常处理
            } finally {
                channel.close();
                connection.close();
            }

        } catch (Exception e) {
            // 异常处理
        }
    }


}
