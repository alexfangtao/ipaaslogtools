package com.sgm.esb.ipaas.log.service;

import org.apache.camel.Exchange;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetErrorMSGTest {

    @Mock
    private Exchange exchange;

    private final GetErrorMSG handler = new GetErrorMSG();

    @Test
    void testErrorHandler_WithException() throws Exception {
        Exception ex = new RuntimeException("connection timeout");
        when(exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class)).thenReturn(ex);

        handler.errorHandler(exchange);

        // 验证能正常执行且不抛异常即可；如有日志 appender 断言可进一步校验
    }

    @Test
    void testErrorHandler_WithoutException() throws Exception {
        when(exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class)).thenReturn(null);

        handler.errorHandler(exchange);

        verify(exchange, never()).setException(any());
    }
}
