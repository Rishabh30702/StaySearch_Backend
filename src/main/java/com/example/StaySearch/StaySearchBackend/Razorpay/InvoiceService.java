package com.example.StaySearch.StaySearchBackend.Razorpay;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class InvoiceService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private Cloudinary cloudinary;   // ✅ already configured in your project

    @Autowired
    private InvoiceRepository invoiceRepository;  // ✅ DB repository

    public void generateAndSendInvoice(String orderId, String paymentId,
                                       String customerEmail, String hotelName,
                                       long amount, String customerPhone) {
        try {
            // 1️⃣ Generate invoice PDF
            byte[] pdfBytes = generateInvoice(orderId, paymentId, customerEmail, hotelName, amount);

            // 2️⃣ Upload to Cloudinary
            String invoiceUrl = uploadToCloudinary(pdfBytes, orderId);

            // 3️⃣ Save metadata + URL in DB
            InvoiceRequest invoice = new InvoiceRequest();
            invoice.setOrderId(orderId);
            invoice.setPaymentId(paymentId);
            invoice.setCustomerEmail(customerEmail);
            invoice.setCustomerPhone(customerPhone);
            invoice.setHotelName(hotelName);
            invoice.setAmountInPaise(amount);
            invoice.setInvoiceUrl(invoiceUrl);
            invoiceRepository.save(invoice);

            // 4️⃣ Email with PDF attached
            sendEmailWithInvoice(customerEmail, pdfBytes, orderId, paymentId, amount, hotelName);

            System.out.println("[InvoiceService] ✅ Invoice saved, uploaded & sent to " + customerEmail);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] generateInvoice(String orderId, String paymentId,
                                   String customerEmail, String hotelName, long amount) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter.getInstance(doc, baos);

        doc.open();

        // Title
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, BaseColor.DARK_GRAY);
        Paragraph title = new Paragraph("StaySearch Hotel Booking Invoice", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);

        doc.add(new Paragraph("\n"));

        // Table with invoice details
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        addTableCell(table, "Order ID:", orderId);
        addTableCell(table, "Payment ID:", paymentId);
        addTableCell(table, "Hotel Name:", hotelName);
        addTableCell(table, "Amount Paid:", "₹" + (amount / 100.0));
        addTableCell(table, "Customer Email:", customerEmail);
        addTableCell(table, "Date:", LocalDateTime.now().toString());
        addTableCell(table, "Status:", "SUCCESS ✅");

        doc.add(table);

        doc.add(new Paragraph("\nThank you for booking with StaySearch. We look forward to hosting you!",
                new Font(Font.FontFamily.HELVETICA, 12, Font.ITALIC, BaseColor.GRAY)));

        doc.close();
        return baos.toByteArray();
    }

    private void addTableCell(PdfPTable table, String key, String value) {
        PdfPCell cell1 = new PdfPCell(new Phrase(key, new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD)));
        PdfPCell cell2 = new PdfPCell(new Phrase(value, new Font(Font.FontFamily.HELVETICA, 12)));

        cell1.setBorder(Rectangle.NO_BORDER);
        cell2.setBorder(Rectangle.NO_BORDER);

        table.addCell(cell1);
        table.addCell(cell2);
    }

    private void sendEmailWithInvoice(String toEmail, byte[] pdfBytes,
                                      String orderId, String paymentId, long amount, String hotelName) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(toEmail);
        helper.setSubject("StaySearch Booking Confirmation - " + orderId);

        String htmlContent =
                "<html>" +
                        "<body style='font-family: Arial, sans-serif; line-height: 1.6;'>" +
                        "<h2 style='color:#2c3e50;'>Booking Confirmation - StaySearch</h2>" +
                        "<p>Dear Customer,</p>" +
                        "<p>Your payment has been successfully processed for your hotel booking.</p>" +
                        "<table style='width:100%; border-collapse: collapse;'>" +
                        "<tr><td style='padding:8px; border:1px solid #ddd;'><b>Order ID:</b></td>" +
                        "<td style='padding:8px; border:1px solid #ddd;'>" + orderId + "</td></tr>" +
                        "<tr><td style='padding:8px; border:1px solid #ddd;'><b>Payment ID:</b></td>" +
                        "<td style='padding:8px; border:1px solid #ddd;'>" + paymentId + "</td></tr>" +
                        "<tr><td style='padding:8px; border:1px solid #ddd;'><b>Hotel:</b></td>" +
                        "<td style='padding:8px; border:1px solid #ddd;'>" + hotelName + "</td></tr>" +
                        "<tr><td style='padding:8px; border:1px solid #ddd;'><b>Amount:</b></td>" +
                        "<td style='padding:8px; border:1px solid #ddd;'>₹" + (amount / 100.0) + "</td></tr>" +
                        "</table>" +
                        "<p>Your detailed invoice is attached to this email.</p>" +
                        "<p>Thank you for choosing <b>StaySearch</b>. We look forward to serving you!</p>" +
                        "<br><p style='color:gray; font-size:12px;'>This is an automated email. Please do not reply.</p>" +
                        "</body></html>";

        helper.setText(htmlContent, true);

        helper.addAttachment("Invoice-" + orderId + ".pdf", () -> new ByteArrayInputStream(pdfBytes));

        mailSender.send(message);
    }

    private String uploadToCloudinary(byte[] pdfBytes, String orderId) throws Exception {
        Map uploadResult = cloudinary.uploader().upload(
                pdfBytes,
                ObjectUtils.asMap(
                        "resource_type", "raw",  // 📂 raw because it's a PDF
                        "public_id", "invoices/Invoice-" + orderId
                )
        );
        return uploadResult.get("secure_url").toString();
    }
}