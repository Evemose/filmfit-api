package com.filmfit.matching;

import org.springframework.boot.SpringApplication;

public class TestMatchingApplication {

    public static void main(String[] args) {
        SpringApplication.from(MatchingApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
