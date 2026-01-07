-- Fix stocks description truncation issue
ALTER TABLE stocks MODIFY COLUMN description TEXT;

-- Remove redundant/incompatible foreign key if it exists
-- This one seems to be a leftover from Hibernate auto-generation
SET @constraint_name = (
    SELECT CONSTRAINT_NAME
    FROM information_schema.KEY_COLUMN_USAGE
    WHERE TABLE_NAME = 'external_service_properties'
    AND CONSTRAINT_NAME = 'fk_external_service_properties_services'
    AND TABLE_SCHEMA = DATABASE()
    LIMIT 1
);

SET @query = IF(@constraint_name IS NOT NULL,
    'ALTER TABLE external_service_properties DROP FOREIGN KEY fk_external_service_properties_services',
    'SELECT "No redundant constraint found"'
);

PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Ensure sector IDs are BIGINT to match Stock entity expectations
ALTER TABLE sectors MODIFY COLUMN id BIGINT AUTO_INCREMENT;
ALTER TABLE stocks MODIFY COLUMN sector_id BIGINT;

-- Ensure external_service_id has the correct type and constraint (from V11)
-- V11 used fk_esp_es, let's make sure it's consistent
ALTER TABLE external_service_properties 
MODIFY COLUMN external_service_id BIGINT NOT NULL;
