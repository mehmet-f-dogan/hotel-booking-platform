package dev.mehmetfd.gateway.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import dev.mehmetfd.common.constants.Role;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    private JwtAuthenticationFilter filter;
    private WebFilterChain chain;

    private String secretKey;
    private Key key;

    @BeforeEach
    void setup() {
        filter = new JwtAuthenticationFilter();
        chain = mock(WebFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        secretKey = Base64.getEncoder().encodeToString(key.getEncoded());
        filter.setJwtSecret(secretKey);
    }

    private String createToken(String userId, Role role, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userid", userId);
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Test
    void validTokenAddsHeaders() {
        String token = createToken("123", Role.ADMIN, "mehmet");
        var request = MockServerHttpRequest.get("/api/hotels")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        var exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        verify(chain).filter(any(ServerWebExchange.class));
    }

    @Test
    void missingTokenReturnsUnauthorized() {
        var request = MockServerHttpRequest.get("/api/hotels").build();
        var exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assert exchange.getResponse().getStatusCode() == HttpStatus.UNAUTHORIZED;
        verify(chain, never()).filter(exchange);
    }

    @Test
    void authPathSkipsJwtFilter() {
        var request = MockServerHttpRequest.get("/api/auth/login").build();
        var exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        verify(chain).filter(exchange);
    }
}
