package com.nebula.boot4;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.nebula.boot4.mapper")
public class SpringBoot4_Application {

    public static void main(String[] args) {

        SpringApplication.run(SpringBoot4_Application.class, args);
    }

}
