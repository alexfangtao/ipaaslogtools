package com.sgm.esb.ipaas.log;

import com.sgm.esb.ipaas.log.component.LoggerComponent;
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
@Component
public class LoggerInit {

    private static final Logger log = LoggerFactory.getLogger(LoggerInit.class);

    @Autowired
    private CamelContext context;

    private static ExecutorService logExecutor;

    private static ProducerTemplate producerTemplate;

    @PostConstruct
    public void initProducer() {
        context.addComponent("logger",new LoggerComponent());
        initLogExecutor();
        initProducerTemplate();
    }

    private void initLogExecutor() {
        ThreadPoolProfile threadPoolProfile = new ThreadPoolProfile();
        threadPoolProfile.setDefaultProfile(true);
        threadPoolProfile.setId("log-profile");
        threadPoolProfile.setPoolSize(20);
        threadPoolProfile.setMaxPoolSize(20);
        threadPoolProfile.setKeepAliveTime(60L);
        threadPoolProfile.setMaxQueueSize(2000);
        threadPoolProfile.setAllowCoreThreadTimeOut(false);
        threadPoolProfile.setRejectedPolicy(ThreadPoolRejectedPolicy.Abort);

        logExecutor = context.getExecutorServiceManager()
                .newThreadPool(this, "logThreadPool", threadPoolProfile);
    }

    private void initProducerTemplate() {
        producerTemplate = new DefaultProducerTemplate(context, logExecutor);
        try {
            producerTemplate.start();
        } catch (Exception e) {
            log.error("fuse-tools ProduceTemplate start fail:{}", e.getMessage());
            throw e;
        }
    }


    @PreDestroy
    public void destroyProducer()  {
        if (producerTemplate != null) {
            try {
                producerTemplate.stop();
            } catch (Exception e) {
                log.error("fuse-tools ProduceTemplate stop fail:{}", e.getMessage());
            }
        }
        if (logExecutor != null) {
            logExecutor.shutdown();
        }
    }

    public static ExecutorService getLogExecutor() {
        return logExecutor;
    }

    public static ProducerTemplate getProducerTemplate() {
        return producerTemplate;
    }
}
