package com.example.StaySearch.StaySearchBackend.Razorpay;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/payments")
public class PaymentsController {

    private final RazorpayClient rp;
    private final BookingService bookingService;
    private static final Logger log = LoggerFactory.getLogger(PaymentsController.class);

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

//    @PostMapping("/create-order")
//    public ResponseEntity<?> createOrder(@RequestBody @Valid Dtos.CreateOrderRequest req) {
//        JsonNode order = rp.createOrder(req.amountInPaise, Optional.ofNullable(req.currency).orElse("INR"),
//                req.receipt, req.autoCapture, req.notes);
//        return ResponseEntity.ok(Map.of(
//                "orderId", order.get("id").asText(),
//                "amount", order.get("amount").asLong(),
//                "currency", order.get("currency").asText(),
//                "key", keyId
//        ));
//    }

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody @Valid Dtos.CreateOrderRequest req) {
        try {
            log.info("[Razorpay][REQUEST] Create Order: amount={} currency={} receipt={}",
                    req.amountInPaise, req.currency, req.receipt);

            JsonNode order = rp.createOrder(
                    req.amountInPaise,
                    Optional.ofNullable(req.currency).orElse("INR"),
                    req.receipt,
                    req.autoCapture,
                    req.notes
            );

            log.info("[Razorpay][RESPONSE] Order created successfully: {}", order.toPrettyString());

            return ResponseEntity.ok(Map.of(
                    "orderId", order.get("id").asText(),
                    "amount", order.get("amount").asLong(),
                    "currency", order.get("currency").asText(),
                    "key", keyId
            ));
        } catch (Exception e) {
            log.error("[Razorpay][ERROR] Failed to create order: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

//    @PostMapping("/create-payment-link")
//    public ResponseEntity<?> createPaymentLink(@RequestBody @Valid Dtos.CreateOrderRequest req) {
//        JsonNode link = rp.createPaymentLink(
//                req.amountInPaise,
//                Optional.ofNullable(req.currency).orElse("INR"),
//                req.customerEmail,
//                req.customerContact,
//                req.receipt,
//                req.notes
//        );
//
//        return ResponseEntity.ok(Map.of(
//                "paymentLinkId", link.get("id").asText(),
//                "status", link.get("status").asText(),
//                "shortUrl", link.get("short_url").asText()
//        ));
//    }

    @PostMapping("/create-payment")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, String> payload) {
        // Extract fields
        String paymentId = payload.get("razorpay_payment_id");
        String orderId   = payload.get("razorpay_order_id");
        String signature = payload.get("razorpay_signature");

        // --- Step 1: Log the incoming request ---
        log.info("[Razorpay][REQUEST] Verify Payment: orderId={} paymentId={} signature={}",
                orderId, paymentId, signature);

        // --- Step 2: Validate input ---
        if (paymentId == null || orderId == null || signature == null) {
            log.warn("[Razorpay][REQUEST][INVALID] Missing parameters: {}", payload);
            return ResponseEntity.badRequest().body(Map.of("error", "Missing required parameters"));
        }

        try {
            // --- Step 3: Verify signature ---
            String generated = HmacUtil.hmacSha256Hex(orderId + "|" + paymentId, keySecret);
            boolean isValid = generated.equals(signature);

            // --- Step 4: Log the verification result ---
            log.info("[Razorpay][RESPONSE] Signature verification result for order {}: {}", orderId, isValid);

            // --- Step 5: Handle accordingly ---
            if (isValid) {
                bookingService.confirmBooking(orderId, paymentId);  // update your booking table or payment status

                log.info("[Razorpay][SUCCESS] Booking confirmed for Order={} Payment={}", orderId, paymentId);
                return ResponseEntity.ok(Map.of("verified", true, "orderId", orderId, "paymentId", paymentId));
            } else {
                log.error("[Razorpay][FAILURE] Invalid signature for Order={} Payment={}", orderId, paymentId);
                return ResponseEntity.badRequest().body(Map.of("verified", false, "error", "Invalid signature"));
            }

        } catch (Exception e) {
            // --- Step 6: Catch & log any runtime error ---
            log.error("[Razorpay][ERROR] Payment verification failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/check-payment-status/{paymentId}")
    public ResponseEntity<Map<String, Object>> checkPaymentStatus(@PathVariable String paymentId) {
        try {
            JsonNode payment = rp.fetchPayment(paymentId);
            String status = payment.get("status").asText(); // created | authorized | captured | refunded | failed

            Map<String, Object> response = new HashMap<>();
            response.put("status", status);
            response.put("paymentId", paymentId);

            if ("captured".equalsIgnoreCase(status) || "paid".equalsIgnoreCase(status)) {
                String orderId = payment.has("order_id") ? payment.get("order_id").asText() : null;

                bookingService.confirmBooking(orderId, paymentId);
                response.put("verified", true);

                return ResponseEntity.ok(response);
            } else {
                response.put("verified", false);
                return ResponseEntity.ok(response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch payment status");
            errorResponse.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/get-latest-payment")
    public ResponseEntity<?> getLatestPayment(@RequestParam String orderId) {
        try {
            // Use Razorpay Orders API to list payments for this order
            JsonNode payments = rp.fetchPaymentsForOrder(orderId);
            if (payments.size() > 0) {
                JsonNode latestPayment = payments.get(payments.size() - 1);
                return ResponseEntity.ok(Map.of(
                        "paymentId", latestPayment.get("id").asText(),
                        "status", latestPayment.get("status").asText()
                ));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No payments found for this order"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch payment", "details", e.getMessage()));
        }
    }



//    @PostMapping("/verify-payment-link")
//    public ResponseEntity<?> verifyPaymentLink(@RequestBody Map<String, String> req) {
//        String orderId = req.get("orderId");
//
//        if (orderId == null || orderId.isBlank()) {
//            return ResponseEntity.badRequest().body(Map.of("error", "Missing orderId"));
//        }
//
//        try {
//            JsonNode paymentsResponse = rp.fetchPaymentsForOrder(orderId);
//            JsonNode items = paymentsResponse.get("items");
//
//            if (items == null || items.isEmpty()) {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                        .body(Map.of("verified", false, "error", "No payments found for order"));
//            }
//
//            JsonNode latestPayment = items.get(items.size() - 1);
//            String paymentId = latestPayment.get("id").asText();
//            String status = latestPayment.get("status").asText();
//
//            if ("captured".equalsIgnoreCase(status) || "paid".equalsIgnoreCase(status)) {
//                bookingService.confirmBooking(orderId, paymentId);
//                return ResponseEntity.ok(Map.of(
//                        "verified", true,
//                        "paymentId", paymentId,
//                        "status", status
//                ));
//            }
//
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
//                    "verified", false,
//                    "status", status
//            ));
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Map.of("error", "Failed to verify payment", "details", e.getMessage()));
//        }
//    }


//    @PostMapping("/verify-payment-link")
//    public ResponseEntity<?> verifyPaymentLink(@RequestParam Map<String, String> req) {
//        try {
//            String orderId = req.get("razorpay_order_id");
//            String paymentId = req.get("razorpay_payment_id");
//            String signature = req.get("razorpay_signature");
//
//            // 🔹 Log incoming data for debugging
//            log.info("[Razorpay][REQUEST] Verify Payment Link: orderId={} paymentId={} signature={}",
//                    orderId, paymentId, signature);
//
//            // 🔹 Case 1: If Razorpay sent direct verification fields
//            if (orderId != null && paymentId != null && signature != null) {
//                try {
//                    // Generate HMAC SHA256 signature using your keySecret
//                    String generated = HmacUtil.hmacSha256Hex(orderId + "|" + paymentId, keySecret);
//                    boolean isValid = generated.equals(signature);
//
//                    log.info("[Razorpay][RESPONSE] Signature valid: {}", isValid);
//
//                    if (isValid) {
//                        bookingService.confirmBooking(orderId, paymentId);
//                        return ResponseEntity.ok(Map.of(
//                                "verified", true,
//                                "paymentId", paymentId,
//                                "orderId", orderId,
//                                "source", "direct"
//                        ));
//                    } else {
//                        return ResponseEntity.badRequest().body(Map.of(
//                                "verified", false,
//                                "error", "Invalid signature",
//                                "orderId", orderId
//                        ));
//                    }
//                } catch (Exception ex) {
//                    log.error("[Razorpay][ERROR] Signature validation failed: {}", ex.getMessage(), ex);
//                    return ResponseEntity.internalServerError().body(Map.of(
//                            "error", "Signature validation failed",
//                            "details", ex.getMessage()
//                    ));
//                }
//            }
//
//            // 🔹 Case 2: Fallback to your old logic (for backward compatibility)
//            String legacyOrderId = req.get("orderId");
//            if (legacyOrderId == null || legacyOrderId.isBlank()) {
//                return ResponseEntity.badRequest().body(Map.of("error", "Missing orderId"));
//            }
//
//            JsonNode paymentsResponse = rp.fetchPaymentsForOrder(legacyOrderId);
//            JsonNode items = paymentsResponse.get("items");
//
//            if (items == null || items.isEmpty()) {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                        .body(Map.of("verified", false, "error", "No payments found for order"));
//            }
//
//            JsonNode latestPayment = items.get(items.size() - 1);
//            String latestPaymentId = latestPayment.get("id").asText();
//            String status = latestPayment.get("status").asText();
//
//            if ("captured".equalsIgnoreCase(status) || "paid".equalsIgnoreCase(status)) {
//                bookingService.confirmBooking(legacyOrderId, latestPaymentId);
//                return ResponseEntity.ok(Map.of(
//                        "verified", true,
//                        "paymentId", latestPaymentId,
//                        "status", status,
//                        "source", "legacy"
//                ));
//            }
//
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
//                    "verified", false,
//                    "status", status,
//                    "source", "legacy"
//            ));
//
//        } catch (Exception e) {
//            log.error("[Razorpay][ERROR] Verify Payment Link failed: {}", e.getMessage(), e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Map.of("error", "Failed to verify payment", "details", e.getMessage()));
//        }
//    }

    @PostMapping("/verify-payment-link")
    public ResponseEntity<?> verifyPaymentLink(@RequestBody Map<String, String> req) {
        try {
            String orderId = req.get("razorpay_order_id");
            String paymentId = req.get("razorpay_payment_id");
            String signature = req.get("razorpay_signature");

            log.info("[Razorpay][REQUEST] Verify Payment Link: orderId={} paymentId={} signature={}",
                    orderId, paymentId, signature);

            if (orderId != null && paymentId != null && signature != null) {
                String generated = HmacUtil.hmacSha256Hex(orderId + "|" + paymentId, keySecret);
                boolean isValid = generated.equals(signature);
                log.info("[Razorpay][RESPONSE] Signature valid: {}", isValid);

                if (isValid) {
                    bookingService.confirmBooking(orderId, paymentId);
                    return ResponseEntity.ok(Map.of(
                            "verified", true,
                            "paymentId", paymentId,
                            "orderId", orderId,
                            "source", "direct"
                    ));
                } else {
                    return ResponseEntity.badRequest().body(Map.of(
                            "verified", false,
                            "error", "Invalid signature",
                            "orderId", orderId
                    ));
                }
            }

            return ResponseEntity.badRequest().body(Map.of("error", "Missing Razorpay fields"));
        } catch (Exception e) {
            log.error("[Razorpay][ERROR] Verify Payment Link failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to verify payment", "details", e.getMessage()));
        }
    }


//    @RequestMapping(value = "/callback", method = {RequestMethod.GET, RequestMethod.POST})
//    public void paymentCallback(
//            @RequestParam Map<String, String> allParams,
//            HttpServletResponse response) throws IOException {
//
//        String razorpayPaymentId = allParams.get("razorpay_payment_id");
//        String razorpayOrderId = allParams.get("razorpay_order_id");
//        String userData = allParams.get("userData"); // optional
//        String razorpaySignature = allParams.get("razorpay_signature");
//
//        // ✅ Basic validation
//        if (razorpayPaymentId == null || razorpayOrderId == null || razorpaySignature == null) {
//            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required parameters");
//            return;
//        }
//
//        // ✅ Verify signature
//        String payload = razorpayOrderId + "|" + razorpayPaymentId;
//        String expectedSignature = HmacUtil.hmacSha256Hex(payload, keySecret);
//        if (!expectedSignature.equals(razorpaySignature)) {
//            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid signature");
//            return;
//        }
//
//        // ✅ Confirm booking in backend
//        bookingService.confirmBooking(razorpayOrderId, razorpayPaymentId);
//
//        // ✅ Build redirect URL for Angular page
//        String redirectUrl = "http://localhost:4200/payment-success?"
//                + "razorpay_payment_id=" + URLEncoder.encode(razorpayPaymentId, StandardCharsets.UTF_8)
//                + "&razorpay_order_id=" + URLEncoder.encode(razorpayOrderId, StandardCharsets.UTF_8);
//
////        String redirectUrl = "http://testing.valliento.tech/payment-success?"  // later replace localhost to prod URL frontend
////                + "razorpay_payment_id=" + URLEncoder.encode(razorpayPaymentId, StandardCharsets.UTF_8)
////                + "&razorpay_order_id=" + URLEncoder.encode(razorpayOrderId, StandardCharsets.UTF_8);
//
//
//        if (userData != null && !userData.isEmpty()) {
//            redirectUrl += "&userData=" + URLEncoder.encode(userData, StandardCharsets.UTF_8);
//        }
//
//        // ✅ Redirect to Angular frontend
//        response.sendRedirect(redirectUrl);
//    }

    @RequestMapping(value = "/callback", method = {RequestMethod.GET, RequestMethod.POST})
    public void paymentCallback(
            @RequestParam Map<String, String> allParams,
            HttpServletResponse response) throws IOException {

        String razorpayPaymentId = allParams.get("razorpay_payment_id");
        String razorpayOrderId = allParams.get("razorpay_order_id");
        String razorpaySignature = allParams.get("razorpay_signature");

        // ✅ Basic validation
        if (razorpayPaymentId == null || razorpayOrderId == null || razorpaySignature == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required parameters");
            return;
        }

        // ✅ Verify Razorpay signature
        String payload = razorpayOrderId + "|" + razorpayPaymentId;
        String expectedSignature = HmacUtil.hmacSha256Hex(payload, keySecret);
        if (!expectedSignature.equals(razorpaySignature)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid signature");
            return;
        }

        // ✅ Confirm booking in backend
        bookingService.confirmBooking(razorpayOrderId, razorpayPaymentId);

        // ✅ Respond with HTML that shows message + auto closes tab
        response.setContentType("text/html");
        response.getWriter().write("""
        <!DOCTYPE html>
        <html>
        <head><title>Payment Success</title></head>
        <body style="text-align:center; font-family:Arial, sans-serif; margin-top:50px;">
            <h2 style="color:green;">Your payment was successful!</h2>
            <p>You can safely close this tab.</p>
            <script>
                // Try to auto-close after 1s
                setTimeout(() => window.close(), 1000);
            </script>
        </body>
        </html>
    """);
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
                    System.out.println("[Webhook] ✅ Payment success for order=" + orderId);
                    break;
                }

                case "payment_link.paid": {
                    String paymentLinkId = event.get("payload").get("payment_link").get("entity").get("id").asText();
                    String referenceId = event.get("payload").get("payment_link").get("entity").get("reference_id").asText();

                    // Get the first payment from payments array
                    String paymentId = event.get("payload").get("payment_link").get("entity")
                            .get("payments").get(0).asText();

                    bookingService.confirmBooking(referenceId, paymentId);
                    System.out.println("[Webhook] ✅ Payment Link paid: " + paymentLinkId);
                    break;
                }

                case "payment.failed":
                    System.out.println("[Webhook] ❌ Payment failed");
                    break;

                default:
                    System.out.println("[Webhook] ⚠️ Unhandled event type: " + type);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error");
        }

        return ResponseEntity.ok("ok");
    }


//    @PostMapping(value = "/webhook", consumes = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<String> webhook(
//            @RequestHeader("X-Razorpay-Signature") String signature,
//            @RequestBody String body) {
//
//        // 🔐 Verify signature
//        String expected = HmacUtil.hmacSha256Hex(body, webhookSecret);
//        if (!expected.equals(signature)) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid signature");
//        }
//
//        try {
//            ObjectNode event = (ObjectNode) new ObjectMapper().readTree(body);
//            String type = event.get("event").asText();
//
//            switch (type) {
//                case "payment.captured":
//                case "order.paid": {
//                    String orderId = event.get("payload").get("payment").get("entity").get("order_id").asText();
//                    String paymentId = event.get("payload").get("payment").get("entity").get("id").asText();
//                    bookingService.confirmBooking(orderId, paymentId);
//                    System.out.println("[Webhook] ✅ Payment success for order=" + orderId + ", payment=" + paymentId);
//                    break;
//                }
//                case "payment.failed": {
//                    String paymentId = event.get("payload").get("payment").get("entity").get("id").asText();
//                    System.out.println("[Webhook] ❌ Payment failed: " + paymentId);
//                    break;
//                }
//                case "refund.processed": {
//                    String refundId = event.get("payload").get("refund").get("entity").get("id").asText();
//                    System.out.println("[Webhook] 💸 Refund processed: " + refundId);
//                    break;
//                }
//                default:
//                    System.out.println("[Webhook] ⚠️ Unhandled event type: " + type);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error");
//        }
//
//        return ResponseEntity.ok("ok");
//    }

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

    @GetMapping("/invoice/order/{orderId}/download")
    public ResponseEntity<byte[]> downloadInvoiceByOrderId(@PathVariable String orderId) {
        return invoiceRepository.findByOrderId(orderId)
                .map(invoice -> {
                    try {
                        String fileUrl = invoice.getInvoiceUrl();
                        System.out.println("[Download Invoice] Fetching from URL: " + fileUrl);

                        if (fileUrl == null || fileUrl.isBlank()) {
                            return ResponseEntity.status(404).body(new byte[0]);
                        }

                        // 🔽 Fetch file from Cloudinary
                        java.net.URL url = new java.net.URL(fileUrl);
                        try (java.io.InputStream in = url.openStream()) {
                            byte[] pdfBytes = in.readAllBytes();

                            return ResponseEntity.ok()
                                    .header("Content-Disposition", "attachment; filename=Invoice-" + invoice.getOrderId() + ".pdf")
                                    .header("Content-Type", "application/pdf")
                                    .body(pdfBytes);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return ResponseEntity.status(500).body(new byte[0]);
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }


}