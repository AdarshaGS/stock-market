package com.externalServices.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.externalServices.service.ExternalService;
import com.externalServices.data.ExternalServicePropertiesEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/external-services")
@Tag(name = "External Services", description = "APIs for fetching external service configurations")
public class ExternalServiceAPIResource {

    private final ExternalService externalService;

    public ExternalServiceAPIResource(final ExternalService externalService) {
        this.externalService = externalService;
    }

    @GetMapping("/{serviceName}")
    @Operation(summary = "Get external service properties", description = "Fetches configuration properties for a given external service.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved service properties")
    public List<ExternalServicePropertiesEntity> getExternalService(@PathVariable String serviceName) {
        return this.externalService.getExternalServicePropertiesByServiceName(serviceName);
    }
}
