package com.sgm.esb.ipaas.log.component;

import com.alibaba.fastjson.JSON;
import com.sgm.esb.ipaas.log.LogEntity;
import com.sgm.esb.ipaas.log.LoggerInit;
import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultExchange;
import org.apache.camel.support.DefaultProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class LoggerProducer extends DefaultProducer {

    private static final Logger log = LoggerFactory.getLogger(LoggerProducer.class);

    private final LoggerEndpoint endpoint;

    public LoggerProducer(LoggerEndpoint endpoint) {
        super(endpoint);
        this.endpoint = endpoint;
    }

    public void process(Exchange exchange) throws Exception {
        try {
            initialUUID(exchange);
            String code = this.endpoint.getCode();

            LogEntity logEntity = new LogEntity();
            logEntity.setSvcNo(exchange.getProperty("SVCNO", String.class));
            logEntity.setUuId(exchange.getProperty("X-SGM-LOG-ID", String.class));
            logEntity.setTraceId(StringUtils.isEmpty(this.endpoint.getTraceId()) ? exchange.getIn().getHeader("traceparent", String.class) : this.endpoint.getTraceId());
            logEntity.setCode(code);
            logEntity.setFromApp(this.endpoint.getFrom());
            logEntity.setToApp(this.endpoint.getTo());
            String key = this.endpoint.getKey();
            logEntity.setMsgTs(System.currentTimeMillis());
            String content = getContentByKey(exchange, key);
            // body大小 限制3m
            int limitSize = 3145728;
            byte[] bodyBytes = content.getBytes(StandardCharsets.UTF_8);
            if (bodyBytes != null && bodyBytes.length > limitSize) {
                byte[] bytesLimit = new byte[limitSize];
                System.arraycopy(bodyBytes, 0, bytesLimit, 0, limitSize);
                content = new String(bytesLimit, "UTF-8");
            }
            logEntity.setBody(content);

            Exchange tempExchange = new DefaultExchange(exchange.getContext());
            tempExchange.getIn().setBody(JSON.toJSONString(logEntity));
            LoggerInit.getProducerTemplate().asyncSend("direct:fools-transaction", tempExchange);
        } catch (Exception ex) {
            log.error("[Logger] component error: {}", ex.getMessage());
        }

    }

    public void initialUUID(Exchange exchange) {
        if (ObjectUtils.isEmpty(exchange.getProperty("X-SGM-LOG-ID"))) {
            exchange.setProperty("X-SGM-LOG-ID", UUID.randomUUID().toString());
        }
    }

    public String getContentByKey(Exchange exchange, String key) {
        String content = "";

        try {
            if (ObjectUtils.isEmpty(key)) {
                content = (String) exchange.getIn().getBody(String.class);
            } else if (!ObjectUtils.isEmpty(exchange.getProperty(key))) {
                content = (String) exchange.getProperty(key, String.class);
            } else if (!ObjectUtils.isEmpty(exchange.getIn().getHeader(key))) {
                content = (String) exchange.getIn().getHeader(key, String.class);
            }
        } catch (Exception var4) {
            content = var4.getMessage();
        }

        return content;
    }
}
