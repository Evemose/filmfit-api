package com.filmfit.actualization;

import com.filmfit.core.Core;
import org.springframework.boot.SpringApplication;

public class TestActualizationApplication {

    public static void main(String[] args) {
        SpringApplication.from(Core::main).with(TestcontainersConfiguration.class).run(args);
    }

}
