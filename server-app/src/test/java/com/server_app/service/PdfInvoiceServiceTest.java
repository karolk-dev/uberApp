package com.server_app.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.uber.common.Invoice;
import com.uber.common.InvoiceItem;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class PdfInvoiceServiceTest {

    private final PdfInvoiceService pdfInvoiceService = new PdfInvoiceService();

    @Test
    void testGenerateInvoicePdf() throws Exception {
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber("INV-12345");
        invoice.setInvoiceDate(LocalDate.parse("2025-03-23"));
        invoice.setCustomerName("Test Customer");
        invoice.setCustomerAdres("Test Address");

        InvoiceItem item1 = new InvoiceItem();
        item1.setDescription("Produkt A");
        item1.setQuantity(2);
        item1.setUnitPrice(10.0);
        item1.setTotal(20.0);

        InvoiceItem item2 = new InvoiceItem();
        item2.setDescription("Produkt B");
        item2.setQuantity(1);
        item2.setUnitPrice(15.0);
        item2.setTotal(15.0);

        invoice.setItems(Arrays.asList(item1, item2));
        invoice.setTotalAmount(BigDecimal.valueOf(35.0));

        ByteArrayOutputStream pdfOutput = pdfInvoiceService.generateInvoicePdf(invoice);
        assertNotNull(pdfOutput, "Output stream nie powinien być nullem");
        byte[] pdfBytes = pdfOutput.toByteArray();
        assertTrue(pdfBytes.length > 0, "PDF powinien zawierać dane");

        PdfDocument pdfDoc = new PdfDocument(new PdfReader(new ByteArrayInputStream(pdfBytes)));
        String pageContent = PdfTextExtractor.getTextFromPage(pdfDoc.getPage(1));
        pdfDoc.close();

        assertTrue(pageContent.contains("Faktura VAT"), "Dokument powinien zawierać tytuł faktury");
        assertTrue(pageContent.contains("INV-12345"), "Numer faktury powinien być zawarty w dokumencie");
        assertTrue(pageContent.contains("2025-03-23"), "Data faktury powinna być zawarta w dokumencie");
        assertTrue(pageContent.contains("Test Customer"), "Nazwa klienta powinna być zawarta w dokumencie");
        assertTrue(pageContent.contains("Test Address"), "Adres klienta powinien być zawarty w dokumencie");
        assertTrue(pageContent.contains("Produkt A"), "Opis pierwszego produktu powinien być zawarty w dokumencie");
        assertTrue(pageContent.contains("Produkt B"), "Opis drugiego produktu powinien być zawarty w dokumencie");
        assertTrue(pageContent.contains("35.00 PLN"), "Łączna kwota powinna być sformatowana i zawarta w dokumencie");
    }

    @Test
    void testGenerateInvoicePdfWithEmptyItems() throws Exception {
        // Przygotowanie faktury bez pozycji
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber("INV-00001");
        invoice.setInvoiceDate(LocalDate.parse("2025-03-23"));
        invoice.setCustomerName("Empty Items Customer");
        invoice.setCustomerAdres("Nowhere");
        invoice.setItems(Collections.emptyList());
        invoice.setTotalAmount(BigDecimal.valueOf(0.0));

        ByteArrayOutputStream pdfOutput = pdfInvoiceService.generateInvoicePdf(invoice);
        assertNotNull(pdfOutput, "Output stream nie powinien być nullem");
        byte[] pdfBytes = pdfOutput.toByteArray();
        assertTrue(pdfBytes.length > 0, "PDF powinien zawierać dane");

        PdfDocument pdfDoc = new PdfDocument(new PdfReader(new ByteArrayInputStream(pdfBytes)));
        String pageContent = PdfTextExtractor.getTextFromPage(pdfDoc.getPage(1));
        pdfDoc.close();

        assertTrue(pageContent.contains("Faktura VAT"), "Dokument powinien zawierać tytuł faktury");
        assertTrue(pageContent.contains("INV-00001"), "Numer faktury powinien być zawarty w dokumencie");
        assertTrue(pageContent.contains("Empty Items Customer"), "Nazwa klienta powinna być zawarta w dokumencie");
        assertTrue(pageContent.contains("Nowhere"), "Adres klienta powinien być zawarty w dokumencie");
        assertTrue(pageContent.contains("Opis"), "Tabela powinna zawierać nagłówek 'Opis'");
        assertTrue(pageContent.contains("0.00 PLN"), "Łączna kwota powinna być sformatowana jako 0.00 PLN");
    }

    @Test
    void testGenerateInvoicePdfWithNullInvoice() {
        assertThrows(NullPointerException.class, () -> {
            pdfInvoiceService.generateInvoicePdf(null);
        });
    }
}
