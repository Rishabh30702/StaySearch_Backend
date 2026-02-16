package com.example.StaySearch.StaySearchBackend.Razorpay;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Service
public class RazorpayClient {
    private final RestTemplate rest = new RestTemplate();

    @Value("${RAZORPAY_KEY_ID}")
    private String keyId;

    @Value("${RAZORPAY_KEY_SECRET}")
    private String keySecret;

    private HttpHeaders authHeaders() {
        String creds = keyId + ":" + keySecret;
        String basic = Base64.getEncoder().encodeToString(creds.getBytes(StandardCharsets.UTF_8));
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + basic);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return headers;
    }

    public JsonNode createOrder(long amount, String currency, String receipt, boolean autoCapture, Map<String,String> notes) {
        String url = "https://api.razorpay.com/v1/orders";
        MultiValueMap<String,String> form = new LinkedMultiValueMap<>();
        form.add("amount", String.valueOf(amount));
        form.add("currency", currency == null ? "INR" : currency);
        if (receipt != null) form.add("receipt", receipt);
        form.add("payment_capture", autoCapture ? "1" : "0");
        if (notes != null) {
            for (Map.Entry<String,String> e : notes.entrySet()) {
                form.add("notes[" + e.getKey() + "]", e.getValue());
            }
        }
        ResponseEntity<JsonNode> resp = rest.postForEntity(url, new HttpEntity<>(form, authHeaders()), JsonNode.class);
        return resp.getBody();
    }

    public JsonNode fetchPayment(String paymentId) {
        String url = "https://api.razorpay.com/v1/payments/" + paymentId;
        ResponseEntity<JsonNode> resp = rest.exchange(url, HttpMethod.GET, new HttpEntity<>(authHeaders()), JsonNode.class);
        return resp.getBody();
    }

    public JsonNode capturePayment(String paymentId, long amountInPaise) {
        String url = "https://api.razorpay.com/v1/payments/" + paymentId + "/capture";
        MultiValueMap<String,String> form = new LinkedMultiValueMap<>();
        form.add("amount", String.valueOf(amountInPaise));
        ResponseEntity<JsonNode> resp = rest.postForEntity(url, new HttpEntity<>(form, authHeaders()), JsonNode.class);
        return resp.getBody();
    }
}