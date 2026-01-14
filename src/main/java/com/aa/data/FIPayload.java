package com.aa.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FIPayload {
    private String consentId;
    private Map<FIType, List<Object>> financialData; // Generic list of objects for different FI types
}
