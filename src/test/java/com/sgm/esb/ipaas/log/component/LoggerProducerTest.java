package com.sgm.esb.ipaas.log.component;

import com.sgm.esb.ipaas.log.config.LoggerConfig;
import com.sgm.esb.ipaas.log.entity.LogEntity;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.support.MessageHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LoggerProducerTest {

    @Mock
    private LoggerEndpoint endpoint;

    @Mock
    private ProducerTemplate producerTemplate;

    @Mock
    private LoggerConfig config;

    @Mock
    private Exchange exchange;

    @Mock
    private Message message;

    private LoggerProducer producer;

    @BeforeEach
    void setUp() {
        producer = new LoggerProducer(endpoint, producerTemplate, config);
    }

    @Test
    void processShouldBuildAndSendLogEntitySuccessfully() throws Exception {
        // Arrange
        when(endpoint.getCode()).thenReturn("8100");
        when(endpoint.getFrom()).thenReturn("APP_A");
        when(endpoint.getTo()).thenReturn("APP_B");
        when(exchange.getProperty("SVCNO", String.class)).thenReturn("SVC001");
        when(exchange.getProperty("X-SGM-LOG-ID", String.class)).thenReturn("uuid-001");
        when(exchange.getProperty("X-TRACE-ID", String.class)).thenReturn("trace-001");
        when(config.getLimitSize()).thenReturn(100);
        when(exchange.getMessage()).thenReturn(message);

        try (MockedStatic<MessageHelper> helper = mockStatic(MessageHelper.class)) {
            helper.when(() -> MessageHelper.extractBodyAsString(message)).thenReturn("{\"data\":\"test\"}");

            // Act
            producer.process(exchange);

            // Assert
            ArgumentCaptor<LogEntity> captor = ArgumentCaptor.forClass(LogEntity.class);
            verify(producerTemplate).asyncSendBody(eq("direct:tools-transaction"), captor.capture());

            LogEntity entity = captor.getValue();
            assertEquals("SVC001", entity.getSvcNo());
            assertEquals("uuid-001", entity.getUuId());
            assertEquals("trace-001", entity.getTraceId());
            assertEquals("8100", entity.getCode());
            assertEquals("APP_A", entity.getFromApp());
            assertEquals("APP_B", entity.getToApp());
            assertEquals("{\"data\":\"test\"}", entity.getBody());
            assertNotNull(entity.getMsgTs());
        }
    }

    @Test
    void processShouldUseTraceparentHeaderWhenTraceIdIsEmpty() throws Exception {
        when(endpoint.getCode()).thenReturn("8200");
        when(endpoint.getFrom()).thenReturn("FROM");
        when(endpoint.getTo()).thenReturn("TO");
        when(exchange.getProperty("SVCNO", String.class)).thenReturn("SVC002");
        when(exchange.getProperty("X-SGM-LOG-ID", String.class)).thenReturn("uuid-002");
        when(exchange.getProperty("X-TRACE-ID", String.class)).thenReturn(null);
        when(exchange.getIn()).thenReturn(message);
        when(message.getHeader("traceparent", String.class)).thenReturn("parent-trace");
        when(config.getLimitSize()).thenReturn(100);
        when(exchange.getMessage()).thenReturn(message);

        try (MockedStatic<MessageHelper> helper = mockStatic(MessageHelper.class)) {
            helper.when(() -> MessageHelper.extractBodyAsString(message)).thenReturn("body");

            producer.process(exchange);

            ArgumentCaptor<LogEntity> captor = ArgumentCaptor.forClass(LogEntity.class);
            verify(producerTemplate).asyncSendBody(eq("direct:tools-transaction"), captor.capture());
            assertEquals("parent-trace", captor.getValue().getTraceId());
        }
    }

    @Test
    void processShouldTruncateBodyWhenExceedsLimit() throws Exception {
        when(endpoint.getCode()).thenReturn("8300");
        when(endpoint.getFrom()).thenReturn("A");
        when(endpoint.getTo()).thenReturn("B");
        when(exchange.getProperty("SVCNO", String.class)).thenReturn("SVC003");
        when(exchange.getProperty("X-SGM-LOG-ID", String.class)).thenReturn("uuid-003");
        when(exchange.getProperty("X-TRACE-ID", String.class)).thenReturn("trace-003");
        when(config.getLimitSize()).thenReturn(5);
        when(exchange.getMessage()).thenReturn(message);

        try (MockedStatic<MessageHelper> helper = mockStatic(MessageHelper.class)) {
            helper.when(() -> MessageHelper.extractBodyAsString(message)).thenReturn("1234567890");

            producer.process(exchange);

            ArgumentCaptor<LogEntity> captor = ArgumentCaptor.forClass(LogEntity.class);
            verify(producerTemplate).asyncSendBody(eq("direct:tools-transaction"), captor.capture());
            assertEquals("12345", captor.getValue().getBody());
        }
    }

    @Test
    void processShouldHandleNullBody() throws Exception {
        when(endpoint.getCode()).thenReturn("8400");
        when(endpoint.getFrom()).thenReturn("C");
        when(endpoint.getTo()).thenReturn("D");
        when(exchange.getProperty("SVCNO", String.class)).thenReturn("SVC004");
        when(exchange.getProperty("X-SGM-LOG-ID", String.class)).thenReturn("uuid-004");
        when(exchange.getProperty("X-TRACE-ID", String.class)).thenReturn("trace-004");
        when(config.getLimitSize()).thenReturn(100);
        when(exchange.getMessage()).thenReturn(message);

        try (MockedStatic<MessageHelper> helper = mockStatic(MessageHelper.class)) {
            helper.when(() -> MessageHelper.extractBodyAsString(message)).thenReturn(null);

            producer.process(exchange);

            ArgumentCaptor<LogEntity> captor = ArgumentCaptor.forClass(LogEntity.class);
            verify(producerTemplate).asyncSendBody(eq("direct:tools-transaction"), captor.capture());
            assertEquals("", captor.getValue().getBody());
        }
    }

    @Test
    void processShouldHandleExtractBodyException() throws Exception {
        when(endpoint.getCode()).thenReturn("9100");
        when(endpoint.getFrom()).thenReturn("E");
        when(endpoint.getTo()).thenReturn("F");
        when(exchange.getProperty("SVCNO", String.class)).thenReturn("SVC005");
        when(exchange.getProperty("X-SGM-LOG-ID", String.class)).thenReturn("uuid-005");
        when(exchange.getProperty("X-TRACE-ID", String.class)).thenReturn("trace-005");
        when(config.getLimitSize()).thenReturn(100);
        when(exchange.getMessage()).thenReturn(message);

        try (MockedStatic<MessageHelper> helper = mockStatic(MessageHelper.class)) {
            helper.when(() -> MessageHelper.extractBodyAsString(message))
                  .thenThrow(new RuntimeException("parse error"));

            assertDoesNotThrow(() -> producer.process(exchange));

            ArgumentCaptor<LogEntity> captor = ArgumentCaptor.forClass(LogEntity.class);
            verify(producerTemplate).asyncSendBody(eq("direct:tools-transaction"), captor.capture());
            assertEquals("", captor.getValue().getBody());
        }
    }

    @Test
    void processShouldCatchExceptionAndNotThrow() throws Exception {
        when(endpoint.getCode()).thenThrow(new RuntimeException("endpoint error"));

        assertDoesNotThrow(() -> producer.process(exchange));
        verify(producerTemplate, never()).asyncSendBody(anyString(), any());
    }

    @Test
    void initialUUIDShouldSetPropertyWhenEmpty() {
        when(exchange.getProperty("X-SGM-LOG-ID")).thenReturn(null);

        producer.initialUUID(exchange);

        verify(exchange).setProperty(eq("X-SGM-LOG-ID"), anyString());
    }

    @Test
    void initialUUIDShouldNotOverrideExistingProperty() {
        when(exchange.getProperty("X-SGM-LOG-ID")).thenReturn("existing-id");

        producer.initialUUID(exchange);

        verify(exchange, never()).setProperty(anyString(), any());
    }
}

