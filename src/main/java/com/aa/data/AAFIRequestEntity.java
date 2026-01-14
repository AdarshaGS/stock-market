package com.aa.data;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "aa_fi_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AAFIRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", unique = true)
    private String requestId;

    @Column(name = "consent_id")
    private String consentId;

    private String status;

    @Column(name = "encrypted_data", columnDefinition = "TEXT")
    private String encryptedData;
}
