package com.finanscepte.goals;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.finanscepte.goals", "com.finanscepte.common"})
public class GoalsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(GoalsServiceApplication.class, args);
    }
}
