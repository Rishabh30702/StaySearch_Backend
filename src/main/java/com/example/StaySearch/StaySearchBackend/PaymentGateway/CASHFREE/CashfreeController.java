package com.example.StaySearch.StaySearchBackend.PaymentGateway.CASHFREE;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

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
            String returnUrl = (String) requestData.get("returnUrl");

            String sessionId = cashfreeService.createPaymentSession(orderId, amount, "INR", returnUrl);

            return ResponseEntity.ok(Map.of("paymentSessionId", sessionId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Unknown error occurred"));
        }
    }


    @GetMapping("/cashfree/status/{orderId}")
    public ResponseEntity<?> getPaymentStatus(@PathVariable String orderId) {
        try {
            Map<String, Object> status = cashfreeService.getPaymentStatus(orderId);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Unknown error occurred"));
        }
    }

}
