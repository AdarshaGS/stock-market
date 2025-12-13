package com.protection.insurance.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.protection.insurance.data.Insurance;
import com.protection.insurance.repo.InsuranceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InsuranceServiceImpl implements InsuranceService {

    private final InsuranceRepository insuranceRepository;

    @Override
    public Insurance createInsurancePolicy(Insurance insurance) {
        return insuranceRepository.save(insurance);
    }

    @Override
    public List<Insurance> getAllInsurancePolicies() {
        return insuranceRepository.findAll();
    }

    @Override
    public List<Insurance> getInsurancePoliciesByUserId(Long userId) {
        return insuranceRepository.findByUserId(userId);
    }

    @Override
    public Insurance getInsurancePolicyById(Long id) {
        Optional<Insurance> policy = insuranceRepository.findById(id);
        return policy.orElse(null);
    }

    @Override
    public void deleteInsurancePolicy(Long id) {
        insuranceRepository.deleteById(id);
    }
}
