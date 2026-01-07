package com.investments.mutualfunds.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.users.consent.data.Consent;
import com.users.consent.repo.ConsentRepository;
import com.investments.mutualfunds.data.MFAssetType;
import com.investments.mutualfunds.data.MFTransaction;
import com.investments.mutualfunds.data.MutualFundHolding;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MutualFundFetchServiceImpl implements MutualFundFetchService {

    private final ConsentRepository consentRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<MutualFundHolding> fetchPortfolio(Long userId) {
        // 1. Consent Validation
        Consent activeConsent = consentRepository.findByUserIdAndAgreed(userId, true)
                .orElseThrow(() -> new RuntimeException("Active consent not found for user: " + userId));

        // 2. Verify Consent Metadata (Simulated - in real world check expiration/types against AA)
        verifyConsentMetadata(activeConsent);

        // 3. Request FI Data
        String rawResponse = fetchRawAAData(activeConsent.getConsentId());

        // 4. Normalize
        return normalizeResponse(rawResponse);
    }

    private void verifyConsentMetadata(Consent consent) {
        // In a real implementation, we would check the 'ConsentStatus' from the AA provider.
        // Here we simulate checking if MF types are present.
        boolean includesMF = true; // Simulated check
        if (!includesMF) {
            throw new RuntimeException("Consent does not include MUTUAL_FUNDS");
        }
        // Check expiry (Simulated)
    }

    private String fetchRawAAData(Long consentHandle) {
        // Simulate AA Fetch for 'MUTUAL_FUNDS' and 'MF_TRANSACTIONS'
        // In real world: HttpClient.send(...)
        
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
        try {
            Map<String, Object> root = objectMapper.readValue(jsonBody, new TypeReference<Map<String, Object>>() {});
            List<Map<String, Object>> holdings = (List<Map<String, Object>>) root.get("holdings");

            for (Map<String, Object> h : holdings) {
                Map<String, Object> summary = (Map<String, Object>) h.get("summary");
                
                List<MFTransaction> transactions = new ArrayList<>();
                if (h.containsKey("transactions")) {
                    List<Map<String, Object>> txns = (List<Map<String, Object>>) h.get("transactions");
                    for (Map<String, Object> t : txns) {
                        transactions.add(MFTransaction.builder()
                            .txnDate(java.time.LocalDate.parse((String)t.get("date")))
                            .txnType((String)t.get("type"))
                            .amount(BigDecimal.valueOf(((Number)t.get("amount")).doubleValue()))
                            .units(BigDecimal.valueOf(((Number)t.get("units")).doubleValue()))
                            .nav(BigDecimal.valueOf(((Number)t.get("nav")).doubleValue()))
                            .build());
                    }
                }

                portfolio.add(MutualFundHolding.builder()
                    .amc((String) h.get("amc"))
                    .schemeName((String) h.get("scheme"))
                    .folioNumber((String) h.get("folio"))
                    .assetType(MFAssetType.valueOf((String) h.get("type"))) // Mapping Check
                    .units(BigDecimal.valueOf(((Number) summary.get("units")).doubleValue()))
                    .nav(BigDecimal.valueOf(((Number) summary.get("nav")).doubleValue()))
                    .currentValue(BigDecimal.valueOf(((Number) summary.get("curValue")).doubleValue()))
                    .costValue(BigDecimal.valueOf(((Number) summary.get("costValue")).doubleValue()))
                    .transactions(transactions)
                    .build());
            }

        } catch (Exception e) {
            log.error("Failed to parse AA response", e);
            throw new RuntimeException("Data Normalization Failed", e);
        }
        return portfolio;
    }
}
