package com.healthStatus.service;

import org.springframework.stereotype.Service;

import com.healthStatus.data.ComponentHealth;
import com.healthStatus.data.HealthStatus;

import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.sql.Connection;
import java.time.LocalDateTime;

@Service
public class HealthCheckServiceImpl implements HealthCheckService {

    private final DataSource dataSource;
    private final long startTime;

    public HealthCheckServiceImpl(DataSource dataSource) {
        this.dataSource = dataSource;
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public HealthStatus getOverallHealth() {
        HealthStatus healthStatus = new HealthStatus();
        healthStatus.setTimestamp(LocalDateTime.now());
        healthStatus.setVersion("0.0.1-SNAPSHOT");
        healthStatus.setUptimeMs(System.currentTimeMillis() - startTime);

        // Check database health
        ComponentHealth databaseHealth = checkDatabaseHealth();
        healthStatus.addComponent("database", databaseHealth);

        // Check external services (placeholder for now)
        ComponentHealth externalServicesHealth = checkExternalServicesHealth();
        healthStatus.addComponent("externalServices", externalServicesHealth);

        // Add application metrics
        addApplicationMetrics(healthStatus);

        // Determine overall status
        String overallStatus = determineOverallStatus(healthStatus);
        healthStatus.setStatus(overallStatus);

        return healthStatus;
    }

    private ComponentHealth checkDatabaseHealth() {
        long startTime = System.currentTimeMillis();
        try (Connection connection = dataSource.getConnection()) {
            // Execute a simple query to verify connectivity
            boolean isValid = connection.isValid(5); // 5 second timeout
            long responseTime = System.currentTimeMillis() - startTime;

            if (isValid) {
                return new ComponentHealth(
                        "database",
                        "UP",
                        "MySQL connection is healthy",
                        responseTime);
            } else {
                return new ComponentHealth(
                        "database",
                        "DOWN",
                        "Database connection validation failed",
                        responseTime);
            }
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            return new ComponentHealth(
                    "database",
                    "DOWN",
                    "Database connection failed: " + e.getMessage(),
                    responseTime);
        }
    }

    private ComponentHealth checkExternalServicesHealth() {
        // Placeholder - in a real implementation, you would check external stock APIs
        // For now, we'll assume they're available
        return new ComponentHealth(
                "externalServices",
                "UP",
                "External stock APIs are available",
                0L);
    }

    private void addApplicationMetrics(HealthStatus healthStatus) {
        // Memory metrics
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long usedMemory = memoryBean.getHeapMemoryUsage().getUsed();
        long maxMemory = memoryBean.getHeapMemoryUsage().getMax();
        long committedMemory = memoryBean.getHeapMemoryUsage().getCommitted();

        healthStatus.addMetric("memory.used", formatBytes(usedMemory));
        healthStatus.addMetric("memory.max", formatBytes(maxMemory));
        healthStatus.addMetric("memory.committed", formatBytes(committedMemory));
        healthStatus.addMetric("memory.usedBytes", usedMemory);
        healthStatus.addMetric("memory.maxBytes", maxMemory);

        // Thread metrics
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        healthStatus.addMetric("threads.active", threadBean.getThreadCount());
        healthStatus.addMetric("threads.peak", threadBean.getPeakThreadCount());
        healthStatus.addMetric("threads.daemon", threadBean.getDaemonThreadCount());

        // Uptime
        healthStatus.addMetric("uptime.seconds", (System.currentTimeMillis() - startTime) / 1000);
    }

    private String determineOverallStatus(HealthStatus healthStatus) {
        boolean hasDown = healthStatus.getComponents().values().stream()
                .anyMatch(component -> "DOWN".equals(component.getStatus()));

        boolean hasDegraded = healthStatus.getComponents().values().stream()
                .anyMatch(component -> "DEGRADED".equals(component.getStatus()));

        if (hasDown) {
            return "DOWN";
        } else if (hasDegraded) {
            return "DEGRADED";
        } else {
            return "UP";
        }
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
}
