package com.pcistudio.task.processor;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MainTest {

    private static final String BASE_URL = "http://localhost:";
    @LocalServerPort
    private int port;

    @Test
    void test() {

        RestTemplate restTemplate = new RestTemplate();
        String url = BASE_URL + port + "/actuator";
        ResponseEntity<Map> response = restTemplate.
                getForEntity(url, Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().get("_links"));
    }
}