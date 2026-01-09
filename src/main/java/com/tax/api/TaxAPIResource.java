package com.tax.api;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import com.tax.data.Tax;
import com.tax.data.TaxDTO;
import com.tax.service.TaxService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import java.math.BigDecimal;

@RestController
@Tag(name = "Income Tax Management")
@RequestMapping("api/v1/tax")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class TaxAPIResource {
    private final TaxService taxService;

    @PostMapping
    @Operation(summary = "Create tax details")
    public TaxDTO createTaxDetails(@RequestBody Tax tax) {
        return this.taxService.createTaxDetails(tax);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get tax details by user and financial year")
    public TaxDTO getTaxDetails(@PathVariable Long userId, @RequestParam String financialYear) {
        return this.taxService.getTaxDetailsByUserId(userId, financialYear);
    }

    @GetMapping("/{userId}/liability")
    @Operation(summary = "Get outstanding tax liability")
    public BigDecimal getOutstandingTaxLiability(@PathVariable Long userId) {
        return this.taxService.getOutstandingTaxLiability(userId);
    }
}