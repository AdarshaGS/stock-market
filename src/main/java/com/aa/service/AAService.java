package com.aa.service;

import java.util.List;
import java.util.Map;

import com.aa.data.ConsentRequest;
import com.aa.data.ConsentResponse;
import com.aa.data.ConsentStatusResponse;
import com.aa.data.EncryptedFIPayload;
import com.aa.data.FIRequest;

public interface AAService {
    List<Map<String, String>> getConsentTemplates();

    ConsentResponse createConsent(ConsentRequest request);

    ConsentStatusResponse getConsentStatus(String consentId);

    EncryptedFIPayload fetchFinancialInformation(FIRequest request);

    void revokeConsent(String consentId);
}
