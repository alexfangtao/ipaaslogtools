package com.sgm.esb.ipaas.log.component;

import com.sgm.esb.ipaas.log.LogEntity;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.StreamCache;
import org.apache.camel.support.DefaultProducer;
import org.apache.camel.support.MessageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class LoggerProducer extends DefaultProducer {

    private static final Logger log = LoggerFactory.getLogger(LoggerProducer.class);

    private final LoggerEndpoint endpoint;

    private ProducerTemplate producerTemplate;

    public LoggerProducer(LoggerEndpoint endpoint, ProducerTemplate producerTemplate) {
        super(endpoint);
        this.endpoint = endpoint;
        this.producerTemplate = producerTemplate;
    }

    public void process(Exchange exchange) throws Exception {
        LogEntity logEntity = new LogEntity();
        try {
            initialUUID(exchange);
            String code = this.endpoint.getCode();

            logEntity.setSvcNo(exchange.getProperty("SVCNO", String.class));
            logEntity.setUuId(exchange.getProperty("X-SGM-LOG-ID", String.class));
            String traceId = exchange.getProperty("X-TRACE-ID", String.class);
            logEntity.setTraceId(org.apache.camel.util.ObjectHelper.isEmpty(traceId) ? exchange.getIn().getHeader("traceparent", String.class) : traceId);
            logEntity.setCode(code);
            logEntity.setFromApp(this.endpoint.getFrom());
            logEntity.setToApp(this.endpoint.getTo());
            logEntity.setMsgTs(System.currentTimeMillis());
            String content = extractBody(exchange);
            logEntity.setBody(content);
            producerTemplate.asyncSendBody("direct:tools-transaction", logEntity);
        } catch (Exception ex) {
            log.error("[Logger] component error: traceId {}|{}" , logEntity.getTraceId(), ex.getMessage());
        }

    }

    public void initialUUID(Exchange exchange) {
        if (ObjectUtils.isEmpty(exchange.getProperty("X-SGM-LOG-ID"))) {
            exchange.setProperty("X-SGM-LOG-ID", UUID.randomUUID().toString());
        }
    }

    private static String extractBody(Exchange exchange) {
        int limitSize = 3145728;
        try {
            String body = MessageHelper.extractBodyAsString(exchange.getMessage());
            if (body == null) {
                return "";
            }
            return body.length() <= limitSize ? body : body.substring(0, limitSize);
        } catch (Exception e) {
            log.warn("[Logger] body 转换失败: {}", e.getMessage());
            return "";
        }
    }
}
