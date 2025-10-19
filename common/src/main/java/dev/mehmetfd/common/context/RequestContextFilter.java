package dev.mehmetfd.common.context;

import java.io.IOException;

import org.springframework.web.filter.OncePerRequestFilter;

import dev.mehmetfd.common.constants.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class RequestContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String userId = request.getHeader("X-USER-ID");
            String username = request.getHeader("X-USERNAME");
            String role = request.getHeader("X-USER-ROLE");

            if (userId != null || username != null || role != null) {
                RequestContextHolder.set(new RequestContext(userId, username, Role.valueOf(role)));
            }

            filterChain.doFilter(request, response);

        } finally {
            RequestContextHolder.clear();
        }
    }
}