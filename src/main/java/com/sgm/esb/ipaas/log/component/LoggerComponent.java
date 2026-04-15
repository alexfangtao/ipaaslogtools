package com.sgm.esb.ipaas.log.component;

import org.apache.camel.Endpoint;
import org.apache.camel.support.DefaultComponent;

import java.util.Map;

public class LoggerComponent extends DefaultComponent {
    public LoggerComponent() {
    }

    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> params) throws Exception {
        LoggerEndpoint endpoint = new LoggerEndpoint(uri, this);
        this.setProperties(endpoint, params);
        endpoint.setName(remaining);
        return endpoint;
    }
}