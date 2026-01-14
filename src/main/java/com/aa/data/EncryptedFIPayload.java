package com.aa.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EncryptedFIPayload {
    private String data; // Base64 or simple mock encryption
    private String keyId;
}
