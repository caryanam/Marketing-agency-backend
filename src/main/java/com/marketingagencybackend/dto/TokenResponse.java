package com.marketingagencybackend.dto;

public record TokenResponse(

        String token,
        long expiration,
        String tokenType,
        String userEmail,
        String userRole
) {

    public static TokenResponse of(String token, long expiration, String userEmail,String role) {
        return new TokenResponse(token, expiration, "Bearer", userEmail,role);
    }
}
