package com.tax.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tax.data.Tax;
import com.tax.data.TaxDTO;
import com.tax.service.TaxService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@Tag(name = "Income Tax Management")
@RequestMapping("api/v1/tax")
@RequiredArgsConstructor
public class TaxAPIResource {

    private final TaxService taxService;

    @PostMapping
    public TaxDTO createTaxDetails(@RequestBody Tax tax){
        return this.taxService.createTaxDetails(tax);
    }
}
