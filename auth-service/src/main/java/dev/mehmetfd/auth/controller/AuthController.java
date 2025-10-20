package dev.mehmetfd.auth.controller;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import dev.mehmetfd.auth.dto.CreateUserRequest;
import dev.mehmetfd.auth.dto.LoginRequest;
import dev.mehmetfd.auth.dto.LoginResponse;
import dev.mehmetfd.auth.dto.UserDto;
import dev.mehmetfd.common.constants.Role;
import dev.mehmetfd.common.exception.InvalidCredentialsException;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Value("${jwt.secret}")
    private String jwtSecret;

    public void setJwtSecret(String secret) {
        this.jwtSecret = secret;
    }

    @Autowired
    private RestTemplate restTemplate;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid @NotNull LoginRequest req) {
        String url = "http://USER-SERVICE/users/" + req.username();
        UserDto user;
        try {
            user = restTemplate.getForObject(url, UserDto.class);
        } catch (Exception e) {
            throw new InvalidCredentialsException();
        }

        if (user == null || !passwordEncoder.matches(req.password(), user.password())) {
            throw new InvalidCredentialsException();
        }

        byte[] decodedKey = Base64.getDecoder().decode(jwtSecret);
        Key key = Keys.hmacShaKeyFor(decodedKey);

        Date now = new Date();

        String accessToken = Jwts.builder()
                .setSubject(req.username())
                .claim("role", user.role())
                .claim("userid", user.id())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + 1000 * 60 * 60))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        String refreshToken = Jwts.builder()
                .setSubject(req.username())
                .claim("role", user.role())
                .claim("userid", user.id())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + 1000L * 60 * 60 * 24 * 7))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return new LoginResponse(accessToken, refreshToken, user.role());
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String refreshToken = authHeader.substring(7);

            byte[] decodedKey = Base64.getDecoder().decode(jwtSecret);
            Key key = Keys.hmacShaKeyFor(decodedKey);

            var claimsJws = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(refreshToken);

            var claims = claimsJws.getBody();

            if (claims.getExpiration().before(new Date())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String username = claims.getSubject();
            String role = (String) claims.get("role");
            Integer userId = (Integer) claims.get("userid");

            String newAccessToken = Jwts.builder()
                    .setSubject(username)
                    .claim("role", role)
                    .claim("userid", userId)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                    .signWith(key, SignatureAlgorithm.HS256)
                    .compact();

            return ResponseEntity.ok(new LoginResponse(newAccessToken, refreshToken, Role.valueOf(role)));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/create")
    public ResponseEntity<String> createUser(@RequestBody @Valid @NotNull CreateUserRequest req) {
        String url = "http://USER-SERVICE/users";
        try {
            restTemplate.postForEntity(url, req, Void.class);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to create user");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body("User created");
    }
}
