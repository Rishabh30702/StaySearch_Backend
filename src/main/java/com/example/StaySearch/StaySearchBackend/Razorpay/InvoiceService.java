package com.example.StaySearch.StaySearchBackend.Razorpay;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;

@Service
public class InvoiceService {

    @Autowired
    private JavaMailSender mailSender;

    public void generateAndSendInvoice(String orderId, String paymentId,
                                       String customerEmail, String hotelName, long amount) {
        try {
            // 1️⃣ Generate invoice PDF as bytes
            byte[] pdfBytes = generateInvoice(orderId, paymentId, customerEmail, hotelName, amount);

            // 2️⃣ Send email with invoice attached
            sendEmailWithInvoice(customerEmail, pdfBytes, orderId, paymentId, amount);

            System.out.println("[InvoiceService] 📧 Invoice sent to " + customerEmail);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] generateInvoice(String orderId, String paymentId,
                                   String customerEmail, String hotelName, long amount) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document();
        PdfWriter.getInstance(doc, baos);

        doc.open();
        doc.add(new Paragraph("StaySearch Hotel Booking Invoice"));
        doc.add(new Paragraph("----------------------------"));
        doc.add(new Paragraph("Order ID: " + orderId));
        doc.add(new Paragraph("Payment ID: " + paymentId));
        doc.add(new Paragraph("Hotel: " + hotelName));
        doc.add(new Paragraph("Amount Paid: ₹" + (amount / 100.0)));
        doc.add(new Paragraph("Customer Email: " + customerEmail));
        doc.add(new Paragraph("Date: " + LocalDateTime.now()));
        doc.add(new Paragraph("Status: SUCCESS ✅"));
        doc.close();

        return baos.toByteArray();
    }

    private void sendEmailWithInvoice(String toEmail, byte[] pdfBytes,
                                      String orderId, String paymentId, long amount) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(toEmail);
        helper.setSubject("StaySearch Booking Confirmation - " + orderId);
        helper.setText("Dear Customer,\n\nYour payment was successful.\n\n" +
                "Order ID: " + orderId + "\n" +
                "Payment ID: " + paymentId + "\n" +
                "Amount: ₹" + (amount / 100.0) + "\n\n" +
                "Your invoice is attached.\n\nThank you for booking with StaySearch!");

        helper.addAttachment("Invoice-" + orderId + ".pdf", () -> new ByteArrayInputStream(pdfBytes));

        mailSender.send(message);
    }
}
