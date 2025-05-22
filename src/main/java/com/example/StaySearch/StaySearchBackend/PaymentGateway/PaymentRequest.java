package com.example.StaySearch.StaySearchBackend.PaymentGateway;

public class PaymentRequest {
    private int amount;
    private String currency;

    public PaymentRequest(int amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }

    public PaymentRequest() {
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}