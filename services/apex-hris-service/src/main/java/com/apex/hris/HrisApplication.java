package com.apex.hris;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.apex.common", "com.apex.hris"})
@EntityScan(basePackages = {"com.apex.common", "com.apex.hris.domain"})
@EnableJpaRepositories(basePackages = {"com.apex.common", "com.apex.hris.repository"})
public class HrisApplication {

    public static void main(String[] args) {
        SpringApplication.run(HrisApplication.class, args);
    }
}

