package org.example.shopdemo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 购物商城应用启动类
 * Spring Boot应用程序的入口点
 */
@SpringBootApplication
//开启定时任务
@EnableScheduling
@MapperScan("org.example.shopdemo.mapper")
public class ShopdemoApplication {

    /**
     * 应用程序主方法
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(ShopdemoApplication.class, args);
    }

}
