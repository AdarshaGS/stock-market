package com.stocks.networth.service;

import com.stocks.networth.data.NetWorthDTO;

public interface NetWorthReadPlatformService {
    NetWorthDTO getNetWorth(Long userId);
}
