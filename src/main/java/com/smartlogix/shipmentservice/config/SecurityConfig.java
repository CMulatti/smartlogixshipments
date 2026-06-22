package com.smartlogix.shipmentservice.config;

import com.smartlogix.shipmentservice.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        //Swagger endpoints (public)
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()

                        //ADMIN: eshops managers/supervisors
                        //USER: warehouse/eshop employee

                        //Internal service communication, ORDERSERVICE tells SHIPMENT SERVICE to create a shipment when an order is created.
                        //THIS SPECIFIC MATCHER SHOULD GO FIRST, BECAUSE SPRING CHECKS TOP TO BOTTOM.
                        .requestMatchers(HttpMethod.POST, "/shipments").permitAll() //can be protected later with service JWTs or API gateway

                        //Managers and employees can see shipments
                        .requestMatchers(HttpMethod.GET, "/shipments/**").hasAnyRole("USER", "ADMIN")

                        //ADMIN as simulator changes shipment status from frontend
                        .requestMatchers(HttpMethod.PUT, "/shipments/*/status").hasRole("ADMIN")

                        //ADMIN as simulator can delete a shipment
                        .requestMatchers(HttpMethod.DELETE, "/shipments/**").hasRole("ADMIN")

                        //Everything else authenticated
                        .anyRequest().authenticated()
                )
                // Add JWT filter before Spring Security's authentication filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://localhost:5175", "http://localhost:8085")); //default ports for Vite + React dev server +GT
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}