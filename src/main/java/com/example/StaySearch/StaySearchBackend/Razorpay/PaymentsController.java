package com.example.StaySearch.StaySearchBackend.Razorpay;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
public class PaymentsController {

    private final RazorpayClient rp;
    private final BookingService bookingService;

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private InvoiceRepository invoiceRepository;

    public PaymentsController(RazorpayClient rp, BookingService bookingService) {
        this.rp = rp;
        this.bookingService = bookingService;
    }

    @Value("${RAZORPAY_KEY_ID}")
    String keyId;
    @Value("${RAZORPAY_KEY_SECRET}")
    String keySecret;
    @Value("${RAZORPAY_WEBHOOK_SECRET}")
    String webhookSecret;

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody @Valid Dtos.CreateOrderRequest req) {
        JsonNode order = rp.createOrder(req.amountInPaise, Optional.ofNullable(req.currency).orElse("INR"),
                req.receipt, req.autoCapture, req.notes);
        return ResponseEntity.ok(Map.of(
                "orderId", order.get("id").asText(),
                "amount", order.get("amount").asLong(),
                "currency", order.get("currency").asText(),
                "key", keyId
        ));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody @Valid Dtos.VerifyRequest body) {
        String payload = body.razorpay_order_id + "|" + body.razorpay_payment_id;
        String expected = HmacUtil.hmacSha256Hex(payload, keySecret);
        if (expected.equals(body.razorpay_signature)) {
            bookingService.confirmBooking(body.razorpay_order_id, body.razorpay_payment_id);
            return ResponseEntity.ok(Map.of("verified", true));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("verified", false));
    }

    @PostMapping(value = "/webhook", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> webhook(
            @RequestHeader("X-Razorpay-Signature") String signature,
            @RequestBody String body) {

        // 🔐 Verify signature
        String expected = HmacUtil.hmacSha256Hex(body, webhookSecret);
        if (!expected.equals(signature)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid signature");
        }

        try {
            ObjectNode event = (ObjectNode) new ObjectMapper().readTree(body);
            String type = event.get("event").asText();

            switch (type) {
                case "payment.captured":
                case "order.paid": {
                    String orderId = event.get("payload").get("payment").get("entity").get("order_id").asText();
                    String paymentId = event.get("payload").get("payment").get("entity").get("id").asText();
                    bookingService.confirmBooking(orderId, paymentId);
                    System.out.println("[Webhook] ✅ Payment success for order=" + orderId + ", payment=" + paymentId);
                    break;
                }
                case "payment.failed": {
                    String paymentId = event.get("payload").get("payment").get("entity").get("id").asText();
                    System.out.println("[Webhook] ❌ Payment failed: " + paymentId);
                    break;
                }
                case "refund.processed": {
                    String refundId = event.get("payload").get("refund").get("entity").get("id").asText();
                    System.out.println("[Webhook] 💸 Refund processed: " + refundId);
                    break;
                }
                default:
                    System.out.println("[Webhook] ⚠️ Unhandled event type: " + type);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error");
        }

        return ResponseEntity.ok("ok");
    }

    @PostMapping("/invoice")
    public ResponseEntity<?> createInvoice(@RequestBody @Valid InvoiceRequest req) {
        invoiceService.generateAndSendInvoice(
                req.getOrderId(),
                req.getPaymentId(),
                req.getCustomerEmail(),
                req.getHotelName(),
                req.getAmountInPaise(),
                req.getCustomerPhone()
        );
        return ResponseEntity.ok(Map.of("success", true, "message", "Invoice generated & sent"));
    }

    // ✅ Get all invoices (admin)
    @GetMapping("/invoice")
    public List<InvoiceRequest> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    // ✅ Get invoices for a specific customer
    @GetMapping("/invoice/customer/{email}")
    public List<InvoiceRequest> getInvoicesByCustomer(@PathVariable String email) {
        return invoiceRepository.findByCustomerEmail(email);
    }

    // ✅ Get single invoice by OrderId
    @GetMapping("/invoice/order/{orderId}")
    public ResponseEntity<?> getInvoiceByOrderId(@PathVariable String orderId) {
        return invoiceRepository.findByOrderId(orderId)
                .map(invoice -> ResponseEntity.ok(invoice))
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ Download invoice (redirect to Cloudinary URL)
    @GetMapping("/invoice/{id}/download")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable Long id) {
        return invoiceRepository.findById(id)
                .map(invoice -> {
                    try {
                        // 🔽 Fetch file from Cloudinary
                        java.net.URL url = new java.net.URL(invoice.getInvoiceUrl());
                        try (java.io.InputStream in = url.openStream()) {
                            byte[] pdfBytes = in.readAllBytes();

                            return ResponseEntity.ok()
                                    .header("Content-Disposition", "attachment; filename=Invoice-" + invoice.getOrderId() + ".pdf")
                                    .header("Content-Type", "application/pdf")
                                    .body(pdfBytes);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return ResponseEntity.status(500).body(new byte[0]); // 👈 fixed
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }
}