package com.protection.insurance.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import com.protection.insurance.data.Insurance;
import com.protection.insurance.service.InsuranceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/insurance")
@RequiredArgsConstructor
@Tag(name = "Insurance", description = "Insurance Management Service")
@PreAuthorize("isAuthenticated()")
public class InsuranceApiResource {

    private final InsuranceService insuranceService;

    @PostMapping
    @Operation(summary = "Create Insurance Policy", description = "Create Insurance Policy Details for a user")
    @ApiResponse(responseCode = "201", description = "Successfully created")
    public ResponseEntity<Insurance> createInsuranceDetails(@RequestBody Insurance insurance) {
        Insurance createdInsurance = insuranceService.createInsurancePolicy(insurance);
        return new ResponseEntity<>(createdInsurance, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all insurance policies", description = "Get all insurance policies")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    public ResponseEntity<List<Insurance>> getAllInsuranceDetails() {
        return new ResponseEntity<>(insuranceService.getAllInsurancePolicies(), HttpStatus.OK);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get insurance policies by user id", description = "Get insurance policies by user id")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    public ResponseEntity<List<Insurance>> getInsuranceDetailsByUserId(@PathVariable Long userId) {
        return new ResponseEntity<>(insuranceService.getInsurancePoliciesByUserId(userId), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get insurance policy by id", description = "Get insurance policy by id")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    public ResponseEntity<Insurance> getInsuranceDetailsById(@PathVariable Long id) {
        Insurance insurance = insuranceService.getInsurancePolicyById(id);
        if (insurance != null) {
            return new ResponseEntity<>(insurance, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete insurance policy", description = "Delete insurance policy by id")
    @ApiResponse(responseCode = "204", description = "Successfully deleted")
    public ResponseEntity<Void> deleteInsurancePolicy(@PathVariable Long id) {
        insuranceService.deleteInsurancePolicy(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
