package com.airops.handling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class AirOpsApplication {
    public static void main(String[] args) {
        SpringApplication.run(AirOpsApplication.class, args);
    }
}
