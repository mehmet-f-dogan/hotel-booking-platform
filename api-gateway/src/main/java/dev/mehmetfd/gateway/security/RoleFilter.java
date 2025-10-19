package dev.mehmetfd.gateway.security;

import java.util.List;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import dev.mehmetfd.common.constants.Role;
import reactor.core.publisher.Mono;

@Component
public class RoleFilter implements WebFilter {
    private static List<HttpMethod> adminMethods = List.of(HttpMethod.DELETE, HttpMethod.POST, HttpMethod.PUT);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        String roleString = exchange.getRequest().getHeaders().getFirst("X-User-Role");

        Role role = null;
        try {
            Role.valueOf(roleString);
        } catch (Exception e) {
        }

        HttpMethod method = exchange.getRequest().getMethod();

        if ((path.startsWith("/api/hotels") || path.startsWith("/api/rooms"))
                && !Role.ADMIN.equals(role)
                && adminMethods.contains(method)) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }
}
