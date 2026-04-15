package com.sgm.esb.ipaas.log;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class GetErrorMSG {
    private static final Logger log = LoggerFactory.getLogger(GetErrorMSG.class);


    public void errorHandler(Exchange exchange) throws Exception {
        Exception ex = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        // Process
        if (Objects.nonNull(ex)) {
            log.error("日志保存接口报错:" + ex.toString());
        }
    }
}
