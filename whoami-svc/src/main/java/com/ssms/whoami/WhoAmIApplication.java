package com.ssms.whoami;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import com.ssms.common.config.SSMSRestConfig;

@Import(value = SSMSRestConfig.class)
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableFeignClients(basePackages = {"com.ssms.company", "com.ssms.account"})
public class WhoAmIApplication {
    public static void main(String[] args) {
        SpringApplication.run(WhoAmIApplication.class, args);
    }
}
