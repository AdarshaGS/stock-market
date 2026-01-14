package com.aa.mock;

import com.aa.adapter.AccountAggregatorAdapter;
import com.aa.data.*;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Component("mockAA")
public class MockAccountAggregator implements AccountAggregatorAdapter {

    private final MockConsentService consentService;
    private final MockFIDataService dataService;
    private final MockEncryptionService encryptionService;

    @Override
    public ConsentResponse createConsent(ConsentRequest request) {
        return consentService.createConsent(request);
    }

    @Override
    public ConsentStatusResponse getConsentStatus(String consentId) {
        ConsentStatus status = consentService.getConsentStatus(consentId);
        return ConsentStatusResponse.builder()
                .consentId(consentId)
                .status(status)
                .build();
    }

    @Override
    public EncryptedFIPayload fetchFinancialInformation(FIRequest request) {
        // Validate consent
        for (FIType type : request.getFiTypes()) {
            if (!consentService.isConsentValid(request.getConsentId(), type)) {
                throw new RuntimeException("Invalid or insufficient consent for: " + type);
            }
        }

        // Generate data
        Map<FIType, List<Object>> data = dataService.generateMockData(request.getFiTypes());

        // Wrap in payload
        FIPayload payload = FIPayload.builder()
                .consentId(request.getConsentId())
                .financialData(data)
                .build();

        // Encrypt and return
        return encryptionService.encrypt(payload);
    }

    @Override
    public void revokeConsent(String consentId) {
        consentService.revokeConsent(consentId);
    }

    @Override
    public List<Map<String, String>> getConsentTemplates() {
        throw new UnsupportedOperationException("Unimplemented method 'getConsentTemplates'");
    }
}
