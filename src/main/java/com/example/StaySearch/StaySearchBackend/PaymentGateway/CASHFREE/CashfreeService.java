package com.example.StaySearch.StaySearchBackend.PaymentGateway.CASHFREE;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class CashfreeService {

    @Value("${cashfree.client.id}")
    private String clientId;

    @Value("${cashfree.client.secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    public String createPaymentSession(String orderId, double amount, String currency, String returnUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-client-id", clientId);
        headers.set("x-client-secret", clientSecret);
        headers.set("x-api-version", "2022-09-01"); // 🔑 Required version
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "order_id", orderId,
                "order_amount", amount,
                "order_currency", currency,
                "order_note", "Test order",
                "customer_details", Map.of(
                        "customer_id", "12345",
                        "customer_email", "test@example.com",
                        "customer_phone", "9999999999"
                ),
                "order_meta", Map.of( // ✅ return_url should be here
                        "return_url", returnUrl
                )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        String url = "https://sandbox.cashfree.com/pg/orders";

        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return (String) response.getBody().get("payment_link");
        }

        throw new RuntimeException("Cashfree session creation failed");
    }
    public Map<String, Object> getPaymentStatus(String orderId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-client-id", clientId);
        headers.set("x-client-secret", clientSecret);
        headers.set("x-api-version", "2022-09-01");

        HttpEntity<Void> request = new HttpEntity<>(headers);
        String url = "https://sandbox.cashfree.com/pg/orders/" + orderId;

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
        return response.getBody();
    }
}
