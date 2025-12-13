CREATE TABLE loans (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,

    loan_type VARCHAR(50) NOT NULL,

    provider VARCHAR(100),
    loan_account_number VARCHAR(100),

    principal_amount DECIMAL(15,2),
    outstanding_amount DECIMAL(15,2),
    interest_rate DECIMAL(5,2),

    tenure_months INT,
    start_date DATE,
    end_date DATE,

    emi_amount DECIMAL(15,2),

    is_auto_fetched BOOLEAN DEFAULT FALSE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_loans_user_id (user_id),
    INDEX idx_loans_loan_type (loan_type)
);





CREATE TABLE insurance_policies (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,

    type VARCHAR(50) NOT NULL,

    policy_number VARCHAR(100),
    provider VARCHAR(100),

    premium_amount DECIMAL(15,2),
    cover_amount DECIMAL(15,2),

    start_date DATE,
    end_date DATE,
    next_premium_date DATE,

    is_auto_fetched BOOLEAN DEFAULT FALSE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_insurance_user_id (user_id),
    INDEX idx_insurance_type (type)
);


ALTER TABLE loans
ADD CONSTRAINT fk_loans_user
FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE insurance_policies
ADD CONSTRAINT fk_insurance_user
FOREIGN KEY (user_id) REFERENCES users(id);