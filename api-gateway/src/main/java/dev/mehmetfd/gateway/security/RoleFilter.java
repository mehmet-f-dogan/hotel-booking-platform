package dev.mehmetfd.gateway.security;

import java.util.List;

import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import dev.mehmetfd.common.constants.Role;
import reactor.core.publisher.Mono;

@Component
public class RoleFilter implements WebFilter, Ordered {
    private static List<HttpMethod> adminMethods = List.of(HttpMethod.DELETE, HttpMethod.POST, HttpMethod.PUT);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        String roleString = exchange.getRequest().getHeaders().getFirst("X-User-Role");

        Role role = null;
        try {
            role = Role.valueOf(roleString);
        } catch (Exception e) {
        }

        HttpMethod method = exchange.getRequest().getMethod();

        if ((path.startsWith("/api/hotels") || path.startsWith("/api/rooms"))
                && adminMethods.contains(method)) {
            if (Role.ADMIN.equals(role)) {
                return chain.filter(exchange);
            } else {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
