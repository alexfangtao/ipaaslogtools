package com.sgm.esb.ipaas.log.component;


import com.sgm.esb.ipaas.log.config.LoggerConfig;
import com.sgm.esb.ipaas.log.service.LoggerInit;
import org.apache.camel.Component;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.UriPath;
import org.apache.camel.support.DefaultEndpoint;

@UriEndpoint(
        firstVersion = "1.0.0-SNAPSHOT",
        title = "Logger",
        scheme = "ipaas-logger",
        syntax = "ipaas-logger:name",
        producerOnly = true
)
public class LoggerEndpoint extends DefaultEndpoint {

    private LoggerInit loggerInit;

    private LoggerConfig config;

    @UriPath
    @Metadata(
            required = true
    )
    private String name;
    @UriParam(
            defaultValue = "code1"
    )
    private String code;
    @UriParam(
            defaultValue = "NULL"
    )
    private String fromApp;
    @UriParam(
            defaultValue = "NULL"
    )
    private String toApp;

    public LoggerEndpoint(String endpointUri, Component component, LoggerInit loggerInit, LoggerConfig config) {
        super(endpointUri, component);
        this.loggerInit = loggerInit;
        this.config = config;
    }

    public Producer createProducer() throws Exception {
        return new LoggerProducer(this, loggerInit.getProducerTemplate(), config);
    }

    public Consumer createConsumer(Processor processor) throws Exception {
        throw new UnsupportedOperationException("You cannot consume messages with this endpoint: " + this.getEndpointUri());
    }

    public String getName() {
        return this.name;
    }

    public String getCode() {
        return this.code;
    }


    public String getFromApp() {
        return this.fromApp;
    }

    public String getToApp() {
        return this.toApp;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setFromApp(String fromApp) {
        this.fromApp = fromApp;
    }

    public void setToApp(String toApp) {
        this.toApp = toApp;
    }
}
