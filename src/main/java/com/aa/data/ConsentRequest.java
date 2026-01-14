package com.aa.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsentRequest {
    @JsonProperty("userId")
    private String userId;

    @JsonProperty("consentTemplateId")
    private String consentTemplateId;

    @JsonProperty("fiTypes")
    private List<FIType> fiTypes;

    @JsonProperty("purpose")
    private String purpose;

    @JsonProperty("dataFrom")
    private String validFrom;

    @JsonProperty("dataTo")
    private String validTill;

    @JsonProperty("frequency")
    private String frequency;

    @JsonProperty("expiry")
    private String expiry;
}
