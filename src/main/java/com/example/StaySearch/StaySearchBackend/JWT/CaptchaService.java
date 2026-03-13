package com.example.StaySearch.StaySearchBackend.JWT;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class CaptchaService {

    @Value("${cloudflare.turnstile.secret-key}")
    private String secretKey;

    @Value("${cloudflare.turnstile.verify-url}")
    private String verifyUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean verify(String token, String ip) {
        // If no token is provided, fail immediately
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        try {
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("secret", secretKey);
            requestBody.put("response", token);
            requestBody.put("remoteip", ip);

            // Cloudflare returns a Map: { "success": true/false, "error-codes": [...] }
            Map<String, Object> response = restTemplate.postForObject(verifyUrl, requestBody, Map.class);

            return response != null && (Boolean) response.get("success");
        } catch (Exception e) {
            // Log the exception in a real app. Default to false for security.
            return false;
        }
    }
}
