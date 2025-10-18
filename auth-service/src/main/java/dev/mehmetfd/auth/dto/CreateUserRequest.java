package dev.mehmetfd.auth.dto;

public record CreateUserRequest(String username, String password, String role) {
}