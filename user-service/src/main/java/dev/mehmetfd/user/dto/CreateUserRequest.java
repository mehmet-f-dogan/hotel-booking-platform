package dev.mehmetfd.user.dto;

import dev.mehmetfd.common.constants.Role;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(@NotBlank String username, @NotBlank String password, Role role) {

}
