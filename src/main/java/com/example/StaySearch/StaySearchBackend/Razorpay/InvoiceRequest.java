package com.example.StaySearch.StaySearchBackend.Razorpay;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
public class InvoiceRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderId;
    private String paymentId;
    private String customerEmail;
    private String customerPhone;
    private String hotelName;
    private Long amountInPaise;

    private String invoiceUrl;   // ✅ Cloudinary URL
    private LocalDateTime createdAt = LocalDateTime.now();

    public InvoiceRequest() {
    }

    public InvoiceRequest(Long id, String orderId, String paymentId, String customerEmail, String customerPhone, String hotelName, Long amountInPaise, String invoiceUrl, LocalDateTime createdAt) {
        this.id = id;
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.hotelName = hotelName;
        this.amountInPaise = amountInPaise;
        this.invoiceUrl = invoiceUrl;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getAmountInPaise() {
        return amountInPaise;
    }

    public void setAmountInPaise(Long amountInPaise) {
        this.amountInPaise = amountInPaise;
    }

    public String getInvoiceUrl() {
        return invoiceUrl;
    }

    public void setInvoiceUrl(String invoiceUrl) {
        this.invoiceUrl = invoiceUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

