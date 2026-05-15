package com.finanscepte.settings;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.finanscepte.settings", "com.finanscepte.common"})
public class SettingsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SettingsServiceApplication.class, args);
    }
}
