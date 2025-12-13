package com.tax.service;

import com.tax.data.Tax;
import com.tax.data.TaxDTO;

public interface TaxService {

    TaxDTO createTaxDetails(Tax tax);
    
}
