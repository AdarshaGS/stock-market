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

import com.protection.insurance.data.Insurance;
import com.protection.insurance.service.InsuranceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/insurance")
@RequiredArgsConstructor
public class InsuranceApiResource {

    private final InsuranceService insuranceService;

    @PostMapping
    public ResponseEntity<Insurance> createInsuranceDetails(@RequestBody Insurance insurance) {
        Insurance createdInsurance = insuranceService.createInsurancePolicy(insurance);
        return new ResponseEntity<>(createdInsurance, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Insurance>> getAllInsuranceDetails() {
        return new ResponseEntity<>(insuranceService.getAllInsurancePolicies(), HttpStatus.OK);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Insurance>> getInsuranceDetailsByUserId(@PathVariable Long userId) {
        return new ResponseEntity<>(insuranceService.getInsurancePoliciesByUserId(userId), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Insurance> getInsuranceDetailsById(@PathVariable Long id) {
        Insurance insurance = insuranceService.getInsurancePolicyById(id);
        if (insurance != null) {
            return new ResponseEntity<>(insurance, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInsurancePolicy(@PathVariable Long id) {
        insuranceService.deleteInsurancePolicy(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
