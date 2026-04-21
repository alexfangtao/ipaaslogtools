package com.sgm.esb.ipaas.log.component;

import com.alibaba.fastjson2.JSONObject;
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
        try {
            initialUUID(exchange);
            String code = this.endpoint.getCode();

            LogEntity logEntity = new LogEntity();
            logEntity.setSvcNo(exchange.getProperty("SVCNO", String.class));
            logEntity.setUuId(exchange.getProperty("X-SGM-LOG-ID", String.class));
            String traceId = exchange.getProperty("X-TRACE-ID", String.class);
            logEntity.setTraceId(org.apache.camel.util.ObjectHelper.isEmpty(traceId) ? exchange.getIn().getHeader("traceparent", String.class) : traceId);
            logEntity.setCode(code);
            logEntity.setFromApp(this.endpoint.getFrom());
            logEntity.setToApp(this.endpoint.getTo());
            logEntity.setMsgTs(System.currentTimeMillis());
            // body大小 限制3m
            int limitSize = 3145728;
            String content = extractPartialBody(exchange, limitSize);
            logEntity.setBody(content);
            producerTemplate.asyncSendBody("direct:tools-transaction", JSONObject.toJSONString(logEntity));
        } catch (Exception ex) {
            log.error("[Logger] component error: {}", ex.getMessage());
        }

    }

    public void initialUUID(Exchange exchange) {
        if (ObjectUtils.isEmpty(exchange.getProperty("X-SGM-LOG-ID"))) {
            exchange.setProperty("X-SGM-LOG-ID", UUID.randomUUID().toString());
        }
    }

    private static String extractPartialBody(Exchange exchange, int maxChars) {
        try {
            Object body = exchange.getIn().getBody();

            // 1. String 类型：直接处理
            if (body instanceof String str) {
                return str.length() <= maxChars ? str : str.substring(0, maxChars);
            }

            // 2. StreamCache 类型：优先使用 Camel 原生能力
            if (body instanceof StreamCache cache) {
                cache.reset();  // 确保从头开始

                try (InputStream is = exchange.getContext().getTypeConverter()
                        .convertTo(InputStream.class, exchange, cache)) {

                    String result = readPartialFromStream(is, maxChars);
                    cache.reset();  // 再次重置，保证后续路由可用
                    return result;
                } catch (Exception e) {
                    log.error("读取 StreamCache 失败", e);
                    cache.reset();
                    return "日志读取报文错误";
                }
            }

            // 3. 普通 InputStream：仅处理支持 mark/reset 的
            if (body instanceof InputStream) {
                InputStream is = (InputStream) body;
                if (!is.markSupported()) {
                    log.error("InputStream 不支持 mark/reset，无法安全截取");
                    return "日志读取报文错误";
                }

                int maxBytes = maxChars * 4;  // UTF-8 保守估计
                is.mark(maxBytes + 1);

                try {
                    String result = readPartialFromStream(is, maxChars);
                    is.reset();
                    return result;
                } catch (Exception e) {
                    log.error("读取 InputStream 失败", e);
                    try {
                        is.reset();
                    } catch (IOException ignored) {
                    }
                    return "日志读取报文错误";
                }
            }
        } catch (Exception e) {
            log.error("日志读取截取异常", e);
            return "日志读取报文错误";
        }

        return "不支持读取的报文类型";
    }

    private static String readPartialFromStream(InputStream is, int maxChars) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (Reader reader = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {

            char[] buffer = new char[1024];
            int totalChars = 0;
            int charsRead;

            while ((charsRead = reader.read(buffer)) != -1) {
                if (totalChars + charsRead >= maxChars) {
                    sb.append(buffer, 0, maxChars - totalChars);
                    break;
                }
                sb.append(buffer, 0, charsRead);
                totalChars += charsRead;
            }
        }
        return sb.toString();
    }
}
