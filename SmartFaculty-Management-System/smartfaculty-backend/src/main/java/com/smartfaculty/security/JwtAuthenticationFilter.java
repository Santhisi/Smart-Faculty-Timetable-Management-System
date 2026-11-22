package com.smartfaculty.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;


    // =====================================================
    // JWT AUTHENTICATION FILTER
    // =====================================================

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        // -------------------------------------------------
        // GET AUTHORIZATION HEADER
        // -------------------------------------------------

        String authHeader =
                request.getHeader("Authorization");


        // -------------------------------------------------
        // VALIDATE AUTHORIZATION HEADER
        // -------------------------------------------------

        if (authHeader == null ||
                authHeader.trim().isEmpty()) {

            filterChain.doFilter(request, response);
            return;
        }


        // -------------------------------------------------
        // VALIDATE BEARER FORMAT
        // -------------------------------------------------

        if (!authHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }


        // -------------------------------------------------
        // EXTRACT JWT TOKEN
        // -------------------------------------------------

        String token =
                authHeader.substring(7).trim();


        // -------------------------------------------------
        // VALIDATE TOKEN IS NOT EMPTY
        // -------------------------------------------------

        if (token.isEmpty()) {

            filterChain.doFilter(request, response);
            return;
        }


        // -------------------------------------------------
        // REJECT BEARER TOKEN WITH INTERNAL WHITESPACE
        // -------------------------------------------------

        if (token.contains(" ")) {

            filterChain.doFilter(request, response);
            return;
        }


        // -------------------------------------------------
        // REJECT OVERSIZED JWT
        // -------------------------------------------------

        if (token.length() > 4096) {

            filterChain.doFilter(request, response);
            return;
        }


        // -------------------------------------------------
        // DO NOT REPLACE EXISTING AUTHENTICATION
        // -------------------------------------------------

        if (SecurityContextHolder
                .getContext()
                .getAuthentication() != null &&
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .isAuthenticated()) {

            filterChain.doFilter(request, response);
            return;
        }


        // =================================================
        // JWT VALIDATION
        // =================================================

        try {

            // -------------------------------------------------
            // EXTRACT USERNAME FROM JWT
            // -------------------------------------------------

            String username =
                    jwtService.extractUsername(token);


            // -------------------------------------------------
            // VALIDATE EXTRACTED USERNAME
            // -------------------------------------------------

            if (username == null ||
                    username.trim().isEmpty()) {

                filterChain.doFilter(request, response);
                return;
            }


            username = username.trim();


            // -------------------------------------------------
            // VALIDATE JWT
            // -------------------------------------------------

            if (!jwtService.isTokenValid(
                    token,
                    username)) {

                filterChain.doFilter(request, response);
                return;
            }


            // -------------------------------------------------
            // LOAD CURRENT USER FROM DATABASE
            // -------------------------------------------------

            UserDetails userDetails =
                    userDetailsService
                            .loadUserByUsername(username);


            // -------------------------------------------------
            // VALIDATE USER DETAILS
            // -------------------------------------------------

            if (userDetails == null) {

                filterChain.doFilter(request, response);
                return;
            }


            // -------------------------------------------------
            // VALIDATE USERNAME FROM USER DETAILS
            // -------------------------------------------------

            if (userDetails.getUsername() == null ||
                    userDetails.getUsername()
                            .trim()
                            .isEmpty()) {

                filterChain.doFilter(request, response);
                return;
            }


            // -------------------------------------------------
            // USERNAME MUST MATCH JWT USERNAME
            // -------------------------------------------------

            if (!userDetails.getUsername()
                    .trim()
                    .equals(username)) {

                filterChain.doFilter(request, response);
                return;
            }


            // -------------------------------------------------
            // CHECK ACCOUNT STATUS
            // -------------------------------------------------

            if (!userDetails.isEnabled()) {

                filterChain.doFilter(request, response);
                return;
            }


            // -------------------------------------------------
            // GET CURRENT AUTHORITIES
            // -------------------------------------------------

            Collection<? extends GrantedAuthority> authorities =
                    userDetails.getAuthorities();


            // -------------------------------------------------
            // VALIDATE AUTHORITIES
            // -------------------------------------------------

            if (authorities == null ||
                    authorities.isEmpty()) {

                filterChain.doFilter(request, response);
                return;
            }


            // =================================================
            // VALIDATE SMART FACULTY ROLES
            // =================================================

            int validRoleCount = 0;

            for (GrantedAuthority authority : authorities) {

                if (authority == null ||
                        authority.getAuthority() == null) {

                    continue;
                }

                String role =
                        authority.getAuthority();


                // -------------------------------------------------
                // ALLOWED ROLES
                // -------------------------------------------------

                if (role.equals("ROLE_ADMIN") ||
                        role.equals("ROLE_HOD") ||
                        role.equals("ROLE_LECTURER") ||
                        role.equals("ROLE_STUDENT")) {

                    validRoleCount++;
                }
            }


            // -------------------------------------------------
            // USER MUST HAVE EXACTLY ONE VALID ROLE
            // -------------------------------------------------

            if (validRoleCount != 1) {

                filterChain.doFilter(request, response);
                return;
            }


            // =================================================
            // PART 14Z
            // FINAL USERNAME CONSISTENCY CHECK
            // =================================================

            if (userDetails.getUsername() == null ||
                    userDetails.getUsername()
                            .trim()
                            .isEmpty()) {

                filterChain.doFilter(request, response);
                return;
            }


            if (!userDetails.getUsername()
                    .trim()
                    .equals(username)) {

                filterChain.doFilter(request, response);
                return;
            }


            // =================================================
            // USE CURRENT DATABASE AUTHORITIES
            // =================================================

            Collection<? extends GrantedAuthority> currentAuthorities =
                    userDetails.getAuthorities();


            if (currentAuthorities == null ||
                    currentAuthorities.isEmpty()) {

                filterChain.doFilter(request, response);
                return;
            }


            // =================================================
            // CREATE AUTHENTICATION
            // =================================================

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            currentAuthorities
                    );


            // -------------------------------------------------
            // SET AUTHENTICATION DETAILS
            // -------------------------------------------------

            authentication.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request)
            );


            // -------------------------------------------------
            // SET SECURITY CONTEXT
            // -------------------------------------------------

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);

        } catch (Exception e) {

            // -------------------------------------------------
            // INVALID JWT -> KEEP USER UNAUTHENTICATED
            // -------------------------------------------------

            SecurityContextHolder
                    .clearContext();
        }


        // -------------------------------------------------
        // CONTINUE FILTER CHAIN
        // -------------------------------------------------

        filterChain.doFilter(request, response);
    }
}