package com.healthStatus.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComponentHealth {

    private String name;
    private String status; // UP, DOWN, DEGRADED
    private String details;
    private Long responseTimeMs;

    public ComponentHealth(String name, String status) {
        this.name = name;
        this.status = status;
        this.details = "";
        this.responseTimeMs = 0L;
    }

    public ComponentHealth(String name, String status, String details) {
        this.name = name;
        this.status = status;
        this.details = details;
        this.responseTimeMs = 0L;
    }
}
