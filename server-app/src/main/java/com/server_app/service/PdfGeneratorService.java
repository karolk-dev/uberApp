package com.server_app.service;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;

@Service
public class PdfGeneratorService {

    public ByteArrayOutputStream generateInvoicePdf() throws DocumentException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, outputStream);
        document.open();
        LocalDateTime localDateTime = LocalDateTime.now();

        Paragraph header = new Paragraph("Faktura FV/" + localDateTime.getYear() + "/" + localDateTime.getMonthValue()
        + "/" + localDateTime.getDayOfMonth());

        document.add(header);
        document.add(new Paragraph("Numer: FV/"));
        document.add(new Paragraph("Data wystwienia" ));
        document.add(new Paragraph("Nazwa firmy"));
        document.add(new Paragraph("Kwota do zaplaty"));

        document.close();

        return outputStream;
    }
}
