package com.example.StaySearch.StaySearchBackend.PaymentGateway.CASHFREE;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

@Service
public class CashfreeService {

    @Value("${cashfree.client.id}")
    private String clientId;

    @Value("${cashfree.client.secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    public String createPaymentSession(String orderId, double amount, String currency, String returnUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-client-id", clientId);          // ✅ Ensure this is defined via @Value
        headers.set("x-client-secret", clientSecret);  // ✅ Ensure this is defined via @Value
        headers.set("x-api-version", "2025-01-01");
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "order_id", orderId,
                "order_amount", amount,
                "order_currency", currency,
                "order_note", "Test order from backend",
                "customer_details", Map.of(
                        "customer_id", "CUST_" + UUID.randomUUID(),
                        "customer_email", "test@example.com",
                        "customer_phone", "9999999999"
                ),
                "order_meta", Map.of(
                        "return_url", returnUrl
                ),
                "payment_method", Map.of(
                        "upi", Map.of(
                                "channel", "link"  // ✅ Enable link-based payment flow
                        )
                )
        );


        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        String url = "https://sandbox.cashfree.com/pg/orders";

        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Map<String, Object> responseBody = response.getBody();
            Object sessionIdObj = responseBody.get("payment_session_id");

            if (sessionIdObj == null && responseBody.containsKey("data")) {
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                if (data != null) {
                    sessionIdObj = data.get("payment_session_id");
                }
            }

            if (sessionIdObj != null) {
                return sessionIdObj.toString();
            }

            throw new RuntimeException("payment_session_id not found in response: " + responseBody);
        }

        throw new RuntimeException("Cashfree session creation failed: " + response.getStatusCode());
    }




    public Map<String, Object> getPaymentStatus(String orderId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-client-id", clientId);
        headers.set("x-client-secret", clientSecret);
        headers.set("x-api-version", "2022-09-01");

        HttpEntity<Void> request = new HttpEntity<>(headers);
        String url = "https://sandbox.cashfree.com/pg/orders/" + orderId;

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to fetch payment status: " + response.getStatusCode());
        }
    }
}
