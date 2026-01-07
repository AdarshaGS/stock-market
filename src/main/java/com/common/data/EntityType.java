package com.common.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EntityType {
    STOCK(1, "Stock"),
    MUTUAL_FUND(2, "Mutual Fund"),
    ETF(3, "ETF"),
    CASH(4, "Cash"),
    LENDING(5, "Lending"),
    SAVINGS(6, "Savings"),
    SAVINGS_ACCOUNT(7, "Savings Account"),
    FIXED_DEPOSIT(8, "Fixed Deposit"),
    RECURRING_DEPOSIT(9, "Recurring Deposit"),
    LOAN(10, "Loan"),
    INSURANCE(11, "Insurance"),
    REAL_ESTATE(12, "Real Estate"),
    GOLD(13, "Gold"),
    HOME_LOAN(14, "Home Loan"),
    PERSONAL_LOAN(15, "Personal Loan"),
    CAR_LOAN(16, "Car Loan"),
    EDUCATION_LOAN(17, "Education Loan"),
    CREDIT_CARD(18, "Credit Card"),
    BNPL(19, "BNPL"),
    PF(20, "Provident Fund"),
    LIFE_INSURANCE(21, "Life Insurance"),
    HEALTH_INSURANCE(22, "Health Insurance"),
    VEHICLE_INSURANCE(23, "Vehicle Insurance"),
    OTHER(24, "Other");

    private final int id;
    private final String name;

    EntityType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    @JsonValue
    public String getValue() {
        return name();
    }

    public String getName() {
        return name;
    }

    @JsonCreator
    public static EntityType fromValue(Object value) {
        if (value == null) {
            return OTHER;
        }
        if (value instanceof Integer) {
            return fromId((Integer) value);
        }
        if (value instanceof String) {
            String strValue = (String) value;
            try {
                return EntityType.valueOf(strValue);
            } catch (IllegalArgumentException e) {
                try {
                    return fromId(Integer.parseInt(strValue));
                } catch (NumberFormatException nfe) {
                    return OTHER;
                }
            }
        }
        return OTHER;
    }

    public static EntityType fromId(int id) {
        for (EntityType type : EntityType.values()) {
            if (type.id == id) {
                return type;
            }
        }
        return OTHER;
    }
}
