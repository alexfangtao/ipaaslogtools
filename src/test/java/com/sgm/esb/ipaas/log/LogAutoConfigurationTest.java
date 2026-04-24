package com.sgm.esb.ipaas.log;

import com.sgm.esb.ipaas.log.component.LoggerComponent;
import com.sgm.esb.ipaas.log.config.LoggerConfig;
import com.sgm.esb.ipaas.log.service.LoggerInit;
import org.apache.camel.CamelContext;
import org.apache.camel.spi.CamelContextCustomizer;
import org.apache.camel.spi.StreamCachingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogAutoConfigurationTest {

    @Mock
    private LoggerInit loggerInit;

    @Mock
    private LoggerConfig config;

    @Mock
    private CamelContext camelContext;

    @Mock
    private StreamCachingStrategy streamCachingStrategy;

    private LogAutoConfiguration autoConfiguration;

    @BeforeEach
    void setUp() {
        autoConfiguration = new LogAutoConfiguration();
    }

    @Test
    void testLoggerComponentCreation() {
        LoggerComponent component = autoConfiguration.loggerComponent(loggerInit, config);
        assertNotNull(component);
    }

    @Test
    void testCustomizer_WhenStreamCachingNotEnabled_AndForceMemory() {
        when(camelContext.isStreamCaching()).thenReturn(false);
        when(config.isForceMemoryOnlyStreamCaching()).thenReturn(true);
        when(camelContext.getStreamCachingStrategy()).thenReturn(streamCachingStrategy);

        CamelContextCustomizer customizer = autoConfiguration.ipaasLogStreamCachingCustomizer(config);
        customizer.configure(camelContext);

        verify(camelContext).setStreamCaching(true);
        verify(streamCachingStrategy).setSpoolEnabled(false);
        verify(streamCachingStrategy).setBufferSize(4096);
    }

    @Test
    void testCustomizer_WhenStreamCachingEnabled_AndForceMemory() {
        when(camelContext.isStreamCaching()).thenReturn(true);
        when(config.isForceMemoryOnlyStreamCaching()).thenReturn(true);
        when(camelContext.getStreamCachingStrategy()).thenReturn(streamCachingStrategy);

        CamelContextCustomizer customizer = autoConfiguration.ipaasLogStreamCachingCustomizer(config);
        customizer.configure(camelContext);

        verify(camelContext, never()).setStreamCaching(anyBoolean());
        verify(streamCachingStrategy).setSpoolEnabled(false);
        verify(streamCachingStrategy).setBufferSize(4096);
    }

    @Test
    void testCustomizer_WhenStreamCachingNotEnabled_AndNotForceMemory() {
        when(camelContext.isStreamCaching()).thenReturn(false);
        when(config.isForceMemoryOnlyStreamCaching()).thenReturn(false);

        CamelContextCustomizer customizer = autoConfiguration.ipaasLogStreamCachingCustomizer(config);
        customizer.configure(camelContext);

        verify(camelContext).setStreamCaching(true);
        verify(camelContext, never()).getStreamCachingStrategy();
    }

    @Test
    void testCustomizer_WhenStreamCachingEnabled_AndNotForceMemory() {
        when(camelContext.isStreamCaching()).thenReturn(true);
        when(config.isForceMemoryOnlyStreamCaching()).thenReturn(false);

        CamelContextCustomizer customizer = autoConfiguration.ipaasLogStreamCachingCustomizer(config);
        customizer.configure(camelContext);

        verify(camelContext, never()).setStreamCaching(anyBoolean());
        verify(camelContext, never()).getStreamCachingStrategy();
    }
}
