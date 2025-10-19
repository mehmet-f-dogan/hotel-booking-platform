package dev.mehmetfd.gateway.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import org.springframework.web.server.WebFilterChain;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RoleFilterTest {

    private RoleFilter roleFilter;
    private WebFilterChain chain;

    @BeforeEach
    void setup() {
        roleFilter = new RoleFilter();
        chain = mock(WebFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void adminCanAccessDeleteOnHotels() {
        var request = MockServerHttpRequest.delete("/api/hotels/1")
                .header("X-User-Role", "ADMIN")
                .build();
        var exchange = MockServerWebExchange.from(request);

        StepVerifier.create(roleFilter.filter(exchange, chain))
                .verifyComplete();

        verify(chain).filter(exchange);
    }

    @Test
    void nonAdminCannotDeleteHotels() {
        var request = MockServerHttpRequest.delete("/api/hotels/1")
                .header("X-User-Role", "USER")
                .build();
        var exchange = MockServerWebExchange.from(request);

        StepVerifier.create(roleFilter.filter(exchange, chain))
                .verifyComplete();

        assert exchange.getResponse().getStatusCode() == HttpStatus.FORBIDDEN;
        verify(chain, never()).filter(exchange);
    }

    @Test
    void nonAdminCanGetHotels() {
        var request = MockServerHttpRequest.get("/api/hotels/1")
                .header("X-User-Role", "USER")
                .build();
        var exchange = MockServerWebExchange.from(request);

        StepVerifier.create(roleFilter.filter(exchange, chain))
                .verifyComplete();

        verify(chain).filter(exchange);
    }
}
