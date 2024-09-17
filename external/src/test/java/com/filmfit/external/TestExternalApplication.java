package com.filmfit.external;

import org.springframework.boot.SpringApplication;

public class TestExternalApplication {

    public static void main(String[] args) {
        SpringApplication.from(External::main).with(TestcontainersConfiguration.class).run(args);
    }

}
