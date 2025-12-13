package com.tax.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.tax.data.Tax;

public interface TaxRepository extends JpaRepository<Tax, Long>, JpaSpecificationExecutor<Tax>{
    
}
