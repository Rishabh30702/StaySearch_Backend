package com.example.StaySearch.StaySearchBackend.PaymentGateway;

import jakarta.persistence.*;

@Entity
@Table(name = "paymentGateway")
public class PaymentGatewaySettings {
    @Id
    private Long id = 1L; // singleton row
    private String activeGateway; // "STRIPE" or "HDFC"

    public PaymentGatewaySettings(Long id, String activeGateway) {
        this.id = id;
        this.activeGateway = activeGateway;
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
}
