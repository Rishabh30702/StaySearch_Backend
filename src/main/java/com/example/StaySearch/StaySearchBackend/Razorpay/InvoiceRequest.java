package com.example.StaySearch.StaySearchBackend.Razorpay;

public class InvoiceRequest {
    public String orderId;
    public String paymentId;
    public String customerEmail;
    public String customerPhone;
    public String hotelName;
    public long amountInPaise;

    public InvoiceRequest() {
    }

    public InvoiceRequest(String orderId, String paymentId, String customerEmail, String customerPhone, String hotelName, long amountInPaise) {
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.hotelName = hotelName;
        this.amountInPaise = amountInPaise;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
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

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public long getAmountInPaise() {
        return amountInPaise;
    }

    public void setAmountInPaise(long amountInPaise) {
        this.amountInPaise = amountInPaise;
    }
}

