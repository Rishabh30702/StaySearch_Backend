package com.example.StaySearch.StaySearchBackend.PaymentGateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment-gateway")
@CrossOrigin(origins = "*") // Update CORS policy as needed
public class PaymentGatewaySettingsController {

    @Autowired
    private PaymentGatewaySettingsService service;

    @GetMapping("/active")
    public ResponseEntity<String> getActiveGateway() {
        return ResponseEntity.ok(service.getActiveGateway());
    }

    @PutMapping("/admin/update")
    public ResponseEntity<Void> updateGateway(@RequestBody String gateway) {
        service.setActiveGateway(gateway);
        return ResponseEntity.ok().build();
    }

    // --- New for Amount ---
    @GetMapping("/amount")
    public ResponseEntity<Double> getAmount() {
        return ResponseEntity.ok(service.getAmount());
    }

    @PutMapping("/admin/update-amount")
    public ResponseEntity<Void> updateAmount(@RequestBody Double amount) {
        service.setAmount(amount);
        return ResponseEntity.ok().build();
    }
}

