package com.campushelp.life;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.campushelp")
@MapperScan({
        "com.campushelp.life.mapper"
})
@EnableRabbit
@EnableFeignClients(basePackages = "com.campushelp.life.client")
public class LifeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LifeServiceApplication.class, args);
    }
}
