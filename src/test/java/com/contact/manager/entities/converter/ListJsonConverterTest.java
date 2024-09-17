// src/test/java/com/contact/manager/entities/converter/ListJsonConverterTest.java
package com.contact.manager.entities.converter;

import com.contact.manager.entities.Address;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ListJsonConverterTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static class AddressListJsonConverter extends ListJsonConverter<Address> {}

    private final AddressListJsonConverter converter = new AddressListJsonConverter();

    @Test
    void testConvertToDatabaseColumn() throws JsonProcessingException {
        Address address = new Address();
        address.setStreet("123 Main St");
        address.setCity("Springfield");
        address.setState("IL");
        address.setZipCode("62701");
        Address address2 = new Address();
        address2.setStreet("456 Elm St");
        address2.setCity("Springfield");
        address2.setState("IL");
        address2.setZipCode("62702");

        List<Address> list = Arrays.asList(
                address,
                address2
        );
        String json = converter.convertToDatabaseColumn(list);
        assertEquals(objectMapper.writeValueAsString(list), json);
    }

    @Test
    void testConvertToEntityAttribute() {
        String json = "[{\"street\":\"123 Main St\",\"city\":\"Springfield\",\"state\":\"IL\",\"zipCode\":\"62701\"}," +
                "{\"street\":\"456 Elm St\",\"city\":\"Springfield\",\"state\":\"IL\",\"zipCode\":\"62702\"}]";
        List<Address> list = converter.convertToEntityAttribute(json);
        assertEquals(2, list.size());
        assertEquals("123 Main St", list.get(0).getStreet());
        assertEquals("456 Elm St", list.get(1).getStreet());
    }
}