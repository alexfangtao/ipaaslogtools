package com.sgm.esb.ipaas.log.component;

import com.sgm.esb.ipaas.log.config.LoggerConfig;
import com.sgm.esb.ipaas.log.service.LoggerInit;
import org.apache.camel.Producer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LoggerEndpointTest {

    @Mock
    private LoggerInit loggerInit;

    @Mock
    private LoggerConfig config;

    private LoggerEndpoint endpoint;

    @BeforeEach
    void setUp() {
        endpoint = new LoggerEndpoint("ipaas-logger:test", null, loggerInit, config);
    }

    @Test
    void testCreateProducer() throws Exception {
        Producer producer = endpoint.createProducer();
        assertNotNull(producer);
        assertTrue(producer instanceof LoggerProducer);
    }

    @Test
    void testCreateConsumer_ShouldThrow() {
        UnsupportedOperationException ex = assertThrows(
                UnsupportedOperationException.class,
                () -> endpoint.createConsumer(null)
        );
        assertTrue(ex.getMessage().contains("cannot consume messages"));
    }

    @Test
    void testProperties() {
        endpoint.setName("myLogger");
        endpoint.setCode("8200");
        endpoint.setFrom("FROM");
        endpoint.setTo("TO");

        assertEquals("myLogger", endpoint.getName());
        assertEquals("8200", endpoint.getCode());
        assertEquals("FROM", endpoint.getFrom());
        assertEquals("TO", endpoint.getTo());
    }
}
