CREATE TABLE `lending_records` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `borrower_name` VARCHAR(255) NOT NULL,
    `borrower_contact` VARCHAR(50),
    `amount_lent` DECIMAL(19, 4) NOT NULL,
    `amount_repaid` DECIMAL(19, 4) DEFAULT 0,
    `outstanding_amount` DECIMAL(19, 4) NOT NULL,
    `date_lent` DATE NOT NULL,
    `due_date` DATE,
    `status` VARCHAR(50) NOT NULL,
    `notes` TEXT,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE `lending_repayments` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `lending_id` BIGINT NOT NULL,
    `amount` DECIMAL(19, 4) NOT NULL,
    `repayment_date` DATE NOT NULL,
    `repayment_method` VARCHAR(50) NOT NULL,
    `notes` TEXT,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_lending_record` FOREIGN KEY (`lending_id`) REFERENCES `lending_records`(`id`) ON DELETE CASCADE
);

ALTER TABLE `lending_records`
	ADD FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON
			UPDATE
				CASCADE ON DELETE CASCADE;