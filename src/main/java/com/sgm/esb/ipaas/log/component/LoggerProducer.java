package com.sgm.esb.ipaas.log.component;

import com.sgm.esb.ipaas.log.LogEntity;
import com.sgm.esb.ipaas.log.LoggerConfig;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.support.DefaultProducer;
import org.apache.camel.support.MessageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;

import java.util.UUID;

public class LoggerProducer extends DefaultProducer {

    private static final Logger log = LoggerFactory.getLogger(LoggerProducer.class);

    private final LoggerEndpoint endpoint;

    private ProducerTemplate producerTemplate;

    private LoggerConfig config;

    public LoggerProducer(LoggerEndpoint endpoint, ProducerTemplate producerTemplate, LoggerConfig config) {
        super(endpoint);
        this.endpoint = endpoint;
        this.producerTemplate = producerTemplate;
        this.config = config;
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

    private String extractBody(Exchange exchange) {
        try {
            String body = MessageHelper.extractBodyAsString(exchange.getMessage());
            if (body == null) {
                return "";
            }
            return body.length() <= config.getLimitSize() ? body : body.substring(0, config.getLimitSize());
        } catch (Exception e) {
            log.warn("[Logger] body 转换失败: {}", e.getMessage());
            return "";
        }
    }
}
