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
        public Map<String,String> notes;
        public String customerEmail;
        public String customerContact;
        public boolean autoCapture = true;


        public String key_id;
        public String name;
        public String description;
        public String prefill_name;
        public String prefill_contact;
        public String callback_url;
        public String cancel_url;
    }

    public static class VerifyRequest {
        @NotNull public String razorpay_order_id;
        @NotNull public String razorpay_payment_id;
        @NotNull public String razorpay_signature;
    }
}
