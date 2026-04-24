package com.sgm.esb.ipaas.log.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;


@Data
@ConfigurationProperties(
        prefix = "ipaas.logger"
)
@Validated
public class LoggerConfig {
    /**
     * HTTP路径配置
     */
    @NotBlank(message = "【启动失败】配置项 'ipaas.logger.httpPath' 是必须的，请在 application中配置！")
    private String httpPath;
    /**
     * 日志保存接口连接超时配置
     */
    private int connectTimeout = 1000;

    /**
     * 日志保存接口响应超时配置
     */
    private int responseTimeout = 1000;

    /**
     * 日志保存接口线程池线程数配置
     */
    private int poolSize = 20;

    /**
     * 日志保存接口线程池最大线程数配置
     */
    private int maxPoolSize = 20;

    /**
     * 日志保存接口线程池最大队列配置
     */
    private int maxQueueSize = 2000;

    /**
     * 日志保存接口线程池拒绝策略配置 1：Abort 2：Discard 3：DiscardOldest 4：CallerRuns
     */
    private int policy = 3;

    /**
     * 日志保存接口http客户端最大连接数配置
     */
    private int maxTotalConnections = 200;

    /**
     * 日志保存接口http客户端单个域名连接数配置
     */
    private int connectionsPerRoute = 20;

    /**
     * 日志body字段压缩字符数配置，默认3M
     */
    private int limitSize = 3145728;

    /**
     * 是否强制使用纯内存 stream caching（关闭 spool 落盘）。
     * 默认 true，适配 K8s 容器化部署。
     * 如接入方业务存在大 body（> 10MB）且希望通过 spool 落盘避免 OOM，
     * 可显式设为 false 保留宿主的 stream caching 配置。
     */
    private boolean forceMemoryOnlyStreamCaching = true;
}
