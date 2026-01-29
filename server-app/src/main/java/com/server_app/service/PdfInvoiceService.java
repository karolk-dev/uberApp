package com.server_app.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.uber.common.Invoice;
import com.uber.common.InvoiceItem;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class PdfInvoiceService {

    public ByteArrayOutputStream generateInvoicePdf(Invoice invoice) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);


        document.add(new Paragraph("Faktura VAT").simulateBold()
                .setFontSize(20)
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));


        document.add(new Paragraph("Numer faktury: " + invoice.getInvoiceNumber()));
        document.add(new Paragraph("Data: " + invoice.getInvoiceDate()));
        document.add(new Paragraph("Klient: " + invoice.getCustomerName()));
        document.add(new Paragraph("Adres: " + invoice.getCustomerAdres()));


        Table table = new Table(4);
        table.addHeaderCell("Opis");
        table.addHeaderCell("Ilość");
        table.addHeaderCell("Cena jednostkowa");
        table.addHeaderCell("Razem");

        for (InvoiceItem item : invoice.getItems()) {
            table.addCell(item.getDescription());
            table.addCell(String.valueOf(item.getQuantity()));
            table.addCell(String.format("%.2f", item.getUnitPrice()));
            table.addCell(String.format("%.2f", item.getTotal()));
        }


        document.add(table);


        document.add(new Paragraph("Łączna kwota: " + String.format("%.2f PLN", invoice.getTotalAmount())).simulateBold()
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT));

        document.close();

        return baos;
    }
}
