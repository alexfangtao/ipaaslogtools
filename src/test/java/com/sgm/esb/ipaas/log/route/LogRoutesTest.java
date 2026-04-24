package com.sgm.esb.ipaas.log.route;

import com.sgm.esb.ipaas.log.config.LoggerConfig;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogRoutesTest {

    @Mock
    private LoggerConfig config;

    private LogRoutes logRoutes;

    @BeforeEach
    void setUp() throws Exception {
        logRoutes = new LogRoutes();
        Field configField = LogRoutes.class.getDeclaredField("config");
        configField.setAccessible(true);
        configField.set(logRoutes, config);
    }

    @Test
    void testRouteLoadedSuccessfully() throws Exception {
        when(config.getHttpPath()).thenReturn("http://localhost:8080/log/save");
        when(config.getConnectTimeout()).thenReturn(1000);
        when(config.getResponseTimeout()).thenReturn(1000);
        when(config.getMaxTotalConnections()).thenReturn(200);
        when(config.getConnectionsPerRoute()).thenReturn(20);

        try (DefaultCamelContext context = new DefaultCamelContext()) {
            context.addRoutes(logRoutes);

            List<RouteDefinition> routeDefinitions = context.getRouteDefinitions();
            assertEquals(1, routeDefinitions.size());

            RouteDefinition route = routeDefinitions.get(0);
            assertNotNull(route);
            assertTrue(route.getEndpointUrl().contains("direct:"));
            assertTrue(route.getEndpointUrl().contains("tools-transaction"));
        }
    }

    @Test
    void testRouteConfigureWithDifferentValues() throws Exception {
        when(config.getHttpPath()).thenReturn("http://10.0.0.1:9000/api/log");
        when(config.getConnectTimeout()).thenReturn(500);
        when(config.getResponseTimeout()).thenReturn(2000);
        when(config.getMaxTotalConnections()).thenReturn(100);
        when(config.getConnectionsPerRoute()).thenReturn(10);

        assertDoesNotThrow(() -> {
            try (DefaultCamelContext context = new DefaultCamelContext()) {
                context.addRoutes(logRoutes);
            }
        });
    }
}

