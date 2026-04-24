package com.sgm.esb.ipaas.log.service;

import com.sgm.esb.ipaas.log.config.LoggerConfig;
import org.apache.camel.CamelContext;
import org.apache.camel.ExtendedCamelContext;
import org.apache.camel.impl.engine.DefaultProducerTemplate;
import org.apache.camel.spi.ExecutorServiceManager;
import org.apache.camel.spi.InternalProcessorFactory;
import org.apache.camel.spi.ThreadPoolProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.concurrent.ThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LoggerInitTest {

    @Mock
    private CamelContext camelContext;

    @Mock
    private LoggerConfig loggerConfig;

    @Mock
    private ExecutorServiceManager executorServiceManager;

    @Mock
    private ThreadPoolExecutor threadPoolExecutor;

    @InjectMocks
    private LoggerInit loggerInit;

    @BeforeEach
    void setUp() {
        ExtendedCamelContext extendedContext = mock(ExtendedCamelContext.class);
        InternalProcessorFactory processorFactory = mock(InternalProcessorFactory.class);

        lenient().when(camelContext.getCamelContextExtension()).thenReturn(extendedContext);
        lenient().when(extendedContext.getContextPlugin(InternalProcessorFactory.class))
                .thenReturn(processorFactory);
        when(camelContext.getExecutorServiceManager()).thenReturn(executorServiceManager);
        when(executorServiceManager.newThreadPool(any(), anyString(), any(ThreadPoolProfile.class)))
                .thenReturn(threadPoolExecutor);
        when(loggerConfig.getPoolSize()).thenReturn(10);
        when(loggerConfig.getMaxPoolSize()).thenReturn(20);
        when(loggerConfig.getMaxQueueSize()).thenReturn(100);
    }

    @Test
    void testInitProducer_Success() {
        when(loggerConfig.getPolicy()).thenReturn(3);

        try (MockedConstruction<DefaultProducerTemplate> mocked = mockConstruction(DefaultProducerTemplate.class)) {
            loggerInit.initProducer();

            assertNotNull(loggerInit.getLogExecutor());
            assertNotNull(loggerInit.getProducerTemplate());
            assertEquals(1, mocked.constructed().size());

            DefaultProducerTemplate template = mocked.constructed().get(0);
            verify(template).start();
        }
    }

    @Test
    void testInitProducer_StartException() {
        when(loggerConfig.getPolicy()).thenReturn(3);

        try (MockedConstruction<DefaultProducerTemplate> mocked = mockConstruction(DefaultProducerTemplate.class,
                (mock, ctx) -> doThrow(new RuntimeException("start failed")).when(mock).start())) {

            RuntimeException ex = assertThrows(RuntimeException.class, () -> loggerInit.initProducer());
            assertEquals("start failed", ex.getMessage());
        }
    }

    @Test
    void testDestroyProducer_Success() {
        when(loggerConfig.getPolicy()).thenReturn(3);

        try (MockedConstruction<DefaultProducerTemplate> mocked = mockConstruction(DefaultProducerTemplate.class)) {
            loggerInit.initProducer();

            DefaultProducerTemplate template = mocked.constructed().get(0);
            loggerInit.destroyProducer();

            verify(template).stop();
        }
    }

    @Test
    void testDestroyProducer_NeverInitialized() {
        assertDoesNotThrow(() -> loggerInit.destroyProducer());
    }

    @Test
    void testPolicyAbort() {
        when(loggerConfig.getPolicy()).thenReturn(1);
        loggerInit.initProducer();
        verify(threadPoolExecutor, never()).setRejectedExecutionHandler(any());
    }

    @Test
    void testPolicyDiscard() {
        when(loggerConfig.getPolicy()).thenReturn(2);
        loggerInit.initProducer();
        verify(threadPoolExecutor).setRejectedExecutionHandler(any(ThreadPoolExecutor.DiscardPolicy.class));
    }

    @Test
    void testPolicyDiscardOldest() {
        when(loggerConfig.getPolicy()).thenReturn(3);
        loggerInit.initProducer();
        verify(threadPoolExecutor).setRejectedExecutionHandler(any(ThreadPoolExecutor.DiscardOldestPolicy.class));
    }

    @Test
    void testPolicyCallerRuns() {
        when(loggerConfig.getPolicy()).thenReturn(4);
        loggerInit.initProducer();
        verify(threadPoolExecutor).setRejectedExecutionHandler(any(ThreadPoolExecutor.CallerRunsPolicy.class));
    }

    @Test
    void testPolicyDefault() {
        when(loggerConfig.getPolicy()).thenReturn(99);
        loggerInit.initProducer();
        verify(threadPoolExecutor).setRejectedExecutionHandler(any(ThreadPoolExecutor.DiscardOldestPolicy.class));
    }

    @Test
    void testThreadPoolProfile() {
        when(loggerConfig.getPolicy()).thenReturn(1);
        when(loggerConfig.getPoolSize()).thenReturn(5);
        when(loggerConfig.getMaxPoolSize()).thenReturn(15);
        when(loggerConfig.getMaxQueueSize()).thenReturn(500);

        loggerInit.initProducer();

        ArgumentCaptor<ThreadPoolProfile> captor = ArgumentCaptor.forClass(ThreadPoolProfile.class);
        verify(executorServiceManager).newThreadPool(eq(loggerInit), eq("logThreadPool"), captor.capture());

        ThreadPoolProfile profile = captor.getValue();
        assertEquals("log-profile", profile.getId());
        assertFalse(profile.isDefaultProfile());
        assertEquals(5, profile.getPoolSize());
        assertEquals(15, profile.getMaxPoolSize());
        assertEquals(500, profile.getMaxQueueSize());
    }
}
