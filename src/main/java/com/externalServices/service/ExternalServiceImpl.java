package com.externalServices.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.externalServices.data.ExternalServiceEntity;
import com.externalServices.data.ExternalServicePropertiesEntity;
import com.externalServices.repo.ExternalServicePropertiesRepository;
import com.externalServices.repo.ExternalServiceRepository;

@Service
public class ExternalServiceImpl implements ExternalService {

    private final ExternalServiceRepository externalServiceRepository;
    private final ExternalServicePropertiesRepository externalServicePropertiesRepository;

    public ExternalServiceImpl(ExternalServiceRepository externalServiceRepository,
            ExternalServicePropertiesRepository externalServicePropertiesRepository) {
        this.externalServiceRepository = externalServiceRepository;
        this.externalServicePropertiesRepository = externalServicePropertiesRepository;
    }

    @Override
    public List<ExternalServicePropertiesEntity> getExternalServicePropertiesByServiceName(String serviceName) {
        List<ExternalServicePropertiesEntity> externalServiceProperties = null;
        ExternalServiceEntity entity = externalServiceRepository.findByServiceName(serviceName);
        if (entity != null) {
            externalServiceProperties = this.externalServicePropertiesRepository
                    .findByExternalServiceId(entity.getId());
        }
        return externalServiceProperties;
    }

}
