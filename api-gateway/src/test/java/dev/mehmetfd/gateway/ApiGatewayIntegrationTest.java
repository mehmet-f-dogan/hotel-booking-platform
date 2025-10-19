package dev.mehmetfd.gateway;

import dev.mehmetfd.common.constants.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.context.ApplicationContext;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiGatewayIntegrationTest {

    @LocalServerPort
    int port;

    @Value("${jwt.secret}")
    String jwtSecret;

    WebTestClient client;

    @BeforeEach
    void setup() {
        client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }
    private String createToken(Long userId, Role role, String username) {
        byte[] decodedKey = Base64.getDecoder().decode(jwtSecret);
        Key key = Keys.hmacShaKeyFor(decodedKey);

        return Jwts.builder()
                .setSubject(username)
                .claim("userid", userId)
                .claim("role", role.name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Test
    void nonAdminCannotDeleteHotel() {
        String token = createToken(456l, Role.USER, "mehmet");

        client.delete().uri("/api/hotels/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isForbidden();

        try{
            Thread.sleep(10000);
        } catch (Exception e){}
    }
}
