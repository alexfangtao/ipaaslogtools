package com.sgm.esb.ipaas.log.config;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LoggerConfigTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void testDefaultValues() {
        LoggerConfig config = new LoggerConfig();

        assertEquals(1000, config.getConnectTimeout());
        assertEquals(1000, config.getResponseTimeout());
        assertEquals(20, config.getPoolSize());
        assertEquals(20, config.getMaxPoolSize());
        assertEquals(2000, config.getMaxQueueSize());
        assertEquals(3, config.getPolicy());
        assertEquals(200, config.getMaxTotalConnections());
        assertEquals(20, config.getConnectionsPerRoute());
        assertEquals(3145728, config.getLimitSize());
        assertTrue(config.isForceMemoryOnlyStreamCaching());
    }

    @Test
    void testValidation_FailWhenHttpPathBlank() {
        LoggerConfig config = new LoggerConfig();
        config.setHttpPath("");

        Set<ConstraintViolation<LoggerConfig>> violations = validator.validate(config);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("ipaas.logger.httpPath")));
    }

    @Test
    void testValidation_SuccessWhenHttpPathPresent() {
        LoggerConfig config = new LoggerConfig();
        config.setHttpPath("http://localhost:8080/log");

        Set<ConstraintViolation<LoggerConfig>> violations = validator.validate(config);
        assertTrue(violations.isEmpty());
    }
}

