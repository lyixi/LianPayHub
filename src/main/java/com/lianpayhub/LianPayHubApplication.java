package com.lianpayhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LianPayHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(LianPayHubApplication.class, args);
    }
}
