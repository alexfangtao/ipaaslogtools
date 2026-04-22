package com.sgm.esb.ipaas.log;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.engine.DefaultProducerTemplate;
import org.apache.camel.spi.ThreadPoolProfile;
import org.apache.camel.util.concurrent.ThreadPoolRejectedPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

@Component
public class LoggerInit {

    private static final Logger log = LoggerFactory.getLogger(LoggerInit.class);

    @Autowired
    private CamelContext context;

    @Autowired
    private LoggerConfig config;

    private ExecutorService logExecutor;

    private ProducerTemplate producerTemplate;

    @PostConstruct
    public void initProducer() {
        initLogExecutor();
        initProducerTemplate();
    }

    private void initLogExecutor() {
        ThreadPoolProfile threadPoolProfile = new ThreadPoolProfile();
        threadPoolProfile.setDefaultProfile(false);
        threadPoolProfile.setId("log-profile");
        threadPoolProfile.setPoolSize(config.getPoolSize());
        threadPoolProfile.setMaxPoolSize(config.getMaxPoolSize());
        threadPoolProfile.setKeepAliveTime(60L);
        threadPoolProfile.setMaxQueueSize(config.getMaxQueueSize());
        threadPoolProfile.setAllowCoreThreadTimeOut(false);
        threadPoolProfile.setRejectedPolicy(ThreadPoolRejectedPolicy.Abort);

        ExecutorService executor = context.getExecutorServiceManager()
                .newThreadPool(this, "logThreadPool", threadPoolProfile);

        if (executor instanceof ThreadPoolExecutor tpe) {
            tpe.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
        }

        logExecutor = executor;
    }

    private void initProducerTemplate() {
        producerTemplate = new DefaultProducerTemplate(context, logExecutor);
        try {
            producerTemplate.start();
        } catch (Exception e) {
            log.error("iPaasLogTools ProduceTemplate start fail:{}", e.getMessage());
            throw e;
        }
    }


    @PreDestroy
    public void destroyProducer()  {
        if (producerTemplate != null) {
            try {
                producerTemplate.stop();
            } catch (Exception e) {
                log.error("iPaasLogTools ProduceTemplate stop fail:{}", e.getMessage());
            }
        }
    }

    public ExecutorService getLogExecutor() {
        return logExecutor;
    }

    public ProducerTemplate getProducerTemplate() {
        return producerTemplate;
    }
}
