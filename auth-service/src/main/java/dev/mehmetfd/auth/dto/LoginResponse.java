package dev.mehmetfd.auth.dto;

import dev.mehmetfd.common.constants.Role;

public record LoginResponse(String token, Role role) {
}