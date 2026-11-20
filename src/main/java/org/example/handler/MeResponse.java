package org.example.handler;

import java.util.List;

/** Who the Bearer token says you are for this request. */
public record MeResponse(String username, List<String> authorities) {
}
