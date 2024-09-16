package com.filmfit.matching;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ContextConfiguration(classes = {TestMatchingApplication.class})
class MatchingApplicationTests {

    @Test
    void contextLoads() {
    }

}
