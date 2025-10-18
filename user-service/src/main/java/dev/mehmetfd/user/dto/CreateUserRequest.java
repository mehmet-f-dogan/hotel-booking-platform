package dev.mehmetfd.user.dto;

import dev.mehmetfd.common.constants.Role;

public record CreateUserRequest(String username, String password, Role role) {

}
