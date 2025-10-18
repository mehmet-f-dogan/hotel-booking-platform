package dev.mehmetfd.user.dto;

import dev.mehmetfd.common.constants.Role;

public record UserDto(Long id, String username, String password, Role role) {

}
