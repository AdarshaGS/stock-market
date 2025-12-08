package com.stocks.networth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stocks.networth.data.NetWorthDTO;
import com.stocks.networth.service.NetWorthReadPlatformService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/networth")
@RequiredArgsConstructor
public class NetWorthController {

    private final NetWorthReadPlatformService netWorthService;

    @GetMapping("/{userId}")
    public ResponseEntity<NetWorthDTO> getNetWorth(@PathVariable Long userId) {
        NetWorthDTO netWorth = netWorthService.getNetWorth(userId);
        return ResponseEntity.ok(netWorth);
    }
}
