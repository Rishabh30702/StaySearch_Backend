package com.example.StaySearch.StaySearchBackend.PaymentGateway.PAYTM;


import com.paytm.pg.merchant.PaytmChecksum;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/paytm")
@CrossOrigin
public class PaytmController {

    @PostMapping("/generateTxnToken")
    public ResponseEntity<Map<String, String>> generateTxnToken(@RequestBody PaymentRequest request) {
        TreeMap<String, String> paytmParams = new TreeMap<>();
        paytmParams.put("MID", PaytmConfig.MID);
        paytmParams.put("WEBSITE", PaytmConfig.WEBSITE);
        paytmParams.put("INDUSTRY_TYPE_ID", PaytmConfig.INDUSTRY_TYPE_ID);
        paytmParams.put("CHANNEL_ID", PaytmConfig.CHANNEL_ID);
        paytmParams.put("ORDER_ID", request.getOrderId());
        paytmParams.put("CUST_ID", request.getCustomerId());
        paytmParams.put("TXN_AMOUNT", request.getAmount());
        paytmParams.put("CALLBACK_URL", PaytmConfig.CALLBACK_URL);

        try {
            String checksum = PaytmChecksum.generateSignature(paytmParams, PaytmConfig.MERCHANT_KEY);
            paytmParams.put("CHECKSUMHASH", checksum);
            return ResponseEntity.ok(paytmParams);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/callback")
    public ResponseEntity<String> paytmCallback(HttpServletRequest request) {
        Map<String, String[]> mapData = request.getParameterMap();
        TreeMap<String, String> parameters = new TreeMap<>();
        String paytmChecksum = "";

        for (Map.Entry<String, String[]> entry : mapData.entrySet()) {
            if ("CHECKSUMHASH".equalsIgnoreCase(entry.getKey())) {
                paytmChecksum = entry.getValue()[0];
            } else {
                parameters.put(entry.getKey(), entry.getValue()[0]);
            }
        }

        try {
            boolean isValidChecksum = PaytmChecksum.verifySignature(parameters, PaytmConfig.MERCHANT_KEY, paytmChecksum);
            if (isValidChecksum && "01".equals(parameters.get("RESPCODE"))) {
                return ResponseEntity.ok("Payment Successful");
            } else {
                return ResponseEntity.status(400).body("Payment Failed: " + parameters.get("RESPMSG"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Checksum validation failed");
        }
    }
}

