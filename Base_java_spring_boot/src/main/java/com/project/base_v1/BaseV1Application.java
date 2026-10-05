package com.project.base_v1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class BaseV1Application {

    public static void main(String[] args) {
        SpringApplication.run(BaseV1Application.class, args);
    }

}
