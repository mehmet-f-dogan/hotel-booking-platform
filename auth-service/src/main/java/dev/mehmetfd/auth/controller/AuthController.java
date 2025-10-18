package dev.mehmetfd.auth.controller;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import dev.mehmetfd.auth.dto.CreateUserRequest;
import dev.mehmetfd.auth.dto.LoginRequest;
import dev.mehmetfd.auth.dto.LoginResponse;
import dev.mehmetfd.auth.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private final RestTemplate restTemplate;

    public AuthController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest req) {
        String url = "http://USER-SERVICE/users/" + req.username();
        System.out.println(url);
        UserDto user;
        try {
            user = restTemplate.getForObject(url, UserDto.class);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("Invalid username or password");
        }

        if (user == null || !user.password().equals(req.password())) {
            throw new RuntimeException("Invalid username or password");
        }

        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        String jwt = Jwts.builder()
                .setSubject(req.username())
                .claim("role", user.role())
                .claim("userid", user.id())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return new LoginResponse(jwt, user.role());
    }

    @PostMapping("/create")
    public ResponseEntity<String> createUser(@RequestBody CreateUserRequest req) {
        String url = "http://USER-SERVICE/users";
        try {
            restTemplate.postForEntity(url, req, Void.class);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create user");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body("User created");
    }
}
