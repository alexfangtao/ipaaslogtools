package com.sgm.esb.ipaas.log;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({LoggerConfig.class})
@ComponentScan({"com.sgm.esb.ipaas.log"})
public class LogAutoConfiguration {

}