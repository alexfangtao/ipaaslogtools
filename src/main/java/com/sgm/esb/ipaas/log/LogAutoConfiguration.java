package com.sgm.esb.ipaas.log;

import com.sgm.esb.ipaas.log.component.LoggerComponent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({LoggerConfig.class})
@ComponentScan({"com.sgm.esb.ipaas.log"})
public class LogAutoConfiguration {

    @Bean(name = "logger")
    @ConditionalOnMissingBean
    public LoggerComponent loggerComponent() {
        return new LoggerComponent();
    }
}