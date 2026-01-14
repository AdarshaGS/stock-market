package com.aa.controller;

import com.aa.adapter.AccountAggregatorAdapter;
import com.aa.data.*;
import com.aa.mock.MockEncryptionService;
import com.aa.repo.AAFIRequestRepository;
import com.aa.service.AAService;
import com.portfolio.engine.PortfolioEngine;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/aa")
@Tag(name = "Account Aggregator", description = "Endpoints for Mock AA data flow")
@PreAuthorize("isAuthenticated()")
@Slf4j
public class AAController {

    private final AAService aaService;
    private final MockEncryptionService encryptionService;
    private final PortfolioEngine portfolioEngine;
    private final AAFIRequestRepository fiRequestRepository;

    public AAController(AAService aaService,
            MockEncryptionService encryptionService,
            PortfolioEngine portfolioEngine,
            AAFIRequestRepository fiRequestRepository) {
        this.aaService = aaService;
        this.encryptionService = encryptionService;
        this.portfolioEngine = portfolioEngine;
        this.fiRequestRepository = fiRequestRepository;
    }

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @GetMapping("/templates")
    @Operation(summary = "Get Consent Templates")
    public ResponseEntity<List<Map<String, String>>> getTemplates() {
        return ResponseEntity.ok(aaService.getConsentTemplates());
    }

    @PostMapping("/consent")
    @Operation(summary = "Create Consent")
    public ConsentResponse createConsent(@RequestBody ConsentRequest request) {
        return aaService.createConsent(request);
    }

    @GetMapping("/consent/{consentId}/status")
    @Operation(summary = "Get Consent Status")
    public ConsentStatusResponse getConsentStatus(@PathVariable("consentId") String consentId) {
        return aaService.getConsentStatus(consentId);
    }

    @PostMapping("/fetch")
    @Operation(summary = "Request Financial Information (Async Simulator)")
    public ResponseEntity<Map<String, String>> initiateFetch(@RequestBody FIRequest request) {
        String requestId = "req-" + java.util.UUID.randomUUID().toString().substring(0, 8);

        AAFIRequestEntity entity = AAFIRequestEntity.builder()
                .requestId(requestId)
                .consentId(request.getConsentId())
                .status("PENDING")
                .build();
        fiRequestRepository.save(entity);

        // Simulate Async processing
        scheduler.schedule(() -> {
            try {
                log.info("Mock AA: Processing FI Fetch for requestId: {}", requestId);
                EncryptedFIPayload encrypted = aaService.fetchFinancialInformation(request);

                AAFIRequestEntity updatedEntity = fiRequestRepository.findByRequestId(requestId)
                        .orElseThrow(() -> new RuntimeException("Request not found: " + requestId));

                updatedEntity.setEncryptedData(encrypted.getData());
                updatedEntity.setStatus("READY");
                fiRequestRepository.save(updatedEntity);

                log.info("Mock AA: FI Data READY for requestId: {}", requestId);
            } catch (Exception e) {
                fiRequestRepository.findByRequestId(requestId).ifPresent(ent -> {
                    ent.setStatus("FAILED: " + e.getMessage());
                    fiRequestRepository.save(ent);
                });
                log.error("Mock AA: FI Fetch FAILED for requestId: {}", requestId, e);
            }
        }, 3, TimeUnit.SECONDS);

        return ResponseEntity.ok(Map.of("requestId", requestId, "status", "PENDING"));
    }

    @GetMapping("/fetch/{requestId}/status")
    @Operation(summary = "Poll FI Fetch Status")
    public ResponseEntity<Map<String, String>> getFetchStatus(@PathVariable("requestId") String requestId) {
        String status = fiRequestRepository.findByRequestId(requestId)
                .map(AAFIRequestEntity::getStatus)
                .orElse("NOT_FOUND");
        return ResponseEntity.ok(Map.of("requestId", requestId, "status", status));
    }

    @GetMapping("/fetch/{requestId}/data")
    @Operation(summary = "Get Decrypted Portfolio Insights")
    public ResponseEntity<?> getDecryptedData(@PathVariable("requestId") String requestId) {
        AAFIRequestEntity entity = fiRequestRepository.findByRequestId(requestId)
                .orElse(null);

        if (entity == null || !"READY".equals(entity.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Data not ready or request failed"));
        }

        EncryptedFIPayload encrypted = EncryptedFIPayload.builder()
                .data(entity.getEncryptedData())
                .keyId("mock-key-001")
                .build();

        FIPayload payload = encryptionService.decrypt(encrypted);
        PortfolioEngine.PortfolioMetrics metrics = portfolioEngine.computeMetrics(payload);

        return ResponseEntity.ok(Map.of(
                "consentId", payload.getConsentId(),
                "metrics", metrics,
                "rawData", payload.getFinancialData()));
    }

    @DeleteMapping("/consent/{consentId}")
    @Operation(summary = "Revoke Consent")
    public void revokeConsent(@PathVariable("consentId") String consentId) {
        aaService.revokeConsent(consentId);
    }
}
