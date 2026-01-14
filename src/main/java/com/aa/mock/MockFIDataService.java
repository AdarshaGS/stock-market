package com.aa.mock;

import com.aa.data.FIType;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class MockFIDataService {

        public Map<FIType, List<Object>> generateMockData(List<FIType> requestedTypes) {
                Map<FIType, List<Object>> data = new HashMap<>();

                for (FIType type : requestedTypes) {
                        switch (type) {
                                case MUTUAL_FUNDS -> data.put(type, generateMFData());
                                case BANK_ACCOUNTS -> data.put(type, generateBankAccounts());
                                case BANK_TRANSACTIONS -> data.put(type, generateTransactions());
                                case LOANS -> data.put(type, generateLoans());
                                case INSURANCE -> data.put(type, generateInsurance());
                                case TAX_INVESTMENTS -> data.put(type, generateTaxInvestments());
                                default -> data.put(type, Collections.emptyList());
                        }
                }
                return data;
        }

        private List<Object> generateMFData() {
                return List.of(
                                Map.of(
                                                "amc", "HDFC Mutual Fund",
                                                "scheme", "HDFC Top 100 Fund - Direct Growth",
                                                "folio", "11223344",
                                                "isin", "INF179K01BE2",
                                                "units", 100.5,
                                                "nav", 500.0,
                                                "currentValue", 50250.0,
                                                "costValue", 45000.0,
                                                "transactions", List.of(
                                                                Map.of("date", "2024-01-01", "type", "BUY", "amount",
                                                                                45000.0, "units", 100.5, "nav",
                                                                                447.76))),
                                Map.of(
                                                "amc", "ICICI Prudential",
                                                "scheme", "ICICI Pru Bluechip Fund",
                                                "folio", "55667788",
                                                "isin", "INF109K01Z48",
                                                "units", 50.0,
                                                "nav", 80.0,
                                                "currentValue", 4000.0,
                                                "costValue", 3500.0,
                                                "transactions", Collections.emptyList()));
        }

        private List<Object> generateBankAccounts() {
                return List.of(
                                Map.of(
                                                "accountNumber", "XXXXXX1234",
                                                "bankName", "HDFC Bank",
                                                "accountType", "SAVINGS",
                                                "balance", 125000.50,
                                                "currency", "INR",
                                                "ifsc", "HDFC0001234"),
                                Map.of(
                                                "accountNumber", "XXXXXX5678",
                                                "bankName", "ICICI Bank",
                                                "accountType", "SAVINGS",
                                                "balance", 45000.00,
                                                "currency", "INR",
                                                "ifsc", "ICIC0005678"));
        }

        private List<Object> generateTransactions() {
                return List.of(
                                Map.of("date", LocalDate.now().minusDays(2).toString(), "amount", 1200.00, "type",
                                                "DEBIT",
                                                "description", "Zomato", "category", "FOOD"),
                                Map.of("date", LocalDate.now().minusDays(5).toString(), "amount", 45000.00, "type",
                                                "CREDIT",
                                                "description", "Salary", "category", "INCOME"),
                                Map.of("date", LocalDate.now().minusDays(10).toString(), "amount", 2500.00, "type",
                                                "DEBIT",
                                                "description", "Amazon", "category", "SHOPPING"));
        }

        private List<Object> generateLoans() {
                return List.of(
                                Map.of(
                                                "loanType", "HOME_LOAN",
                                                "outstandingAmount", 4500000.00,
                                                "totalLoanAmount", 5000000.00,
                                                "emi", 45000.00,
                                                "interestRate", 8.5,
                                                "tenureMonths", 240));
        }

        private List<Object> generateInsurance() {
                return List.of(
                                Map.of(
                                                "policyType", "LIFE_INSURANCE",
                                                "policyName", "HDFC Life Click 2 Protect",
                                                "coverageAmount", 10000000.00,
                                                "premiumAmount", 15000.00,
                                                "expiryDate", LocalDate.now().plusYears(20).toString()));
        }

        private List<Object> generateTaxInvestments() {
                return List.of(
                                Map.of(
                                                "financialYear", "2024-25",
                                                "totalInvestments", 150000.00,
                                                "80C_Investments", List.of(
                                                                Map.of("type", "ELSS", "amount", 50000.00),
                                                                Map.of("type", "PPF", "amount", 100000.00))));
        }
}
