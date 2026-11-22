package com.smartfaculty.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final PasswordEncoder passwordEncoder;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public SecurityConfig(
            PasswordEncoder passwordEncoder,
            JwtAuthenticationFilter jwtAuthenticationFilter) {

        this.passwordEncoder =
                passwordEncoder;

        this.jwtAuthenticationFilter =
                jwtAuthenticationFilter;
    }


    // =====================================================
    // SECURITY FILTER CHAIN
    // =====================================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http

                // =================================================
                // CSRF
                // =================================================

                .csrf(csrf ->
                        csrf.disable()
                )


                // =================================================
                // CORS
                // =================================================

                .cors(cors ->
                {
                })


                // =================================================
                // SECURITY HEADERS
                // =================================================
                //
                // Additional browser-level security protection.
                //
                // frameOptions:
                // Prevents the application from being embedded
                // inside an iframe.
                //
                // contentTypeOptions:
                // Prevents browsers from MIME-type sniffing.
                //
                // referrerPolicy:
                // Limits sensitive URL information being sent
                // through the Referer header.
                //
                // cacheControl:
                // Prevents sensitive authenticated responses
                // from being unnecessarily cached.
                // =================================================

                .headers(headers -> headers

                        .frameOptions(frameOptions ->
                                frameOptions.deny()
                        )

                        .contentTypeOptions(contentTypeOptions ->
                        {
                        })

                        .referrerPolicy(referrerPolicy ->
                                referrerPolicy
                                        .policy(
                                                org.springframework.security.web.header.writers
                                                        .ReferrerPolicyHeaderWriter.ReferrerPolicy
                                                        .STRICT_ORIGIN_WHEN_CROSS_ORIGIN
                                        )
                        )

                        .cacheControl(cacheControl ->
                        {
                        })
                )


                // =================================================
                // SESSION MANAGEMENT
                // =================================================

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .exceptionHandling(exception -> exception

                        // -------------------------------------------------
                        // 401 UNAUTHORIZED
                        // -------------------------------------------------

                        .authenticationEntryPoint((request, response, authException) -> {

                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");

                            response.getWriter().write(
                                    "{\"message\":\"Authentication is required.\"}"
                            );
                        })

                        // -------------------------------------------------
                        // 403 FORBIDDEN
                        // -------------------------------------------------

                        .accessDeniedHandler((request, response, accessDeniedException) -> {

                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");

                            response.getWriter().write(
                                    "{\"message\":\"You do not have permission to access this resource.\"}"
                            );
                        })
                )


                // =================================================
                // AUTHORIZATION
                // =================================================

                .authorizeHttpRequests(auth -> auth

                        // =====================================================
                        // PUBLIC ENDPOINTS
                        // =====================================================

                        .requestMatchers(
                                "/api/auth/login",
                                "/api/students/register"
                        ).permitAll()

                        // Public timetable viewing used by the homepage.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/timetables/year/*/semester/*"
                        ).permitAll()


                        // =====================================================
                        // ADMIN-ONLY HTTP METHODS
                        // =====================================================

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/timetables"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/timetables/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/timetables/**"
                        ).hasRole("ADMIN")


                        // =====================================================
                        // API AUTHENTICATION
                        // =====================================================

                        .requestMatchers(
                                "/api/**"
                        ).authenticated()


                        // =====================================================
                        // OTHER REQUESTS
                        // =====================================================

                        .anyRequest().permitAll()
                )


                // =================================================
                // JWT FILTER
                // =================================================

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );


        return http.build();
    }
}