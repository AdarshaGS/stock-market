package com.healthStatus.data;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
public class HealthStatus {

    private String status; // UP, DOWN, DEGRADED
    private LocalDateTime timestamp;
    private String version;
    private Long uptimeMs;
    private Map<String, ComponentHealth> components;
    private Map<String, Object> metrics;

    public HealthStatus() {
        this.components = new HashMap<>();
        this.metrics = new HashMap<>();
        this.timestamp = LocalDateTime.now();
    }

    public void addComponent(String key, ComponentHealth component) {
        if (this.components == null) {
            this.components = new HashMap<>();
        }
        this.components.put(key, component);
    }

    public void addMetric(String key, Object value) {
        if (this.metrics == null) {
            this.metrics = new HashMap<>();
        }
        this.metrics.put(key, value);
    }
}
