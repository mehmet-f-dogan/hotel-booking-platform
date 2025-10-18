package dev.mehmetfd.gateway.security;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import dev.mehmetfd.common.constants.Role;
import reactor.core.publisher.Mono;

@Component
public class RoleFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        String roleString = exchange.getRequest().getHeaders().getFirst("X-User-Role");

        Role role = Role.valueOf(roleString);

        if (path.startsWith("/api/hotels/create") && !Role.ADMIN.equals(role)) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }
}
