package com.marketingagencybackend.security;

import com.marketingagencybackend.dto.ApiResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable) // safe: stateless, token-based API, no browser session/cookie auth
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // 1. Swagger UI & API Docs (Public)
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // 2. Auth Endpoints (Public)
                        .requestMatchers("/auth/**").permitAll()

                        // 3. Public Client, Enquiry & Public Feedback Fetch Endpoints
                        .requestMatchers(HttpMethod.POST,
                                "/api/client/registration",
                                "/api/client/delete-account",
                                "/api/enquirie/create"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/feedback/all",
                                "/api/feedback/*"
                        ).permitAll()

                        // 4. Feedback Management Endpoints (CLIENT & ADMIN)
                        .requestMatchers(
                                "/api/feedback/create/**",
                                "/api/feedback/update/**",
                                "/api/feedback/delete/**"
                        ).hasAnyRole("CLIENT", "ADMIN")

                        // 5. Client Profile Operations (CLIENT & ADMIN)
                        .requestMatchers(
                                "/api/client/**"
                        ).hasAnyRole("CLIENT", "ADMIN")

                        // 6. Subscription Purchase Operations (CLIENT Only)
                        .requestMatchers(
                                "/api/subscription/**"
                        ).hasRole("CLIENT")

                        // 6. Admin Management Operations (ADMIN Only)
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // 7. All other requests require authentication
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, e) -> {
                    response.setStatus(401);
                    response.setContentType("application/json");

                    String message = e.getMessage();
                    String error = (String) request.getAttribute("error");
                    if (error != null) {
                        message = error;
                    }

                    ApiResponseDTO<Object> body = new ApiResponseDTO<>("FAIL", message, null);
                    response.getWriter().write(objectMapper.writeValueAsString(body));
                }))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("https://frontend-domain.com"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws AuthenticationException, Exception {
        return config.getAuthenticationManager();
    }
}
