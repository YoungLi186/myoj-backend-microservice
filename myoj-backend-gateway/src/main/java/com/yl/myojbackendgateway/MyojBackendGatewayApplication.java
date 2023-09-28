package com.yl.myojbackendgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})//由于网关模块并不会用到数据库，所以禁用了Spring Boot应用程序的数据库自动配置
@EnableDiscoveryClient
public class MyojBackendGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyojBackendGatewayApplication.class, args);
    }

}
