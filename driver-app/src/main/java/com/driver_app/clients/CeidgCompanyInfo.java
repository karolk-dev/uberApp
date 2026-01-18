package com.driver_app.clients;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CeidgCompanyInfo {
    private String id;
    private String nazwa;
    private AdresDzialalnosci adresDzialalnosci;
    private Wlasciciel wlasciciel;
    private String dataRozpoczecia;
    private String status;
    private String link;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdresDzialalnosci {
        private String ulica;
        private String budynek;
        private String lokal;
        private String miasto;
        private String wojewodztwo;
        private String powiat;
        private String gmina;
        private String kraj;
        private String kod;
        private String terc;
        private String simc;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Wlasciciel {
        private String imie;
        private String nazwisko;
        private String nip;
        private String regon;
    }
}