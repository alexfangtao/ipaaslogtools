package com.sgm.esb.ipaas.log;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Data
@ConfigurationProperties(
        prefix = "ipaas.logger"
)
public class LoggerConfig {
    /**
     * HTTP路径配置
     */
    private String httpPath = "http://logger.com";
    /**
     * 日志保存接口连接超时配置
     */
    private int connectTimeout = 1000;

    /**
     * 日志保存接口响应超时配置
     */
    private int responseTimeout = 1000;
}
