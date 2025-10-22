package com.example.StaySearch.StaySearchBackend.Razorpay;

import java.time.LocalDateTime;
import java.util.Map;

public class RazorpayLog {
    public LocalDateTime timestamp = LocalDateTime.now();
    public String type; // REQUEST or RESPONSE
    public Map<String, Object> data;

    public RazorpayLog(String type, Map<String, Object> data) {
        this.type = type;
        this.data = data;
    }
}
