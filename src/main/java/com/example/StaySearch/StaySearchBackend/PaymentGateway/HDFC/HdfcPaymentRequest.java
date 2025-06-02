package com.example.StaySearch.StaySearchBackend.PaymentGateway.HDFC;


public class HdfcPaymentRequest {
    private String orderId;
    private Double amount;
    private String customerName;
    private String customerEmail;
    private String customerPhone;

    // Getters and Setters


    public HdfcPaymentRequest(String orderId, Double amount, String customerName, String customerEmail, String customerPhone) {
        this.orderId = orderId;
        this.amount = amount;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
    }

    public HdfcPaymentRequest() {
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }
}

