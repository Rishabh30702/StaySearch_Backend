package com.example.StaySearch.StaySearchBackend.PaymentGateway;

import jakarta.persistence.*;

@Entity
@Table(name = "paymentGateway")
public class PaymentGatewaySettings {
    @Id
    private Long id = 1L; // singleton row
    private String activeGateway; // "STRIPE" or "HDFC"
    private Double amount;

    public PaymentGatewaySettings(Long id, String activeGateway, Double amount) {
        this.id = id;
        this.activeGateway = activeGateway;
        this.amount = amount;
    }

    public PaymentGatewaySettings() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getActiveGateway() {
        return activeGateway;
    }

    public void setActiveGateway(String activeGateway) {
        this.activeGateway = activeGateway;
    }
    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
