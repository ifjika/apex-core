package com.apex.procurement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.apex.common", "com.apex.procurement"})
@EntityScan(basePackages = {"com.apex.common", "com.apex.procurement.domain"})
@EnableJpaRepositories(basePackages = {"com.apex.common", "com.apex.procurement.repository"})
@EnableScheduling
public class ProcurementApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProcurementApplication.class, args);
    }
}

