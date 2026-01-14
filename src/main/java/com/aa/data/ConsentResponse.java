package com.aa.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsentResponse {
    private String consentId;
    private String userConsentId;
    private ConsentStatus status;
    private List<FIType> fiTypes;
    private String validFrom;
    private String validTill;
    private String redirectUrl;
}
