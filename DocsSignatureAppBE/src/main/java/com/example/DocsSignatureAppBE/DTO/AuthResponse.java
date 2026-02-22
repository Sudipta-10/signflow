package com.example.DocsSignatureAppBE.DTO;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private String role;
    private String refreshToken;

    public AuthResponse() {}

    public AuthResponse(String token) {
        this.token = token;
        this.role = "ROLE_USER";
    }

    public AuthResponse(String token, String role) {
        this.token = token;
        this.role = role;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}


