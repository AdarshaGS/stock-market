package com.aa.adapter;

import com.aa.data.*;
import com.externalServices.service.ExternalService;
import com.externalServices.data.ExternalServicePropertiesEntity;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;
import java.util.Collections;

@Slf4j
@Component("externalAA")
public class ExternalAccountAggregatorAdapter implements AccountAggregatorAdapter {

    private final ExternalService externalService;
    private final RestTemplate restTemplate = new RestTemplate();

    public ExternalAccountAggregatorAdapter(ExternalService externalService) {
        this.externalService = externalService;
    }

    private String getBaseUrl() {
        List<ExternalServicePropertiesEntity> properties = externalService
                .getExternalServicePropertiesByServiceName("AA_SIMULATOR");
        if (properties == null || properties.isEmpty()) {
            return "http://localhost:8081/aa";
        }
        return properties.stream()
                .filter(p -> "url".equalsIgnoreCase(p.getName()))
                .map(ExternalServicePropertiesEntity::getValue)
                .findFirst()
                .orElse("http://localhost:8081/aa");
    }

    @Override
    public ConsentResponse createConsent(ConsentRequest request) {
        String url = getBaseUrl() + "/consents";
        log.info("Creating consent at {} for user {}", url, request.getUserId());
        return restTemplate.postForObject(url, request, ConsentResponse.class);
    }

    @Override
    public ConsentStatusResponse getConsentStatus(String consentId) {
        String url = getBaseUrl() + "/consents/" + consentId;
        log.info("Getting consent status from {}", url);
        return restTemplate.getForObject(url, ConsentStatusResponse.class);
    }

    @Override
    public EncryptedFIPayload fetchFinancialInformation(FIRequest request) {
        String url = getBaseUrl() + "/fetch";
        log.info("Fetching financial information from {} for consent {}", url, request.getConsentId());
        return restTemplate.postForObject(url, request, EncryptedFIPayload.class);
    }

    @Override
    public void revokeConsent(String consentId) {
        String url = getBaseUrl() + "/consents/" + consentId;
        log.info("Revoking consent at {}", url);
        restTemplate.delete(url);
    }

    @Override
    public List<Map<String, String>> getConsentTemplates() {
        // This might need a new endpoint in simulator if supported
        return Collections.emptyList();
    }
}
