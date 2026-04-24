package com.sgm.esb.ipaas.log.route;

import com.sgm.esb.ipaas.log.config.LoggerConfig;
import com.sgm.esb.ipaas.log.service.GetErrorMSG;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.fastjson.FastjsonDataFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LogRoutes extends RouteBuilder {

    @Autowired
    LoggerConfig config;

    private final FastjsonDataFormat fastjson = new FastjsonDataFormat();

    @Override
    public void configure() throws Exception {

        from("direct:tools-transaction")
                .marshal(fastjson)
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json;charset=UTF-8"))
                .doTry()
                .to(config.getHttpPath() + "?connectTimeout=" + config.getConnectTimeout()
                        + "&responseTimeout=" + config.getResponseTimeout()
                        + "&maxTotalConnections=" + config.getMaxTotalConnections()
                        + "&connectionsPerRoute=" + config.getConnectionsPerRoute())
                .doCatch(Exception.class)
                .bean(GetErrorMSG.class, "errorHandler")
                .end();

    }
}
