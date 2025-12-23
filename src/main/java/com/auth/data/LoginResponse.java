package com.auth.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private Long userId;
    private String token;
    private String refreshToken;
    private String email;
    private String name;
    private String message;
}
