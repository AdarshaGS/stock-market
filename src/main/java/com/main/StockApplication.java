package com.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
                "com.investments",
                "com.externalServices",
                "com.users",
                "com.auth",
                "com.savings"
})
@EnableJpaRepositories(basePackages = {
                "com.investments.*.repo",
                "com.investments.*.*.repo",
                "com.investments.*.*.*.repo",
                "com.externalServices.repo",
                "com.users.repo",
                "com.users.*.repo",
                "com.savings.repo"
})
@EntityScan(basePackages = {
                "com.investments.*.data",
                "com.investments.*.*.data",
                "com.investments.*.*.*.data",
                "com.externalServices.data",
                "com.users.data",
                "com.users.*.data",
                "com.savings.data"
})
@EnableScheduling
public class StockApplication {

        public static void main(String[] args) {
                SpringApplication.run(StockApplication.class, args);
        }

}
