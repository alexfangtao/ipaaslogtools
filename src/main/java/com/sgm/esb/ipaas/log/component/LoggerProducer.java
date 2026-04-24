package com.sgm.esb.ipaas.log.component;

import com.sgm.esb.ipaas.log.LogConstant;
import com.sgm.esb.ipaas.log.entity.LogEntity;
import com.sgm.esb.ipaas.log.config.LoggerConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.support.DefaultProducer;
import org.apache.camel.support.MessageHelper;
import org.springframework.util.ObjectUtils;

import java.util.UUID;

@Slf4j
public class LoggerProducer extends DefaultProducer {


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

            logEntity.setSvcNo(exchange.getProperty(LogConstant.svcNo, String.class));
            logEntity.setUuid(exchange.getProperty(LogConstant.logId, String.class));
            String traceId = exchange.getProperty(LogConstant.traceId, String.class);
            logEntity.setTraceId(traceId);
            logEntity.setCode(code);
            logEntity.setFromApp(this.endpoint.getFromApp());
            logEntity.setToApp(this.endpoint.getToApp());
            logEntity.setMsgTs(System.currentTimeMillis());
            String content = extractBody(exchange);
            logEntity.setBody(content);
            producerTemplate.asyncSendBody("direct:tools-transaction", logEntity);
        } catch (Exception ex) {
            log.error("[ipaas-logger] component error: traceId {}|{}" , logEntity.getTraceId(), ex.getMessage());
        }

    }

    public void initialUUID(Exchange exchange) {
        if (ObjectUtils.isEmpty(exchange.getProperty(LogConstant.logId))) {
            exchange.setProperty(LogConstant.logId, UUID.randomUUID().toString());
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
            log.error("[ipaas-logger] body 转换失败: {}", e.getMessage());
            return "";
        }
    }
}
