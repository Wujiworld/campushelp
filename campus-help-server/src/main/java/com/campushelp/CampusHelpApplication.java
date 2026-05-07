package com.campushelp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 校园帮单体应用入口：聚合用户认证、订单履约、门店商品等域。
 * <p>
 * 生产部署：{@code java -jar campus-help-server-*.jar}，配置通过环境变量与 profile 注入。
 */
@SpringBootApplication(scanBasePackages = "com.campushelp")
@MapperScan({
        "com.campushelp.user.mapper",
        "com.campushelp.order.mapper",
        "com.campushelp.product.mapper",
        "com.campushelp.life.mapper"
})
@EnableScheduling
@EnableRabbit
public class CampusHelpApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusHelpApplication.class, args);
    }
}
