package com.loan.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.loan.data.Loan;
import com.loan.service.LoanService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
@Tag(name = "Loan", description = "Loan Management Service")
public class LoanApiResource {

    private final LoanService loanService;

    @PostMapping("/create")
    @Operation(summary = "Create Loan", description = "Create Loan Details of a user")
    @ApiResponse(responseCode = "200", description = "Successfully created")
    public Loan createLoan(@RequestBody Loan loan) {
        return loanService.createLoan(loan);
    }

    @GetMapping("/all")
    @Operation(summary = "Get all loans", description = "Get all loans")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    public ResponseEntity<List<Loan>> getAllLoans() {
        return new ResponseEntity<>(loanService.getAllLoans(), HttpStatus.OK);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get loans by user id", description = "Get loans by user id")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    public ResponseEntity<List<Loan>> getLoansByUserId(@PathVariable Long userId) {
        return new ResponseEntity<>(loanService.getLoansByUserId(userId), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "User login", description = "Authenticate user with email and password, returns JWT token")
    @ApiResponse(responseCode = "200", description = "Successfully authenticated")
    public ResponseEntity<Loan> getLoanById(@PathVariable Long id) {
        Loan loan = loanService.getLoanById(id);
        if (loan != null) {
            return new ResponseEntity<>(loan, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "User login", description = "Authenticate user with email and password, returns JWT token")
    @ApiResponse(responseCode = "200", description = "Successfully authenticated")
    public ResponseEntity<Void> deleteLoan(@PathVariable Long id) {
        loanService.deleteLoan(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{id}/simulate-prepayment")
    @Operation(summary = "User login", description = "Authenticate user with email and password, returns JWT token")
    @ApiResponse(responseCode = "200", description = "Successfully authenticated")
    public ResponseEntity<Map<String, Object>> simulatePrepayment(
            @PathVariable Long id,
            @RequestParam BigDecimal amount) {
        Map<String, Object> simulation = loanService.simulatePrepayment(id, amount);
        return new ResponseEntity<>(simulation, HttpStatus.OK);
    }
}
