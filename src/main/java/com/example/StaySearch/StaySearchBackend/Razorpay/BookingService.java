package com.example.StaySearch.StaySearchBackend.Razorpay;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookingService {

    @Autowired
    InvoiceService invoiceService;
    public void confirmBooking(String orderId, String paymentId) {
        System.out.println("[BookingService] Confirming booking for order=" + orderId + " payment=" + paymentId);
        // TODO: Save to DB, send email, etc.
    }
    public void sendInvoiceEmail(String orderId, String paymentId, String customerEmail,
                                 String hotelName, long amountInPaise) {
        invoiceService.generateAndSendInvoice(orderId, paymentId, customerEmail, hotelName, amountInPaise);
    }
}