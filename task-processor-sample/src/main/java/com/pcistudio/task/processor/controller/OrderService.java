package com.pcistudio.task.processor.controller;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final Counter orderCounter;

    public OrderService(MeterRegistry meterRegistry) {
        // Define a custom Counter metric
        this.orderCounter = Counter.builder("orders_created_total")
                .description("Total number of orders created")
                .tags("status", "success") // Optional tags for filtering
                .register(meterRegistry);
    }

    public void createOrder() {
        // Simulate order creation logic
        System.out.println("Order Created!");

        // Increment the counter
        orderCounter.increment();
    }
}