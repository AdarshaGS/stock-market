package com.aa.repo;

import com.aa.data.AAConsentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AAConsentRepository extends JpaRepository<AAConsentEntity, Long> {
    Optional<AAConsentEntity> findByConsentId(String consentId);

    Optional<AAConsentEntity> findByConsentTemplateId(String consentTemplateId);
}
