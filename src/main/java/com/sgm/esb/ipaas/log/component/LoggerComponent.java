package com.sgm.esb.ipaas.log.component;

import com.sgm.esb.ipaas.log.config.LoggerConfig;
import com.sgm.esb.ipaas.log.service.LoggerInit;
import org.apache.camel.Endpoint;
import org.apache.camel.support.DefaultComponent;

import java.util.Map;

public class LoggerComponent extends DefaultComponent {

    private final LoggerInit loggerInit;

    private final LoggerConfig config;

    public LoggerComponent(LoggerInit loggerInit, LoggerConfig config) {
        this.loggerInit = loggerInit;
        this.config = config;
    }

    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> params) throws Exception {
        LoggerEndpoint endpoint = new LoggerEndpoint(uri, this, loggerInit, config);
        this.setProperties(endpoint, params);
        endpoint.setName(remaining);
        return endpoint;
    }
}