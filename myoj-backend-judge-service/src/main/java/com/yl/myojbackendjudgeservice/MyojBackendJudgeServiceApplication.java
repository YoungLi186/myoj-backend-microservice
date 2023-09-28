package com.yl.myojbackendjudgeservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

import static com.yl.myojbackendjudgeservice.rabbitmq.InitRabbitMq.doInit;

@SpringBootApplication(exclude = {RedisAutoConfiguration.class})
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@ComponentScan("com.yl")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.yl.myojbackendserviceclient.service"})
public class MyojBackendJudgeServiceApplication {

    public static void main(String[] args) {
        doInit();//启动消息队列
        SpringApplication.run(MyojBackendJudgeServiceApplication.class, args);
    }

}
