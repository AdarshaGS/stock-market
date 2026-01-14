CREATE TABLE aa_consents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    consent_id VARCHAR(100) UNIQUE,
    consent_template_id VARCHAR(100) UNIQUE,
    user_id VARCHAR(200),
    status VARCHAR(20),
    fi_types TEXT,
    valid_from VARCHAR(50),
    valid_till VARCHAR(50)
);

CREATE TABLE aa_fi_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id VARCHAR(100) UNIQUE,
    consent_id VARCHAR(100),
    status VARCHAR(50),
    encrypted_data TEXT
);

INSERT IGNORE INTO `external_service_properties`
(`id`, `name`, `value`, `external_service_id`)
VALUES
(
  DEFAULT,
  'consent-url',
  'http://localhost:8081/aa/consents',
  (SELECT id FROM external_service WHERE name = 'ACCOUNT_AGGREGATOR')
);

INSERT IGNORE INTO `external_service_properties`
(`id`, `name`, `value`, `external_service_id`)
VALUES
(
  DEFAULT,
  'consent-url-fetch',
  'http://localhost:8081/aa/consents',
  (SELECT id FROM external_service WHERE name = 'ACCOUNT_AGGREGATOR')
);

INSERT IGNORE INTO `external_service_properties`
(`id`, `name`, `value`, `external_service_id`)
VALUES
(
  DEFAULT,
  'aa-data-fetch',
  'http://localhost:8081/aa/fetch',
  (SELECT id FROM external_service WHERE name = 'ACCOUNT_AGGREGATOR')
);