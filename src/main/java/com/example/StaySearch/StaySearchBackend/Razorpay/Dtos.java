package com.example.StaySearch.StaySearchBackend.Razorpay;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public class Dtos {

    public static class CreateOrderRequest {
        @Min(1)
        public long amountInPaise;
        public String currency = "INR";
        public String receipt;
        public Map<String, String> notes;
        public String customerEmail;
        public String customerContact;
        public boolean autoCapture = true;

        // ✅ Additional fields for Razorpay Checkout
        public String name;            // e.g., "L N College"
        public String description;     // e.g., "Admission Fee"
        public String prefill_name;    // e.g., "AMISHA KUMARI"
        public String prefill_contact; // e.g., "9955410789"
        public String callback_url;    // success redirect
        public String cancel_url;      // cancel redirect
    }

    public static class VerifyRequest {
        @NotNull public String razorpay_order_id;
        @NotNull public String razorpay_payment_id;
        @NotNull public String razorpay_signature;
    }
}
