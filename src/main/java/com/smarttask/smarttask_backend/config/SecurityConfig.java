package com.smarttask.smarttask_backend.config;
import com.smarttask.smarttask_backend.security.JwtAuthFilter;
import com.smarttask.smarttask_backend.security.JwtService;
import com.smarttask.smarttask_backend.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl uds;
    private final JwtService jwt;

    @Bean PasswordEncoder passwordEncoder(){ return new BCryptPasswordEncoder(); }

    @Bean AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(uds);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean JwtAuthFilter jwtAuthFilter(){ return new JwtAuthFilter(jwt, uds); }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // ✅ CORS CONFIG FIX
                .cors(cors -> cors.configurationSource(req -> {
                    var c = new CorsConfiguration();
                    // ✅ WHY: Allow Swagger, Render frontend, Flutter app
                    c.setAllowedOrigins(List.of(
                            "https://smarttask-backend.onrender.com",  // your Render backend
                            "https://smarttask-app.onrender.com",     // your Flutter web app (if deployed)
                            "http://localhost:8080",
                            "http://localhost:3000",
                            "http://127.0.0.1:5500",
                            "*"
                    ));
                    c.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
                    c.setAllowedHeaders(List.of("*"));
                    c.setExposedHeaders(List.of("Authorization"));
                    return c;
                }))
                // ✅ SECURITY FIX: Whitelist Swagger + Auth endpoints
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger/**",
                                "/swagger-ui/**",
                                "/v1/api-docs/**",
                                "/api/auth/**",      // includes swagger-login, register, login
                                "/actuator/health",
                                "/error"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
