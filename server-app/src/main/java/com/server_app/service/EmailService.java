package com.server_app.service;

import com.server_app.model.Ride;
import com.uber.common.Invoice;
import com.uber.common.InvoiceItem;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender javaMailSender;
    private final PdfGeneratorService pdfGeneratorService;
    private final PdfInvoiceService pdfInvoiceService;

    public void sendInvoiceEmail(String toEmail, Ride ride) throws Exception {
        InvoiceItem invoiceItem = InvoiceItem.builder()
                .description("route")
                .total(ride.getAmount())
                .quantity(1)
                .unitPrice(ride.getProduct().getPriceMultiplier())
                .build();
        InvoiceItem penalty = InvoiceItem.builder()
                .description("rzyganie")
                .total(ride.getPenaltyAmount())
                .unitPrice(300)
                .quantity(1)
                .build();

        Invoice invoice = Invoice.builder()
                .customerAdres("")
                .invoiceDate(LocalDate.now())
                .invoiceNumber(UUID.randomUUID().toString())
                .items(List.of(invoiceItem, penalty))
                .customerName("jan")
                .durationInMinutes(20)
                .distance(20)
                .build();

        ByteArrayOutputStream pdfOutputStream = pdfGeneratorService.generateInvoicePdf();
        ByteArrayOutputStream outputStream = pdfInvoiceService.generateInvoicePdf(invoice);

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(toEmail);
        helper.setSubject("faktura");
        helper.setText("przesylam fakture");

        helper.addAttachment("faktura.pdf", new ByteArrayResource(pdfOutputStream.toByteArray()));

        MimeMessage message1 = javaMailSender.createMimeMessage();
        MimeMessageHelper helper1 = new MimeMessageHelper(message1, true);

        helper.setTo(toEmail);
        helper.setSubject("test");
        helper.addAttachment("testFV.pdf", new ByteArrayResource(outputStream.toByteArray()));

        javaMailSender.send(message);
    }

    public void sendEarningsReport(String email, byte[] report) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(email);
        helper.setSubject("Raport zarobków");
        helper.setText("W załączniku znajduje się raport zarobków.");

        helper.addAttachment("earnings_report.csv", new ByteArrayResource(report));

        javaMailSender.send(message);
    }
}
