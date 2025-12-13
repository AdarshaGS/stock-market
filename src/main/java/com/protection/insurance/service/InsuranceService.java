package com.protection.insurance.service;

import java.util.List;

import com.protection.insurance.data.Insurance;

public interface InsuranceService {

    Insurance createInsurancePolicy(Insurance insurance);

    List<Insurance> getAllInsurancePolicies();

    List<Insurance> getInsurancePoliciesByUserId(Long userId);

    Insurance getInsurancePolicyById(Long id);

    void deleteInsurancePolicy(Long id);

    // Future: Coverage adequacy, reminders, etc.
}
