package com.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AppTest {

    @Test
    void testGetMessage() {
        assertEquals(
                "Hello from Jenkins CI/CD!",
                App.getMessage()
        );
    }
}
