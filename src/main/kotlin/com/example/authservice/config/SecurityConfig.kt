package com.example.authservice.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtFilter: JwtFilter
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // ✅ ENABLE CORS
            .cors { }

            // ✅ DISABLE CSRF (API)
            .csrf { it.disable() }

            // ✅ NO SESSION (JWT)
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }

            // ✅ AUTH RULES
            .authorizeHttpRequests { auth ->
                auth
                    // ✅ allow preflight
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                    // ✅ public endpoints
                    .requestMatchers(
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-ui.html",
                        "/error",

                        // ✅ CORRECT FOR CONTEXT PATH
                        "/login",
                        "/refresh",
                        "/logout"
                    ).permitAll()

                    // ✅ everything else secured
                    .anyRequest().authenticated()
            }

            // ✅ DISABLE DEFAULT LOGIN
            .formLogin { it.disable() }
            .httpBasic { it.disable() }

        // ✅ JWT FILTER
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    // ✅ CORS CONFIGURATION
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration()

        // ✅ Allow Angular dev server (any port)
        config.allowedOriginPatterns = listOf("http://localhost:*")

        // ✅ Allow all methods
        config.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")

        // ✅ Allow all headers (important for Authorization)
        config.allowedHeaders = listOf("*")

        // ✅ Allow credentials (cookies / auth)
        config.allowCredentials = true

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", config)
        return source
    }

}
