package com.protection.insurance.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.protection.insurance.data.Insurance;

@Repository
public interface InsuranceRepository extends JpaRepository<Insurance, Long> {

    List<Insurance> findByUserId(Long userId);

}
