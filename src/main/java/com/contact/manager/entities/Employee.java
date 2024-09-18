package com.contact.manager.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@DiscriminatorValue("EMPLOYEE")
public class Employee extends Contact {

    private String title;

    @Column(nullable = false)
    private BigDecimal salary;

    @Column(nullable = false)
    private PaymentFrequency paymentFrequency;

    @Column(nullable = false)
    private LocalDate startedDate;

    @Column
    private LocalDate endDate;

    // Getters and setters
    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public LocalDate getStartedDate() {
        return startedDate;
    }

    public void setStartedDate(LocalDate startedDate) {
        this.startedDate = startedDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public PaymentFrequency getPaymentFrequency() {
        return paymentFrequency;
    }

    public Employee setPaymentFrequency(PaymentFrequency paymentFrequency) {
        this.paymentFrequency = paymentFrequency;
        return this;
    }
}