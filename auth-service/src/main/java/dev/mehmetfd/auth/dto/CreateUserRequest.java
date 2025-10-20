package dev.mehmetfd.auth.dto;

import dev.mehmetfd.common.constants.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUserRequest(@NotBlank String username, @NotBlank String password, @NotNull Role role) {
}