package com.tax.service;

import org.springframework.stereotype.Service;

import com.tax.data.Tax;
import com.tax.data.TaxDTO;
import com.tax.repo.TaxRepository;

@Service
public class TaxServiceImpl implements TaxService {

    private final TaxRepository repository;

    public TaxServiceImpl(final TaxRepository repository){
        this.repository = repository;
    }

    @Override
    public TaxDTO createTaxDetails(Tax tax) {
            @SuppressWarnings("unused")
           Tax saved = this.repository.save(tax);
           return TaxDTO.builder().build();
    }

}
