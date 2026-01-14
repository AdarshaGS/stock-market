package com.users.consent.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import com.aa.mock.MockConsentService;
import com.users.consent.data.ConsentTemplate;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConsentServiceImpl implements ConsentService {

    private final MockConsentService mockConsentService;

    @Override
    public List<ConsentTemplate> getConsentTemplates() {
        return mockConsentService.getConsentTemplates().stream()
                .map(m -> new ConsentTemplate(m.get("description")))
                .collect(Collectors.toList());
    }

    @Override
    public Long createConsentForUser(com.users.consent.data.ConsentRequest request) {
        // Adapt the request to the new Mock AA system
        com.aa.data.ConsentRequest aaRequest = com.aa.data.ConsentRequest.builder()
                .userId(String.valueOf(request.getUserId()))
                .consentTemplateId(request.getConsentTemplateId())
                .build();

        mockConsentService.createConsent(aaRequest);
        // The old API returned a Long (likely a DB ID), we'll return a dummy one or use
        // the user ID
        return request.getUserId();
    }
}
