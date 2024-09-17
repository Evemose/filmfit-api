package com.filmfit.external;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication(scanBasePackages = "com.filmfit")
@ConfigurationPropertiesScan(basePackages = "com.filmfit")
@PropertySource("classpath:external.properties")
public class External {

    public static void main(String[] args) {
        SpringApplication.run(External.class, args);
    }

}
