package com.savings.api;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.savings.data.RecurringDeposit;
import com.savings.data.RecurringDepositDTO;
import com.savings.service.RecurringDepositService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("api/v1/recurring-deposit")
@Tag(name = "Recurring Deposit Management", description = "APIs for managing Recurring Deposits with automatic maturity calculation")
@AllArgsConstructor
public class RecurringDepositApiResource {

    private final RecurringDepositService recurringDepositService;

    @PostMapping
    @Operation(summary = "Create Recurring Deposit", description = "Creates a new Recurring Deposit with automatic maturity calculation")
    @ApiResponse(responseCode = "200", description = "Successfully created Recurring Deposit")
    public RecurringDepositDTO createRecurringDeposit(@RequestBody RecurringDeposit recurringDeposit) {
        return recurringDepositService.createRecurringDeposit(recurringDeposit);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Recurring Deposit", description = "Retrieves a specific Recurring Deposit by ID")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved Recurring Deposit")
    public RecurringDepositDTO getRecurringDeposit(@PathVariable Long id, @RequestParam Long userId) {
        return recurringDepositService.getRecurringDeposit(id, userId);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get all Recurring Deposits for user", description = "Retrieves all Recurring Deposits for a specific user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved Recurring Deposits")
    public List<RecurringDepositDTO> getAllRecurringDeposits(@PathVariable Long userId) {
        return recurringDepositService.getAllRecurringDeposits(userId);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Recurring Deposit", description = "Updates an existing Recurring Deposit and recalculates maturity")
    @ApiResponse(responseCode = "200", description = "Successfully updated Recurring Deposit")
    public RecurringDepositDTO updateRecurringDeposit(
            @PathVariable Long id,
            @RequestParam Long userId,
            @RequestBody RecurringDeposit recurringDeposit) {
        return recurringDepositService.updateRecurringDeposit(id, userId, recurringDeposit);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Recurring Deposit", description = "Deletes a Recurring Deposit")
    @ApiResponse(responseCode = "200", description = "Successfully deleted Recurring Deposit")
    public void deleteRecurringDeposit(@PathVariable Long id, @RequestParam Long userId) {
        recurringDepositService.deleteRecurringDeposit(id, userId);
    }
}
