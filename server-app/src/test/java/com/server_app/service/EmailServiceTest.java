package com.server_app.service;

import com.itextpdf.text.DocumentException;
import com.server_app.model.Ride;
import com.uber.common.productSelector.Product;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.io.ByteArrayOutputStream;
import java.util.Properties;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private PdfGeneratorService pdfGeneratorService;

    @Mock
    private PdfInvoiceService pdfInvoiceService;

    @InjectMocks
    private EmailService emailService;

    @Test
    void sendInvoiceEmail_Success() throws Exception {

        Session session = Session.getDefaultInstance(new Properties());
        MimeMessage realMimeMessage = new MimeMessage(session);
        when(javaMailSender.createMimeMessage()).thenReturn(realMimeMessage);
        when(pdfGeneratorService.generateInvoicePdf()).thenReturn(new ByteArrayOutputStream());
        when(pdfInvoiceService.generateInvoicePdf(any())).thenReturn(new ByteArrayOutputStream());

        // Arrange
        Ride ride = Ride.builder()
                .uuid("uuid")
                .penaltyAmount(300)
                .product(Product.UberX)
                .amount(2000)
                .build();
        String testEmail = "test@example.com";
        // Act
        emailService.sendInvoiceEmail(testEmail, ride);

        // Assert
        verify(javaMailSender).send(any(MimeMessage.class));
        verify(pdfGeneratorService).generateInvoicePdf();
        verify(pdfInvoiceService).generateInvoicePdf(any());
    }

    @Test
    void sendInvoiceEmail_PdfGenerationFailure() throws Exception {
        // Arrange
        Ride ride = Ride.builder()
                .uuid("uuid")
                .penaltyAmount(300)
                .product(Product.UberX)
                .amount(2000)
                .build();
        String testEmail = "test@example.com";

        when(pdfGeneratorService.generateInvoicePdf()).thenThrow(new DocumentException());

        // Act & Assert
        assertThrows(DocumentException.class, () -> {
            emailService.sendInvoiceEmail(testEmail, ride);
        });
    }


}
