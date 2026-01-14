package com.aa.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.aa.adapter.AccountAggregatorAdapter;
import com.aa.data.ConsentRequest;
import com.aa.data.ConsentResponse;
import com.aa.data.ConsentStatusResponse;
import com.aa.data.EncryptedFIPayload;
import com.aa.data.FIRequest;

@Service
public class AAServiceImpl implements AAService {

    private final AccountAggregatorAdapter accountAggregatorAdapter;

    public AAServiceImpl(@Qualifier("externalAA") AccountAggregatorAdapter accountAggregatorAdapter) {
        this.accountAggregatorAdapter = accountAggregatorAdapter;
    }

    @Override
    public List<Map<String, String>> getConsentTemplates() {
        return accountAggregatorAdapter.getConsentTemplates();
    }

    @Override
    public ConsentResponse createConsent(ConsentRequest request) {
        return accountAggregatorAdapter.createConsent(request);
    }

    @Override
    public ConsentStatusResponse getConsentStatus(String consentId) {
        return accountAggregatorAdapter.getConsentStatus(consentId);
    }

    @Override
    public EncryptedFIPayload fetchFinancialInformation(FIRequest request) {
        return accountAggregatorAdapter.fetchFinancialInformation(request);
    }

    @Override
    public void revokeConsent(String consentId) {
        accountAggregatorAdapter.revokeConsent(consentId);
    }

}
