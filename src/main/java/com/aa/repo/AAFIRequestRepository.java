package com.aa.repo;

import com.aa.data.AAFIRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AAFIRequestRepository extends JpaRepository<AAFIRequestEntity, Long> {
    Optional<AAFIRequestEntity> findByRequestId(String requestId);
}
