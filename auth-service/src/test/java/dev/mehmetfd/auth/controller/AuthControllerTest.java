package dev.mehmetfd.auth.controller;

import dev.mehmetfd.auth.dto.*;
import dev.mehmetfd.common.constants.Role;
import dev.mehmetfd.common.exception.InvalidCredentialsException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private RestTemplate restTemplate;

    private String secretKey;
    private Key key;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        secretKey = Base64.getEncoder().encodeToString(key.getEncoded());
        authController.setJwtSecret(secretKey);
    }

    @Test
    void testLoginSuccess() {
        LoginRequest req = new LoginRequest("john", "password");
        UserDto user = new UserDto(1l, "john",
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("password"), Role.USER);

        when(restTemplate.getForObject("http://USER-SERVICE/users/john", UserDto.class)).thenReturn(user);

        LoginResponse response = authController.login(req);

        assertNotNull(response);
        assertNotNull(response.accessToken());
        assertNotNull(response.refreshToken());
        assertEquals(Role.USER, response.role());
    }

    @Test
    void testLoginInvalidPasswordThrowsException() {
        LoginRequest req = new LoginRequest("john", "wrongpassword");
        UserDto user = new UserDto(1l, "john",
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("password"), Role.USER);

        when(restTemplate.getForObject("http://USER-SERVICE/users/john", UserDto.class)).thenReturn(user);

        assertThrows(InvalidCredentialsException.class, () -> authController.login(req));
    }

    @Test
    void testLoginUserNotFoundThrowsException() {
        LoginRequest req = new LoginRequest("unknown", "password");
        when(restTemplate.getForObject(anyString(), eq(UserDto.class))).thenThrow(new RuntimeException("not found"));

        assertThrows(InvalidCredentialsException.class, () -> authController.login(req));
    }

    @Test
    void testRefreshSuccess() {
        String username = "john";
        Role role = Role.USER;
        int userId = 1;

        String refreshToken = Jwts.builder()
                .setSubject(username)
                .claim("role", role.name())
                .claim("userid", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        String authHeader = "Bearer " + refreshToken;

        ResponseEntity<LoginResponse> response = authController.refresh(authHeader);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().accessToken());
        assertEquals(refreshToken, response.getBody().refreshToken());
        assertEquals(role, response.getBody().role());
    }

    @Test
    void testRefreshExpiredTokenUnauthorized() throws InterruptedException {
        String refreshToken = Jwts.builder()
                .setSubject("john")
                .claim("role", Role.USER.name())
                .claim("userid", 1)
                .setIssuedAt(new Date(System.currentTimeMillis() - 10000))
                .setExpiration(new Date(System.currentTimeMillis() - 5000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        String authHeader = "Bearer " + refreshToken;

        ResponseEntity<LoginResponse> response = authController.refresh(authHeader);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testRefreshInvalidHeader() {
        ResponseEntity<LoginResponse> response = authController.refresh("InvalidHeader");
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testCreateUserSuccess() {
        CreateUserRequest req = new CreateUserRequest("john", "pass", Role.USER);

        ResponseEntity<String> expectedResponse = ResponseEntity.status(HttpStatus.CREATED).body("User created");

        when(restTemplate.postForEntity("http://USER-SERVICE/users", req, Void.class))
                .thenReturn(ResponseEntity.ok().build());

        ResponseEntity<String> response = authController.createUser(req);

        assertEquals(expectedResponse.getStatusCode(), response.getStatusCode());
        assertEquals("User created", response.getBody());
    }

    @Test
    void testCreateUserFailure() {
        CreateUserRequest req = new CreateUserRequest("john", "pass", Role.USER);

        doThrow(new RuntimeException("Failed")).when(restTemplate)
                .postForEntity("http://USER-SERVICE/users", req, Void.class);

        ResponseEntity<String> response = authController.createUser(req);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Failed to create user", response.getBody());
    }
}
