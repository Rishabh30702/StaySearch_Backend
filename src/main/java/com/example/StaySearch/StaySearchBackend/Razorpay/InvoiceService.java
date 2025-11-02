package com.example.StaySearch.StaySearchBackend.Razorpay;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.StaySearch.StaySearchBackend.PaymentGateway.PaymentGatewaySettingsService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

@Service
public class InvoiceService {

    @Autowired
    private Cloudinary cloudinary;   // ✅ already configured in your project

    @Autowired
    private InvoiceRepository invoiceRepository;  // ✅ DB repository


    @Autowired
    private PaymentGatewaySettingsService paymentGatewayService;

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Value("${sendgrid.from.email}")
    private String fromEmail;

    public void generateAndSendInvoice(String paymentId,
                                       String customerEmail,
                                       String hotelName,
                                       long amount,
                                       String customerPhone) {
        try {
            byte[] pdfBytes = generateInvoice(paymentId, customerEmail, hotelName, amount);

            String invoiceUrl = uploadToCloudinary(pdfBytes, paymentId);

            InvoiceRequest invoice = new InvoiceRequest();
            invoice.setPaymentId(paymentId);
            invoice.setCustomerEmail(customerEmail);
            invoice.setCustomerPhone(customerPhone);
            invoice.setHotelName(hotelName);
            invoice.setAmountInPaise(amount);
            invoice.setInvoiceUrl(invoiceUrl);
            invoiceRepository.save(invoice);

            sendEmailWithInvoice(customerEmail, pdfBytes, paymentId, amount, hotelName);

            System.out.println("[InvoiceService] ✅ Invoice saved, uploaded & sent to " + customerEmail);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private byte[] generateInvoice(String paymentId,
                                   String customerEmail,
                                   String hotelName,
                                   long amount) throws Exception {
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

    private void sendEmailWithInvoice(String toEmail,
                                      byte[] pdfBytes,
                                      String paymentId,
                                      long amount,
                                      String hotelName) throws Exception {
        Email from = new Email(fromEmail);
        Email to = new Email(toEmail);
        String subject = "StaySearch Booking Confirmation - " + paymentId;

        String htmlContent =
                "<html>" +
                        "<body style='font-family: Arial, sans-serif; line-height: 1.6;'>" +
                        "<h2 style='color:#2c3e50;'>Booking Confirmation - StaySearch</h2>" +
                        "<p>Dear Customer,</p>" +
                        "<p>Your payment has been successfully processed for your hotel booking.</p>" +
                        "<table style='width:100%; border-collapse: collapse;'>" +
                        "<tr><td><b>Payment ID:</b></td><td>" + paymentId + "</td></tr>" +
                        "<tr><td><b>Hotel:</b></td><td>" + hotelName + "</td></tr>" +
                        "<tr><td><b>Amount:</b></td><td>₹" + (amount / 100.0) + "</td></tr>" +
                        "</table>" +
                        "<p>Your detailed invoice is attached to this email.</p>" +
                        "<p>Thank you for choosing <b>StaySearch</b>.</p>" +
                        "<p style='color:gray; font-size:12px;'>This is an automated email. Please do not reply.</p>" +
                        "</body></html>";

        Content content = new Content("text/html", htmlContent);
        Mail mail = new Mail(from, subject, to, content);

        // ✅ Attach PDF
        Attachments attachments = new Attachments();
        attachments.setContent(Base64.getEncoder().encodeToString(pdfBytes));
        attachments.setType("application/pdf");
        attachments.setFilename("Invoice-" + paymentId + ".pdf");
        attachments.setDisposition("attachment");
        mail.addAttachments(attachments);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        Response response = sg.api(request);
        System.out.println("Status Code: " + response.getStatusCode());
        System.out.println("Body: " + response.getBody());
    }

    private String uploadToCloudinary(byte[] pdfBytes, String paymentId) throws Exception {
        Map uploadResult = cloudinary.uploader().upload(
                pdfBytes,
                ObjectUtils.asMap(
                        "resource_type", "raw",
                        "public_id", "invoices/Invoice-" + paymentId
                )
        );
        return uploadResult.get("secure_url").toString();
    }
}
