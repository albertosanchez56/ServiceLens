package com.servicelens.platform.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService(PasswordEncoder encoder) {
        return new InMemoryUserDetailsManager(
                User.withUsername("editor")
                        .password(encoder.encode("editor"))
                        .roles("EDITOR")
                        .build(),
                User.withUsername("viewer")
                        .password(encoder.encode("viewer"))
                        .roles("VIEWER")
                        .build());
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter)
            throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.cors(c -> c.configurationSource(corsConfigurationSource()));
        http.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**", "/api/v1/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/meta").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/incidents").hasAnyRole("VIEWER", "EDITOR")
                .requestMatchers(HttpMethod.GET, "/api/v1/incidents/*").hasAnyRole("VIEWER", "EDITOR")
                .requestMatchers(HttpMethod.GET, "/api/v1/incidents/*/events").hasAnyRole("VIEWER", "EDITOR")
                .requestMatchers(HttpMethod.GET, "/api/v1/incidents/*/evidence").hasAnyRole("VIEWER", "EDITOR")
                .requestMatchers(HttpMethod.GET, "/api/v1/me").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/v1/incidents").hasRole("EDITOR")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/incidents/*").hasRole("EDITOR")
                .requestMatchers(HttpMethod.POST, "/api/v1/incidents/*/events").hasRole("EDITOR")
                .requestMatchers(HttpMethod.POST, "/api/v1/incidents/*/evidence").hasRole("EDITOR")
                .anyRequest().authenticated());
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowedOrigins(List.of("http://localhost:4200", "http://127.0.0.1:4200"));
        c.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));
        c.setAllowedHeaders(List.of("*"));
        c.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", c);
        return source;
    }
}
