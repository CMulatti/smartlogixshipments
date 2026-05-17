package com.smartlogix.shipmentservice.security;

import com.smartlogix.shipmentservice.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    //This method is called automatically by Spring for each HTTP request
    @Override
    protected void doFilterInternal(HttpServletRequest request,  //incoming HTTP request
                                    HttpServletResponse response, //HTTP response object
                                    FilterChain filterChain) throws ServletException, IOException {  //filerChain lets the request continue to the next filter or controller

        //Check for Authorization header
        String authHeader = request.getHeader("Authorization");

        // If no token, continue without authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extract token
            String token = authHeader.substring(7); //remove Bearer

            //Take JWT string and validate it using JWTService, if valid extract username and role from token
            if (jwtService.isTokenValid(token)) {
                String username = jwtService.getUsernameFromToken(token);
                String role = jwtService.getRoleFromToken(token);

                //Create authentication object
                // IMPORTANT: Spring Security expects roles with "ROLE_" prefix
                //UsernamePasswordAuthenticationToken is a Spring security object that represents the user, it includes, username, null for password bc we trust the JWT already, and authorities which is the list of roles (admin or user)
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, List.of(authority));

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request)); //setDetails attaches extra info

                //set authentication in security context
                //we are telling Spring "This request is authenticated, this is the user and his role"
                //After this, any controller can check authentication with Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            // Invalid token : continue without authentication
            System.err.println("Error de validación JWT: " + e.getMessage());
        }

        filterChain.doFilter(request, response); //let the request go to the next filter or controller
    }
}