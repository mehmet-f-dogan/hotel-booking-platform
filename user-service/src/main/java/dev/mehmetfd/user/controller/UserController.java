package dev.mehmetfd.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import dev.mehmetfd.user.dto.CreateUserRequest;
import dev.mehmetfd.user.dto.UserDto;
import dev.mehmetfd.user.model.User;
import dev.mehmetfd.user.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @NotNull @RequestBody CreateUserRequest req) {
        if (userRepository.findByUsername(req.username()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        User user = new User();
        user.setUsername(req.username());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setRole(req.role());
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new UserDto(user.getId(), user.getUsername(), user.getPassword(), user.getRole()));
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserDto> getUser(@PathVariable(name = "username") String username) {
        return userRepository.findByUsername(username)
                .map(user -> ResponseEntity
                        .ok(new UserDto(user.getId(), user.getUsername(), user.getPassword(), user.getRole())))
                .orElse(ResponseEntity.notFound().build());
    }
}
