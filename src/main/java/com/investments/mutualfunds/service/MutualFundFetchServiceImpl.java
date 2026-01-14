package com.investments.mutualfunds.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.aa.data.ConsentStatus;
import com.aa.repo.AAConsentRepository;
import com.investments.mutualfunds.data.MutualFundHolding;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MutualFundFetchServiceImpl implements MutualFundFetchService {

    private final AAConsentRepository consentRepository;

    @Override
    public List<MutualFundHolding> fetchPortfolio(Long userId) {
        // 1. Consent Validation (Simplistic for mock)
        // In real world, we would filter by status and types
        boolean hasActiveConsent = consentRepository.findAll().stream()
                .anyMatch(c -> c.getUserId().equals(userId) && c.getStatus() == ConsentStatus.ACTIVE);

        if (!hasActiveConsent) {
            throw new RuntimeException("Active AA consent not found for user: " + userId);
        }

        // 2. Request FI Data (Simplified mock)
        String rawResponse = fetchRawAAData();

        // 3. Normalize
        return normalizeResponse(rawResponse);
    }

    private String fetchRawAAData() {
        // Mock Response conforming to RBI AA Schema (Simplified)
        return """
                    {
                        "holdings": [
                            {
                                "amc": "HDFC Mutual Fund",
                                "scheme": "HDFC Top 100 Fund - Direct Growth",
                                "folio": "11223344",
                                "isin": "INF179K01BE2",
                                "type": "EQUITY",
                                "summary": {
                                    "units": 100.5,
                                    "nav": 500.0,
                                    "curValue": 50250.0,
                                    "costValue": 45000.0
                                },
                                "transactions": [
                                    {
                                        "date": "2024-01-01",
                                        "type": "BUY",
                                        "amount": 45000.0,
                                        "units": 100.5,
                                        "nav": 447.76
                                    }
                                ]
                            },
                            {
                                "amc": "ICICI Prudential",
                                "scheme": "ICICI Pru Bluechip Fund",
                                "folio": "55667788",
                                "isin": "INF109K01Z48",
                                "type": "EQUITY",
                                "summary": {
                                    "units": 50.0,
                                    "nav": 80.0,
                                    "curValue": 4000.0,
                                    "costValue": 3500.0
                                },
                                 "transactions": []
                            }
                        ]
                    }
                """;
    }

    private List<MutualFundHolding> normalizeResponse(String jsonBody) {
        List<MutualFundHolding> portfolio = new ArrayList<>();
        // In a real implementation, use ObjectMapper. For mock, we can return empty or
        // hardcoded
        return portfolio;
    }
}
