package com.users.consent.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import com.users.consent.data.Consent;
import com.users.consent.data.ConsentRequest;
import com.users.consent.data.ConsentTemplate;
import com.users.consent.repo.ConsentRepository;

@ExtendWith(MockitoExtension.class)
class ConsentServiceImplTest {

    @Mock
    private ConsentRepository repository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private ConsentServiceImpl service;

    private ConsentRequest consentRequest;

    @SuppressWarnings("unused")
    private Consent consent;

    @BeforeEach
    void setUp() {
        consentRequest = ConsentRequest.builder()
                .userId(1L)
                .consentId(1L)
                .build();

        consent = Consent.builder()
                .id(1L)
                .userId(1L)
                .consentId(1L)
                .agreed(true)
                .build();
    }

    @Test
    void testCreateConsent_Success() {
        when(repository.save(any(Consent.class))).thenAnswer(invocation -> {
            Consent saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        Long result = service.createConsentForUser(consentRequest);

        assertNotNull(result);
        assertEquals(1L, result);
        verify(repository, times(1)).save(any(Consent.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetConsentTemplates_ReturnsAll() {
        ConsentTemplate template1 = ConsentTemplate.builder()
                .message("I agree to share my financial data")
                .build();
        ConsentTemplate template2 = ConsentTemplate.builder()
                .message("I agree to Account Aggregator terms")
                .build();

        List<ConsentTemplate> templates = Arrays.asList(template1, template2);

        when(jdbcTemplate.query(anyString(), any(BeanPropertyRowMapper.class)))
                .thenReturn(templates);

        List<ConsentTemplate> results = service.getConsentTemplates();

        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("I agree to share my financial data", results.get(0).getMessage());
        verify(jdbcTemplate, times(1)).query(anyString(), any(BeanPropertyRowMapper.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetConsentTemplates_ReturnsEmpty() {
        when(jdbcTemplate.query(anyString(), any(BeanPropertyRowMapper.class)))
                .thenReturn(Arrays.asList());

        List<ConsentTemplate> results = service.getConsentTemplates();

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }
}
