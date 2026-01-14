package com.users.consent.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.users.consent.data.ConsentRequest;
import com.users.consent.data.ConsentTemplate;
import com.users.consent.service.ConsentService;

@RestController
@RequestMapping("api/v1/consent")
@Tag(name = "Consent Management", description = "APIs for managing user consent for Account Aggregator authorization")
@PreAuthorize("isAuthenticated()")
public class ConsentAPIResource {

    private final ConsentService consentService;

    public ConsentAPIResource(final ConsentService consentService) {
        this.consentService = consentService;
    }

    @GetMapping("/template")
    @Operation(summary = "Get consent templates", description = "Retrieves all available consent templates for Account Aggregator")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved consent templates")
    public List<ConsentTemplate> getConsentTemplate() {
        return this.consentService.getConsentTemplates();
    }

    @PostMapping()
    @Operation(summary = "Create user consent", description = "Records user consent for Account Aggregator data access")
    @ApiResponse(responseCode = "200", description = "Successfully created consent")
    public Long createConsent(@RequestBody ConsentRequest consentRequest) {
        return this.consentService.createConsentForUser(consentRequest);
    }
}
