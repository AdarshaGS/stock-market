package com.stocks.diversification.sectors.service;

import org.springframework.stereotype.Component;

@Component
public class SectorNormalizer {

    public String normalize(String rawIndustry) {
        if (rawIndustry == null || rawIndustry.isBlank()) {
            return "Others";
        }
        return rawIndustry;
    }
}
