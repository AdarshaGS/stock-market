package com.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
                "com.stocks",
                "com.externalServices",
                "com.users"
})
@EnableJpaRepositories(basePackages = { "com.stocks.repo", "com.externalServices.repo", "com.users.repo",
                "com.stocks.*.*.repo", "com.stocks.networth.repo" })
@EntityScan(basePackages = { "com.stocks.data", "com.externalServices.data", "com.users.data", "com.stocks.*.*.data",
                "com.stocks.networth.data" })
@EnableScheduling
public class StockApplication {

        public static void main(String[] args) {
                SpringApplication.run(StockApplication.class, args);
        }

}
