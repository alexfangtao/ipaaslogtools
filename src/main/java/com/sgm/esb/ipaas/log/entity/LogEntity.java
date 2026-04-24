package com.sgm.esb.ipaas.log.entity;


import lombok.Data;

import java.io.Serializable;

@Data
public class LogEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 接口编号
     */
    private String svcNo;

    /**
     * 链路ID
     */
    private String uuid;
    /**
     * 业务id
     */
    private String traceId;
    /**
     * 8100、8200、8300、8400、9100
     */
    private String code;

    private String fromApp;

    private String toApp;
    /**
     * 请求参数内容
     */
    private String body;
    /**
     * 日志生成时间
     */
    private Long msgTs;
}
