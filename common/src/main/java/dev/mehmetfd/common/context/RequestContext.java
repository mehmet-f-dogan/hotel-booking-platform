package dev.mehmetfd.common.context;

import dev.mehmetfd.common.constants.Role;

public record RequestContext(
        String userId,
        String username,
        Role role) {
}