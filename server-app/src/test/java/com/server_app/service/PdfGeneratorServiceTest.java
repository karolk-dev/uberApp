package com.server_app.service;

import com.itextpdf.text.DocumentException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;

public class PdfGeneratorServiceTest {

    private final PdfGeneratorService pdfGeneratorService = new PdfGeneratorService();

    @Test
    public void testGenerateInvoicePdfSuccess() throws DocumentException {
        // when
        ByteArrayOutputStream outputStream = pdfGeneratorService.generateInvoicePdf();
        // then
        assertNotNull(outputStream, "Output stream should not be null");
        byte[] pdfBytes = outputStream.toByteArray();
        assertTrue(pdfBytes.length > 0, "Generated PDF should contain data");

        String pdfContent = new String(pdfBytes);
        assertTrue(pdfContent.contains("%PDF"), "PDF should contain the '%PDF' header");
    }

    @Test
    public void testGenerateInvoicePdfDoesNotThrowException() {
        // when & then
        assertDoesNotThrow(pdfGeneratorService::generateInvoicePdf);
    }
}
