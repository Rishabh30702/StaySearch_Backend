package com.example.StaySearch.StaySearchBackend.PaymentGateway.CASHFREE;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
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
        headers.set("x-api-version", "2022-09-01");
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
                "order_meta", Map.of(
                        "return_url", returnUrl
                )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        String url = "https://sandbox.cashfree.com/pg/orders";

        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Map<String, Object> responseBody = response.getBody();

            // Try to get payment_session_id (most reliable)
            Object sessionIdObj = responseBody.get("payment_session_id");

            // If not found, try inside 'data'
            if (sessionIdObj == null && responseBody.containsKey("data")) {
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                sessionIdObj = (data != null) ? data.get("payment_session_id") : null;
            }

            if (sessionIdObj != null) {
                String sessionId = sessionIdObj.toString();

                // Remove trailing duplicate 'payment' if present
                while (sessionId.endsWith("payment")) {
                    sessionId = sessionId.substring(0, sessionId.length() - "payment".length());
                }

                // Build the payment page URL user should be redirected to
                return "https://sandbox.cashfree.com/pg/payments/" + sessionId;
            }

            throw new RuntimeException("Payment session ID not found in response: " + responseBody);
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
