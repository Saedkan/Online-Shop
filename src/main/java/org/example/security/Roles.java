package org.example.security;

/**
 * Role names without the ROLE_ prefix. Spring adds the prefix itself:
 * hasRole("MANAGER") checks the authority ROLE_MANAGER.
 */
public final class Roles {

    /** Junior role: creates and reads orders. */
    public static final String CLERK = "CLERK";

    /** Senior role: may also run the locked action (confirm payment). */
    public static final String MANAGER = "MANAGER";

    private Roles() {
    }
}
