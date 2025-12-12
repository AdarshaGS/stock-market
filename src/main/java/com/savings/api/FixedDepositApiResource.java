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

import com.savings.data.FixedDeposit;
import com.savings.data.FixedDepositDTO;
import com.savings.service.FixedDepositService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("api/v1/fixed-deposit")
@Tag(name = "Fixed Deposit Management", description = "APIs for managing Fixed Deposits with automatic maturity calculation")
@AllArgsConstructor
public class FixedDepositApiResource {

    private final FixedDepositService fixedDepositService;

    @PostMapping
    @Operation(summary = "Create Fixed Deposit", description = "Creates a new Fixed Deposit with automatic maturity calculation using compound interest")
    @ApiResponse(responseCode = "200", description = "Successfully created Fixed Deposit")
    public FixedDepositDTO createFixedDeposit(@RequestBody FixedDeposit fixedDeposit) {
        return fixedDepositService.createFixedDeposit(fixedDeposit);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Fixed Deposit", description = "Retrieves a specific Fixed Deposit by ID")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved Fixed Deposit")
    public FixedDepositDTO getFixedDeposit(@PathVariable Long id, @RequestParam Long userId) {
        return fixedDepositService.getFixedDeposit(id, userId);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get all Fixed Deposits for user", description = "Retrieves all Fixed Deposits for a specific user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved Fixed Deposits")
    public List<FixedDepositDTO> getAllFixedDeposits(@PathVariable Long userId) {
        return fixedDepositService.getAllFixedDeposits(userId);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Fixed Deposit", description = "Updates an existing Fixed Deposit and recalculates maturity")
    @ApiResponse(responseCode = "200", description = "Successfully updated Fixed Deposit")
    public FixedDepositDTO updateFixedDeposit(
            @PathVariable Long id,
            @RequestParam Long userId,
            @RequestBody FixedDeposit fixedDeposit) {
        return fixedDepositService.updateFixedDeposit(id, userId, fixedDeposit);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Fixed Deposit", description = "Deletes a Fixed Deposit")
    @ApiResponse(responseCode = "200", description = "Successfully deleted Fixed Deposit")
    public void deleteFixedDeposit(@PathVariable Long id, @RequestParam Long userId) {
        fixedDepositService.deleteFixedDeposit(id, userId);
    }
}
