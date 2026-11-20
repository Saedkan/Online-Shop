package org.example.handler;

/** Successful login body. The password is never included. */
public record TokenResponse(String accessToken, String tokenType, long expiresIn) {
}
