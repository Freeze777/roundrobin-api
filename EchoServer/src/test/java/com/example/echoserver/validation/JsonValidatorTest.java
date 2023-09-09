package com.example.echoserver.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonValidatorTest {
    @Test
    public void testIsValidJson() {
        String json = "{\"name\":\"John\", \"age\":30, \"car\":null}";
        assertTrue(JsonValidator.isValidJson(json));
    }

    @Test
    public void testIsInvalidJson() {
        String json = "{\"name\":\"John\", \"age\":30, \"car\":null";
        assertFalse(JsonValidator.isValidJson(json));
    }
}
