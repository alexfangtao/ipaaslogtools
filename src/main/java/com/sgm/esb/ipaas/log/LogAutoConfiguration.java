package com.sgm.esb.ipaas.log;

import com.sgm.esb.ipaas.log.component.LoggerComponent;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.spi.CamelContextCustomizer;
import org.apache.camel.spi.StreamCachingStrategy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({LoggerConfig.class})
@ComponentScan({"com.sgm.esb.ipaas.log"})
@Slf4j
public class LogAutoConfiguration {

    @Bean(name = "ipaas-logger")
    @ConditionalOnMissingBean
    public LoggerComponent loggerComponent(LoggerInit loggerInit, LoggerConfig config) {
        return new LoggerComponent(loggerInit, config);
    }

    @Bean
    public CamelContextCustomizer ipaasLogStreamCachingCustomizer(LoggerConfig config) {
        return ctx -> {
            if (!ctx.isStreamCaching()) {
                ctx.setStreamCaching(true);
                log.info("[ipaas-log] stream caching 已由日志组件启用");
            }

            if (config.isForceMemoryOnlyStreamCaching()) {
                StreamCachingStrategy strategy = ctx.getStreamCachingStrategy();
                strategy.setSpoolEnabled(false);
                strategy.setBufferSize(4096);
                log.info("[ipaas-log] 强制使用纯内存 stream caching");
            } else {
                log.info("[ipaas-log] 沿用宿主 stream caching 配置（spool 可能落盘）");
            }
        };
    }
}