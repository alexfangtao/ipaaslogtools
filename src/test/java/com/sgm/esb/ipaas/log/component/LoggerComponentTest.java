package com.sgm.esb.ipaas.log.component;

import com.sgm.esb.ipaas.log.config.LoggerConfig;
import com.sgm.esb.ipaas.log.service.LoggerInit;
import org.apache.camel.Endpoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LoggerComponentTest {

    @Mock
    private LoggerInit loggerInit;

    @Mock
    private LoggerConfig config;

    private LoggerComponent component;

    @BeforeEach
    void setUp() {
        component = new LoggerComponent(loggerInit, config);
    }

    @Test
    void testCreateEndpoint() throws Exception {
        Map<String, Object> params = new HashMap<>();

        // 通过反射调用protected方法
        java.lang.reflect.Method method = LoggerComponent.class.getDeclaredMethod(
                "createEndpoint", String.class, String.class, Map.class);
        method.setAccessible(true);
        Endpoint endpoint = (Endpoint) method.invoke(component, "ipaas-logger:myName", "myName", params);

        assertNotNull(endpoint);
        assertTrue(endpoint instanceof LoggerEndpoint);
        assertEquals("myName", ((LoggerEndpoint) endpoint).getName());
    }
}
