package com.example.StaySearch.StaySearchBackend.PaymentGateway.PAYTM;


public class PaymentRequest {
    private String orderId;
    private String customerId;
    private String amount;

    public PaymentRequest(String orderId, String customerId, String amount) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.amount = amount;
    }

    public PaymentRequest() {
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}