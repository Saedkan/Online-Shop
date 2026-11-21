package org.example.handler;

import java.util.List;

/** Who the filter says you are: the token's subject and its authorities (for example ROLE_MANAGER). */
public record MeResponse(String username, List<String> authorities) {
}
