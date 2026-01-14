package com.aa.mock;

import com.aa.data.EncryptedFIPayload;
import com.aa.data.FIPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class MockEncryptionService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public EncryptedFIPayload encrypt(FIPayload payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            String encoded = Base64.getEncoder().encodeToString(json.getBytes());
            return EncryptedFIPayload.builder()
                    .data(encoded)
                    .keyId("mock-key-001")
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public FIPayload decrypt(EncryptedFIPayload encrypted) {
        try {
            byte[] decoded = Base64.getDecoder().decode(encrypted.getData());
            String json = new String(decoded);
            return objectMapper.readValue(json, FIPayload.class);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}
