package com.sgm.esb.ipaas.log.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LogEntityTest {

    @Test
    void testGetterAndSetter() {
        LogEntity entity = new LogEntity();
        entity.setSvcNo("SVC001");
        entity.setUuId("uuid-123");
        entity.setTraceId("trace-456");
        entity.setCode("8100");
        entity.setFromApp("APP-A");
        entity.setToApp("APP-B");
        entity.setBody("{\"key\":\"value\"}");
        entity.setMsgTs(1700000000000L);

        assertEquals("SVC001", entity.getSvcNo());
        assertEquals("uuid-123", entity.getUuId());
        assertEquals("trace-456", entity.getTraceId());
        assertEquals("8100", entity.getCode());
        assertEquals("APP-A", entity.getFromApp());
        assertEquals("APP-B", entity.getToApp());
        assertEquals("{\"key\":\"value\"}", entity.getBody());
        assertEquals(1700000000000L, entity.getMsgTs());
    }

    @Test
    void testSerializable() {
        assertTrue(new LogEntity() instanceof java.io.Serializable);
    }
}
