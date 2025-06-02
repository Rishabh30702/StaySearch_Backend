package com.example.StaySearch.StaySearchBackend.PaymentGateway.HDFC;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/hdfc")
@CrossOrigin(origins = "*") // Configure as needed for frontend access
public class HdfcPaymentController {

    private static final String MERCHANT_ID = "TEST12345";         // Use actual test merchant ID
    private static final String ACCESS_CODE = "AVABCD12345ABCD123"; // From test setup
    private static final String WORKING_KEY = "A1B2C3D4E5F6G7H8I9J0K1L2M3N4O5P6";

    private static final String PAYMENT_URL = "https://test.ccavenue.com/transaction/transaction.do?command=initiateTransaction";
    private static final String REDIRECT_URL = "https://yourdomain.com/api/hdfc/callback";

    @PostMapping("/pay")
    public String initiatePayment(@ModelAttribute HdfcPaymentRequest request, Model model) throws Exception {
        String data = String.format(
                "merchant_id=%s&order_id=%s&currency=INR&amount=%.2f&redirect_url=%s&cancel_url=%s&language=EN"
                        + "&billing_name=%s&billing_email=%s&billing_tel=%s",
                MERCHANT_ID,
                request.getOrderId(),
                request.getAmount(),
                REDIRECT_URL,
                REDIRECT_URL,
                request.getCustomerName(),
                request.getCustomerEmail(),
                request.getCustomerPhone()
        );

        String encRequest = HdfcEncryptionUtil.encrypt(data, WORKING_KEY);
        model.addAttribute("encRequest", encRequest);
        model.addAttribute("accessCode", ACCESS_CODE);
        model.addAttribute("paymentUrl", PAYMENT_URL);

        return "hdfc_redirect_form";
    }


    @PostMapping("/callback")
    @ResponseBody
    public String handleCallback(@RequestParam("encResp") String encResp) throws Exception {
        String decrypted = HdfcEncryptionUtil.decrypt(encResp);
        return "Payment response: " + decrypted;
    }
}
