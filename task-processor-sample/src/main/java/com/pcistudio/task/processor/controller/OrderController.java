package com.pcistudio.task.processor.controller;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class OrderController {
    private MeterRegistry meterRegistry;

    @PostConstruct
    public void verifyRegistry() {
        log.info("Registry={}", meterRegistry.getClass().getCanonicalName());
    }

    private final OrderService orderService;

    public OrderController(OrderService orderService, MeterRegistry meterRegistry) {
        this.orderService = orderService;
        this.meterRegistry = meterRegistry;
    }

    @GetMapping("/create-order")
    public String createOrder() {
        orderService.createOrder();
        return "Order Created!";
    }

}