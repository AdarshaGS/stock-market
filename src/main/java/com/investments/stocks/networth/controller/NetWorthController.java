package com.investments.stocks.networth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.investments.stocks.networth.data.NetWorthDTO;
import com.investments.stocks.networth.service.NetWorthReadPlatformService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/net-worth")
@Tag(name = "Net Worth Management", description = "APIs for calculating user net worth")
public class NetWorthController {

    private final NetWorthReadPlatformService netWorthReadPlatformService;

    public NetWorthController(NetWorthReadPlatformService netWorthReadPlatformService) {
        this.netWorthReadPlatformService = netWorthReadPlatformService;
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get net worth", description = "Calculates total assets, liabilities, and net worth.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved net worth")
    public NetWorthDTO getNetWorth(@PathVariable Long userId) {
        return netWorthReadPlatformService.getNetWorth(userId);
    }
}
