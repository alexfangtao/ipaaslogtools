package com.sgm.esb.ipaas.log.service;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.support.MessageHelper;

import java.util.Objects;

@Slf4j
public class GetErrorMSG {


    public void errorHandler(Exchange exchange) throws Exception {
        Exception ex = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        if (Objects.nonNull(ex)) {
            String traceId = "";
            try {
                String s = MessageHelper.extractBodyAsString(exchange.getMessage());
                if (!org.apache.camel.util.ObjectHelper.isEmpty(s)) {
                    traceId = JSONObject.parseObject(s).getString("traceId");
                }
            } catch (Exception e) {
                log.error("[ipaas-logger] traceId 获取失败: {}", e.getMessage());
            }

            log.error("[ipaas-logger] 日志保存接口报错: traceId{}|{}", traceId ,ex.toString());
        }
    }
}
