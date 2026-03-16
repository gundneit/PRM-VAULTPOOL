package com.lavela.pool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LaVelaApplication {

    public static void main(String[] args) {
        SpringApplication.run(LaVelaApplication.class, args);
    }
}
