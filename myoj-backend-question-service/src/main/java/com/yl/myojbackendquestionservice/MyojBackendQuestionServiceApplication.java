package com.yl.myojbackendquestionservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@SpringBootApplication(exclude = {RedisAutoConfiguration.class})
@MapperScan("com.yl.myojbackendquestionservice.mapper")
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@ComponentScan("com.yl")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.yl.myojbackendserviceclient.service"})
//@EnableRedisHttpSession
public class MyojBackendQuestionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyojBackendQuestionServiceApplication.class, args);
    }

}
