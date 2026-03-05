package com.claude.learn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        var ctx = SpringApplication.run(DemoApplication.class, args);

        // 临时调试：打印数据库配置
        var env = ctx.getEnvironment();
        System.out.println("DB URL: " + env.getProperty("spring.datasource.url"));
        System.out.println("DB USER: " + env.getProperty("spring.datasource.username"));

    }

}
