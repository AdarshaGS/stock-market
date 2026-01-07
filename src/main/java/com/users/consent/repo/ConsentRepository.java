package com.users.consent.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.users.consent.data.Consent;

public interface ConsentRepository extends JpaRepository<Consent, Long>, JpaSpecificationExecutor<Consent> {

    java.util.Optional<Consent> findByUserId(Long userId);

    java.util.Optional<Consent> findByUserIdAndAgreed(Long userId, boolean agreed);
}
