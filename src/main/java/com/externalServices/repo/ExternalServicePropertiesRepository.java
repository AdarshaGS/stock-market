package com.externalServices.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.externalServices.data.ExternalServicePropertiesEntity;

public interface ExternalServicePropertiesRepository extends JpaRepository<ExternalServicePropertiesEntity, Long> {

    List<ExternalServicePropertiesEntity> findByExternalServiceId(Long externalServiceId);
}
