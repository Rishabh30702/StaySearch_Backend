package com.example.StaySearch.StaySearchBackend.PaymentGateway.CASHFREE;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class CashfreeController {

    private final CashfreeService cashfreeService;

    public CashfreeController(CashfreeService cashfreeService) {
        this.cashfreeService = cashfreeService;
    }

    @PostMapping("/cashfree/session")
    public ResponseEntity<?> createSession(@RequestBody Map<String, Object> requestData) {
        try {
            String orderId = (String) requestData.get("orderId");
            double amount = Double.parseDouble(requestData.get("amount").toString());
            String currency = "INR";
            String returnUrl = (String) requestData.get("returnUrl");

            String paymentLink = cashfreeService.createPaymentSession(orderId, amount, currency, returnUrl);
            return ResponseEntity.ok(Map.of("paymentLink", paymentLink));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
