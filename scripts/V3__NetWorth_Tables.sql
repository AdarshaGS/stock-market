CREATE TABLE `user_assets` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `asset_type` varchar(50) NOT NULL,
  `reference_symbol` varchar(50) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `quantity` decimal(18,4) DEFAULT NULL,
  `current_value` decimal(18,2) NOT NULL,
  `last_updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `user_liabilities` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `liability_type` varchar(50) NOT NULL,
  `name` varchar(255) NOT NULL,
  `outstanding_amount` decimal(18,2) NOT NULL,
  `interest_rate` decimal(5,2) DEFAULT NULL,
  `due_date` date DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
