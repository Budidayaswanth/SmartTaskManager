package com.smarttask.smarttask_backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.smarttask.smarttask_backend.service.UserDetailsServiceImpl;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * ✅ JwtAuthFilter
 * - Intercepts each request once (OncePerRequestFilter)
 * - Extracts and validates JWT from Authorization header
 * - Sets the authenticated user in SecurityContext
 * - Skips Swagger and public endpoints
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;
    @Value("${swagger.auth.username}")
    private String swaggerUsername;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();

        // 🔓 Skip public & Swagger endpoints
        if (isPublicEndpoint(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 🔒 Extract JWT token
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);

        try {
            Claims claims = jwtService.getClaims(jwt);
            String username = claims.getSubject();

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (isSwaggerToken(username)) {
                    if (!jwtService.isTokenExpired(jwt)) {
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        username,
                                        null,
                                        buildSwaggerAuthorities(claims));
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        log.debug("✅ Authenticated Swagger user");
                    } else {
                        log.warn("⚠️ Swagger JWT token expired");
                    }
                } else {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (jwtService.isTokenValid(jwt, userDetails)) {
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        log.debug("✅ Authenticated user: {}", username);
                    } else {
                        log.warn("⚠️ Invalid JWT token for user: {}", username);
                    }
                }
            }
        } catch (UsernameNotFoundException e) {
            log.warn("⚠️ JWT user not found: {}", e.getMessage());
        } catch (Exception e) {
            log.error("❌ JWT validation failed: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Helper to identify routes that don’t need JWT authentication.
     */
    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/api/auth/swagger-login") ||
                path.startsWith("/api/auth/login") ||
                path.startsWith("/api/auth/register") ||
                path.startsWith("/swagger") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v1/api-docs") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/error") ||
                path.startsWith("/actuator/health");
    }

    private boolean isSwaggerToken(String username) {
        return swaggerUsername != null && swaggerUsername.equals(username);
    }

    private Collection<SimpleGrantedAuthority> buildSwaggerAuthorities(Claims claims) {
        String role = claims.get("role", String.class);
        if (role == null || role.isBlank()) {
            role = "SWAGGER_ADMIN";
        }
        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }
        return List.of(new SimpleGrantedAuthority(role));
    }
}
