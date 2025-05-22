package com.example.StaySearch.StaySearchBackend.PaymentGateway;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "*") // Configure as needed for frontend access
public class PaymentController {

    @Autowired
    private StripeService stripeService;

    @PostMapping("/create-payment-intent")
    public ResponseEntity<Map<String, String>> createPaymentIntent(@RequestBody PaymentRequest request) {
        try {
            String clientSecret = stripeService.createPaymentIntent(request.getAmount()); // only pass amount
            return ResponseEntity.ok(Collections.singletonMap("clientSecret", clientSecret));
        } catch (StripeException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PostMapping("/create-checkout-session")
    public ResponseEntity<Map<String, String>> createCheckoutSession(@RequestBody Map<String, Object> data) {
        try {
            long amount = ((Number) data.get("amount")).longValue(); // amount in cents/paise
            String currency = (String) data.get("currency");

            SessionCreateParams params =
                    SessionCreateParams.builder()
                            .setMode(SessionCreateParams.Mode.PAYMENT)
                            .setSuccessUrl("http://localhost:4200/payment-success")
                            .setCancelUrl("http://localhost:4200/payment-cancel")
                            .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                            // no .addPaymentMethodType("upi") here, only enum-supported types
                            .addLineItem(
                                    SessionCreateParams.LineItem.builder()
                                            .setQuantity(1L)
                                            .setPriceData(
                                                    SessionCreateParams.LineItem.PriceData.builder()
                                                            .setCurrency(currency)
                                                            .setUnitAmount(amount)
                                                            .setProductData(
                                                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                            .setName("Hotel Registration Payment")
                                                                            .build())
                                                            .build())
                                            .build())
                            .build();

            Session session = Session.create(params);
            return ResponseEntity.ok(Collections.singletonMap("id", session.getId()));

        } catch (StripeException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Collections.singletonMap("error", e.getMessage()));
        }
    }


}