package org.example.handler;

/** accessToken is the JWT, tokenType is always "Bearer", expiresIn is in seconds (900 = 15 minutes). */
public record LoginResponse(String accessToken, String tokenType, long expiresIn) {
    @Override
    public String toString() {
        return "LoginResponse[tokenType=" + tokenType + ", expiresIn=" + expiresIn + "]"; // no token
    }
}