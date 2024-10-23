package com.SSMS.company;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@EnableFeignClients(basePackages = {"com.SSMS.account", "com.SSMS.bot"})
@SpringBootApplication
public class CompanyApplication {

    public static void main(String[] args) {
        SpringApplication.run(CompanyApplication.class, args);
    }
}

