package com.example.StaySearch.StaySearchBackend.PaymentGateway.CASHFREE;

import org.springframework.http.*;
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

            if (paymentLink == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Cashfree did not return a payment link"));
            }

            return ResponseEntity.ok(Map.of("paymentLink", paymentLink));

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
