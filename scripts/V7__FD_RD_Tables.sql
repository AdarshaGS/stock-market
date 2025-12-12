-- Fixed Deposit table
CREATE TABLE IF NOT EXISTS `fixed_deposits` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `bank_name` varchar(255) NOT NULL,
  `account_number` varchar(100) DEFAULT NULL,
  `principal_amount` DECIMAL(19,2) NOT NULL,
  `interest_rate` DECIMAL(5,2) NOT NULL,
  `tenure_months` int NOT NULL,
  `maturity_amount` DECIMAL(19,2) DEFAULT NULL,
  `start_date` DATE NOT NULL,
  `maturity_date` DATE NOT NULL,
  `status` varchar(50) DEFAULT 'ACTIVE',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `fixed_deposits_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Recurring Deposit table
CREATE TABLE IF NOT EXISTS `recurring_deposits` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `bank_name` varchar(255) NOT NULL,
  `account_number` varchar(100) DEFAULT NULL,
  `monthly_installment` DECIMAL(19,2) NOT NULL,
  `interest_rate` DECIMAL(5,2) NOT NULL,
  `tenure_months` int NOT NULL,
  `maturity_amount` DECIMAL(19,2) DEFAULT NULL,
  `start_date` DATE NOT NULL,
  `maturity_date` DATE NOT NULL,
  `status` varchar(50) DEFAULT 'ACTIVE',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `recurring_deposits_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Add unique constraint to savings accounts to prevent duplicates (only if it doesn't exist)
-- First, remove any duplicate entries if they exist
DELETE t1 FROM savings_account_details t1
INNER JOIN savings_account_details t2 
WHERE t1.id > t2.id 
AND t1.user_id = t2.user_id 
AND t1.bank_name = t2.bank_name;

-- Now add the unique constraint
ALTER TABLE `savings_account_details` 
ADD UNIQUE KEY `unique_user_bank` (`user_id`, `bank_name`);
