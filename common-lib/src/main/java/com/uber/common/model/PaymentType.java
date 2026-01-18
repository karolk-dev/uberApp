package com.uber.common.model;

public enum PaymentType {
    CASH("Gotówka"),
    CARD("Karta"),
    BLIK("BLIK");

    private final String displayName;

    PaymentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}