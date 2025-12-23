package com.investments.stocks.exception;

import org.springframework.http.HttpStatus;

import com.common.exception.BusinessException;

public class SymbolNotFoundException extends BusinessException {

    public SymbolNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message, "SYMBOL_NOT_FOUND");
    }

}
