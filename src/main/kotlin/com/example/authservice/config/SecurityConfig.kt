package com.example.authservice.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() } // Disable CSRF for testing POST/PUT requests
            .authorizeHttpRequests { auth ->
                auth
                    // Allow Swagger and API docs
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                    // Allow Spring error page
                    .requestMatchers("/error").permitAll()
                    // Allow your client endpoints temporarily
                    .requestMatchers("/api/client/**").permitAll()
                    // All other requests require authentication
                    .anyRequest().authenticated()
            }
            .formLogin { it.disable() } // Disable default login page
            .httpBasic { it.disable() } // Disable HTTP basic auth

        return http.build()
    }
}
