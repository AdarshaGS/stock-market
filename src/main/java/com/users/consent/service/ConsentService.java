package com.users.consent.service;

import java.util.List;
import com.users.consent.data.ConsentRequest;
import com.users.consent.data.ConsentTemplate;

public interface ConsentService {
    List<ConsentTemplate> getConsentTemplates();

    Long createConsentForUser(ConsentRequest request);
}
