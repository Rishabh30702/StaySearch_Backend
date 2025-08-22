package com.example.StaySearch.StaySearchBackend.Razorpay;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<InvoiceRequest, Long> {
    List<InvoiceRequest> findByCustomerEmail(String email);
    Optional<InvoiceRequest> findByOrderId(String orderId);
}
