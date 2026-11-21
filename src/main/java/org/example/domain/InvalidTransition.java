package org.example.domain;

/**
 * The status policy forbids this move (for example paying an order that is already PAID).
 * Unchecked, plain Java. The web advice turns it into 409.
 */
public class InvalidTransition extends RuntimeException {
    public InvalidTransition(String message) {
        super(message);
    }
}
