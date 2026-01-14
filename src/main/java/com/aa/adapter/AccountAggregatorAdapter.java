package com.aa.adapter;

import java.util.List;
import java.util.Map;

import com.aa.data.*;

public interface AccountAggregatorAdapter {

    ConsentResponse createConsent(ConsentRequest request);

    ConsentStatusResponse getConsentStatus(String consentId);

    EncryptedFIPayload fetchFinancialInformation(FIRequest request);

    void revokeConsent(String consentId);

    List<Map<String, String>> getConsentTemplates();
}
