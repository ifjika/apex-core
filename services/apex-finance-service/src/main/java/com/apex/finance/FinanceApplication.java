package com.apex.finance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.apex.common", "com.apex.finance"})
@EntityScan(basePackages = {"com.apex.common", "com.apex.finance.domain"})
@EnableJpaRepositories(basePackages = {"com.apex.common", "com.apex.finance.repository"})
public class FinanceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinanceApplication.class, args);
    }
}

