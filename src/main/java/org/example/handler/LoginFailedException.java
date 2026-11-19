package org.example.handler;

/** Wrong username or wrong password — one message for both. */
public class LoginFailedException extends RuntimeException {

    public LoginFailedException() {
        super("bad username or password");
    }
}
