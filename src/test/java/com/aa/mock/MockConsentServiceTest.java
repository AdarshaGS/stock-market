package com.aa.mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aa.data.AAConsentEntity;
import com.aa.data.ConsentRequest;
import com.aa.data.ConsentResponse;
import com.aa.data.ConsentStatus;
import com.aa.data.FIType;
import com.aa.repo.AAConsentRepository;

@ExtendWith(MockitoExtension.class)
class MockConsentServiceTest {

    @Mock
    private AAConsentRepository consentRepository;

    @InjectMocks
    private MockConsentService service;

    private ConsentRequest consentRequest;
    private AAConsentEntity consentEntity;

    @BeforeEach
    void setUp() {
        consentRequest = ConsentRequest.builder()
                .userId("1")
                .consentTemplateId("test-template")
                .fiTypes(List.of(FIType.MUTUAL_FUNDS, FIType.BANK_ACCOUNTS))
                .build();

        consentEntity = AAConsentEntity.builder()
                .id(1L)
                .consentId("mock-consent-1234")
                .consentTemplateId("test-template")
                .userId("1")
                .status(ConsentStatus.ACTIVE)
                .fiTypes("MUTUAL_FUNDS,BANK_ACCOUNTS")
                .validFrom(LocalDateTime.now().toString())
                .validTill(LocalDateTime.now().plusMonths(6).toString())
                .build();
    }

    @Test
    void testCreateConsent_Success() {
        when(consentRepository.save(any(AAConsentEntity.class))).thenReturn(consentEntity);

        ConsentResponse response = service.createConsent(consentRequest);

        assertNotNull(response);
        assertEquals("test-template", response.getUserConsentId());
        assertEquals(ConsentStatus.ACTIVE, response.getStatus());
        verify(consentRepository, times(1)).save(any(AAConsentEntity.class));
    }

    @Test
    void testGetConsentStatus_Active() {
        when(consentRepository.findByConsentId("mock-consent-1234")).thenReturn(Optional.of(consentEntity));

        ConsentStatus status = service.getConsentStatus("mock-consent-1234");

        assertEquals(ConsentStatus.ACTIVE, status);
    }

    @Test
    void testGetConsentStatus_Expired() {
        consentEntity.setValidTill(LocalDateTime.now().minusDays(1).toString());
        when(consentRepository.findByConsentId("mock-consent-1234")).thenReturn(Optional.of(consentEntity));

        ConsentStatus status = service.getConsentStatus("mock-consent-1234");

        assertEquals(ConsentStatus.EXPIRED, status);
        verify(consentRepository, times(1)).save(consentEntity);
    }

    @Test
    void testRevokeConsent() {
        when(consentRepository.findByConsentId("mock-consent-1234")).thenReturn(Optional.of(consentEntity));

        service.revokeConsent("mock-consent-1234");

        assertEquals(ConsentStatus.REVOKED, consentEntity.getStatus());
        verify(consentRepository, times(1)).save(consentEntity);
    }

    @Test
    void testIsConsentValid_True() {
        when(consentRepository.findByConsentId("mock-consent-1234")).thenReturn(Optional.of(consentEntity));

        boolean valid = service.isConsentValid("mock-consent-1234", FIType.MUTUAL_FUNDS);

        assertTrue(valid);
    }

    @Test
    void testIsConsentValid_False_WrongType() {
        when(consentRepository.findByConsentId("mock-consent-1234")).thenReturn(Optional.of(consentEntity));

        boolean valid = service.isConsentValid("mock-consent-1234", FIType.LOANS);

        assertFalse(valid);
    }

    @Test
    void testGetConsentTemplates() {
        List<Map<String, String>> templates = service.getConsentTemplates();

        assertNotNull(templates);
        assertFalse(templates.isEmpty());
        assertEquals("DEFAULT_READ", templates.get(0).get("templateId"));
    }
}
