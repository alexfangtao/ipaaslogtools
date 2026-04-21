package com.sgm.esb.ipaas.log.component;


import com.sgm.esb.ipaas.log.LoggerInit;
import com.sgm.esb.ipaas.log.SpringContextUtil;
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
        scheme = "logger",
        syntax = "logger:name",
        producerOnly = true
)
public class LoggerEndpoint extends DefaultEndpoint {

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
    private String from;
    @UriParam(
            defaultValue = "NULL"
    )
    private String to;

    public LoggerEndpoint(String endpointUri, Component component) {
        super(endpointUri, component);
    }

    public Producer createProducer() throws Exception {
        return new LoggerProducer(this, SpringContextUtil.getBean(LoggerInit.class).getProducerTemplate());
    }

    public Consumer createConsumer(Processor processor) throws Exception {
        throw new UnsupportedOperationException("You cannot consume messages with this endpoint: " + this.getEndpointUri());
    }

    public boolean isSingleton() {
        return false;
    }

    public String getName() {
        return this.name;
    }

    public String getCode() {
        return this.code;
    }


    public String getFrom() {
        return this.from;
    }

    public String getTo() {
        return this.to;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
