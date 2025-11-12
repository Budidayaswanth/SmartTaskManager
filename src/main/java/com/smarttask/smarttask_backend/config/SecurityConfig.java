package com.smarttask.smarttask_backend.config;

import com.smarttask.smarttask_backend.security.JwtAuthFilter;
import com.smarttask.smarttask_backend.security.JwtService;
import com.smarttask.smarttask_backend.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

/**
 * 🔐 Security Configuration
 * WHY:
 * - Stateless API (JWT)
 * - Enable CORS for Flutter & Swagger
 * - Permit Swagger + Auth endpoints
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl uds;
    private final JwtService jwt;

    /** Password hashing with BCrypt (safe for production) */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Authentication Provider connects Spring Security with our custom UserDetailsService */
    @Bean
    AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(uds);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /** Manages authentication during login */
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    /** JWT Filter to intercept every request and validate token */
    @Bean
    JwtAuthFilter jwtAuthFilter() {
        return new JwtAuthFilter(jwt, uds);
    }

    /** Main HTTP Security Configuration */
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF (JWT-based app)
                .csrf(csrf -> csrf.disable())

                // Stateless sessions (no cookies)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Enable CORS (so Flutter & Swagger work)
                .cors(cors -> cors.configurationSource(req -> {
                    var c = new CorsConfiguration();
                    c.setAllowedOrigins(List.of("*")); // 👉 Replace * with your Flutter web domain in prod
                    c.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
                    c.setAllowedHeaders(List.of("*"));
                    c.setExposedHeaders(List.of("Authorization"));
                    return c;
                }))

                // URL access control
                .authorizeHttpRequests(auth -> auth
                        // ✅ Allow Swagger UI and Docs (important fix)
                        .requestMatchers(
                                "/swagger-ui/**",       // Swagger UI resources
                                "/swagger/**",          // optional alias path
                                "/v1/api-docs/**"       // OpenAPI docs JSON
                        ).permitAll()

                        // ✅ Allow authentication endpoints
                        .requestMatchers("/api/auth/**").permitAll()

                        // ✅ Allow health checks
                        .requestMatchers(HttpMethod.GET, "/actuator/health", "/api/health").permitAll()

                        // 🔒 Everything else needs JWT
                        .anyRequest().authenticated()
                )

                // Plug in our authentication + JWT filter
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
