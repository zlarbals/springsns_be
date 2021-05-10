package com.springsns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class SpringsnsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringsnsApplication.class, args);
    }

}
