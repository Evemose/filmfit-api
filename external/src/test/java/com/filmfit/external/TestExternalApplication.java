package com.filmfit.external;

import org.springframework.boot.SpringApplication;

public class TestExternalApplication {

    public static void main(String[] args) {
        SpringApplication.from(ExternalApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
