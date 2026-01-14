package com.aa.data;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "aa_consents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AAConsentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "consent_id", unique = true)
    private String consentId;

    @Column(name = "consent_template_id", unique = true)
    private String consentTemplateId;

    @Column(name = "user_id")
    private String userId;

    @Enumerated(EnumType.STRING)
    private ConsentStatus status;

    @Column(name = "fi_types")
    private String fiTypes; // Comma-separated FIType names

    @Column(name = "valid_from")
    private String validFrom;

    @Column(name = "valid_till")
    private String validTill;
}
