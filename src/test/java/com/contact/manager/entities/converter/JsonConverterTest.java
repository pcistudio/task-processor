// src/test/java/com/contact/manager/entities/converter/JsonConverterTest.java
package com.contact.manager.entities.converter;

import com.contact.manager.entities.Address;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class JsonConverterTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static class AddressJsonConverter extends JsonConverter<Address> {}

    private final AddressJsonConverter converter = new AddressJsonConverter();

    @Test
    void testConvertToDatabaseColumn() throws JsonProcessingException {
        Address address = new Address();
        address.setStreet("123 Main St");
        address.setCity("Springfield");
        address.setState("IL");
        address.setZipCode("62701");
        String json = converter.convertToDatabaseColumn(address);
        assertEquals(objectMapper.writeValueAsString(address), json);
    }

    @Test
    void testConvertToEntityAttribute()  {
        String json = "{\"street\":\"123 Main St\",\"city\":\"Springfield\",\"state\":\"IL\",\"zipCode\":\"62701\"}";
        Address address = converter.convertToEntityAttribute(json);
        assertEquals("123 Main St", address.getStreet());
        assertEquals("Springfield", address.getCity());
        assertEquals("IL", address.getState());
        assertEquals("62701", address.getZipCode());
    }
}