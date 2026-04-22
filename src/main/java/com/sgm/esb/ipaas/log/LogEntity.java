package com.sgm.esb.ipaas.log;


import java.io.Serializable;

public class LogEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 接口编号
     */
    private String svcNo;

    /**
     * 链路ID
     */
    private String uuId;
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

    public String getSvcNo() {
        return svcNo;
    }

    public void setSvcNo(String svcNo) {
        this.svcNo = svcNo;
    }

    public String getUuId() {
        return uuId;
    }

    public void setUuId(String uuId) {
        this.uuId = uuId;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getFromApp() {
        return fromApp;
    }

    public void setFromApp(String fromApp) {
        this.fromApp = fromApp;
    }

    public String getToApp() {
        return toApp;
    }

    public void setToApp(String toApp) {
        this.toApp = toApp;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Long getMsgTs() {
        return msgTs;
    }

    public void setMsgTs(Long msgTs) {
        this.msgTs = msgTs;
    }
}
