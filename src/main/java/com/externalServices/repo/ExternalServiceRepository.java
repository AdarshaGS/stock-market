package com.externalServices.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.externalServices.data.ExternalServiceEntity;

@Repository
public interface ExternalServiceRepository extends JpaRepository<ExternalServiceEntity, Long> {

    ExternalServiceEntity findByServiceName(String serviceName);

}
