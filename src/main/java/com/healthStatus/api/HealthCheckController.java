package com.healthStatus.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.healthStatus.data.HealthStatus;
import com.healthStatus.service.HealthCheckService;

@RestController
@RequestMapping("/api/health")
@Tag(name = "Health Check", description = "APIs for monitoring application health and status")
public class HealthCheckController {

    private final HealthCheckService healthCheckService;

    public HealthCheckController(HealthCheckService healthCheckService) {
        this.healthCheckService = healthCheckService;
    }

    @GetMapping
    @Operation(summary = "Get application health status", description = "Returns comprehensive health information including database connectivity, external services, and application metrics. Returns HTTP 200 for UP, 503 for DOWN/DEGRADED.")
    @ApiResponse(responseCode = "200", description = "Application is healthy")
    @ApiResponse(responseCode = "503", description = "Application is unhealthy or degraded")
    public ResponseEntity<HealthStatus> getHealth() {
        HealthStatus healthStatus = healthCheckService.getOverallHealth();

        // Return 503 if status is DOWN or DEGRADED
        if ("DOWN".equals(healthStatus.getStatus()) || "DEGRADED".equals(healthStatus.getStatus())) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(healthStatus);
        }

        return ResponseEntity.ok(healthStatus);
    }
}
