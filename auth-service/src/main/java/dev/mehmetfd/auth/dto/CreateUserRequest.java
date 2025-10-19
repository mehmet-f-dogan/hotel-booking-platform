package dev.mehmetfd.auth.dto;

import dev.mehmetfd.common.constants.Role;

public record CreateUserRequest(String username, String password, Role role) {
}