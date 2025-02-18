package com.pcistudio.task.procesor;

import static org.junit.jupiter.api.Assertions.*;

class HandlerWritePropertiesTest {

    @org.junit.jupiter.api.Test
    void builder() {
        HandlerWriteProperties handlerWriteProperties = HandlerWriteProperties.builder()
                .tableName("test_table")
                .handlerName("test_handler")
                .encrypt(true)
                .build();
        assertEquals("test_table", handlerWriteProperties.getTableName());
        assertEquals("test_handler", handlerWriteProperties.getHandlerName());
        assertTrue(handlerWriteProperties.isEncrypt());
    }

    @org.junit.jupiter.api.Test
    void testHandlerNotSetUseTableName() {
        HandlerWriteProperties handlerWriteProperties = HandlerWriteProperties.builder()
                .tableName("test_table")
                .encrypt(true)
                .build();
        assertEquals("test_table", handlerWriteProperties.getTableName());
        assertEquals("test_table", handlerWriteProperties.getHandlerName());
        assertTrue(handlerWriteProperties.isEncrypt());
    }

    @org.junit.jupiter.api.Test
    void testTableNameNotSetUseHandlerName() {
        HandlerWriteProperties handlerWriteProperties = HandlerWriteProperties.builder()
                .handlerName("test_handler")
                .encrypt(true)
                .build();
        assertEquals("test_handler", handlerWriteProperties.getTableName());
        assertEquals("test_handler", handlerWriteProperties.getHandlerName());
        assertTrue(handlerWriteProperties.isEncrypt());
    }

    @org.junit.jupiter.api.Test
    void testNonTableNameOrUseHandlerNameSet() {
        HandlerWriteProperties.HandlerWritePropertiesBuilder<?> builder = HandlerWriteProperties.builder()
                .encrypt(true);

        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, builder::build);

        assertEquals("tableName or handlerName must be set", illegalArgumentException.getMessage());
    }

}