package com.sgm.esb.ipaas.log;

import com.sgm.esb.ipaas.log.component.LoggerComponent;
import com.sgm.esb.ipaas.log.config.LoggerConfig;
import com.sgm.esb.ipaas.log.route.LogRoutes;
import com.sgm.esb.ipaas.log.service.LoggerInit;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.spi.CamelContextCustomizer;
import org.apache.camel.spi.StreamCachingStrategy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({LoggerConfig.class})
@Slf4j
public class LogAutoConfiguration {

    @Bean(name = "ipaas-logger")
    @ConditionalOnMissingBean
    public LoggerComponent loggerComponent(LoggerInit loggerInit, LoggerConfig config) {
        return new LoggerComponent(loggerInit, config);
    }

    @Bean
    @ConditionalOnMissingBean
    public LoggerInit loggerInit() {
        return new LoggerInit();
    }

    @Bean
    @ConditionalOnMissingBean
    public LogRoutes logRoutes() {
        return new LogRoutes();
    }

    @Bean
    public CamelContextCustomizer ipaasLogStreamCachingCustomizer(LoggerConfig config) {
        return ctx -> {
            if (!ctx.isStreamCaching()) {
                ctx.setStreamCaching(true);
                log.info("[ipaas-logger] stream caching 已由日志组件启用");
            }

            if (config.isForceMemoryOnlyStreamCaching()) {
                StreamCachingStrategy strategy = ctx.getStreamCachingStrategy();
                strategy.setSpoolEnabled(false);
                log.info("[ipaas-logger] 强制使用纯内存 stream caching");
            } else {
                log.info("[ipaas-logger] 沿用宿主 stream caching 配置（spool 可能落盘）");
            }
        };
    }
}