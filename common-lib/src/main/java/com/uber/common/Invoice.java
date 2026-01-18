package com.uber.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Invoice {
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private String customerName;
    private String customerAdres;
    private BigDecimal totalAmount;
    private double distance;
    private double durationInMinutes;
    private List<InvoiceItem> items;


}
