package com.filmfit.core;

import org.springframework.boot.SpringApplication;

public class TestCoreApplication {

    public static void main(String[] args) {
        SpringApplication.from(Core::main).with(TestcontainersConfiguration.class).run(args);
    }

}
