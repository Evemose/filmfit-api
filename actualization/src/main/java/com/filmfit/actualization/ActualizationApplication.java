package com.filmfit.actualization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication(scanBasePackages = "com.filmfit")
@PropertySource("classpath:actualization.properties")
public class ActualizationApplication {

    public static void main(String[] args) {
        SpringApplication.run(ActualizationApplication.class, args);
    }

}
