package com.atguigu.yygh.gateway;

import javafx.application.Application;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
public class ServiceGateWayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceGateWayApplication.class,args);
    }
}
