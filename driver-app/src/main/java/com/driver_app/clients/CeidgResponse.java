package com.driver_app.clients;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CeidgResponse {
    private List<CeidgCompanyInfo> firma;
    private Properties properties;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Properties {
        private String title;
        private String description;
        private String language;
        private String provider;
        private String datePublished;
    }
}