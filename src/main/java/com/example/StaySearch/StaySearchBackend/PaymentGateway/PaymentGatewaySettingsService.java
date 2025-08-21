package com.example.StaySearch.StaySearchBackend.PaymentGateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PaymentGatewaySettingsService {

    @Autowired
    private PaymentGatewaySettingsRepository repository;

    public String getActiveGateway() {
        Optional<PaymentGatewaySettings> settings = repository.findById(1L);
        return settings.map(PaymentGatewaySettings::getActiveGateway).orElse("STRIPE");
    }

    public void setActiveGateway(String gateway) {
        PaymentGatewaySettings settings = repository.findById(1L).orElse(new PaymentGatewaySettings());
        settings.setId(1L);
        settings.setActiveGateway(gateway.toUpperCase());
        repository.save(settings);
    }
    // --- New methods for Amount ---
    public Double getAmount() {
        Optional<PaymentGatewaySettings> settings = repository.findById(1L);
        return settings.map(PaymentGatewaySettings::getAmount).orElse(0.0); // default 0
    }

    public void setAmount(Double amount) {
        PaymentGatewaySettings settings = repository.findById(1L).orElse(new PaymentGatewaySettings());
        settings.setId(1L);
        settings.setAmount(amount);
        repository.save(settings);
    }
}

