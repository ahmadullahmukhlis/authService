package com.example.authservice.controller

import com.example.authservice.repository.UserRepository
import com.example.authservice.service.JwtService
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class OidcController(
    private val jwtService: JwtService,
    private val userRepository: UserRepository,
    @Value("\${app.auth.issuer:authservice}") private val issuer: String,
    @Value("\${server.servlet.context-path:}") private val contextPath: String,
    @Value("\${server.port:8080}") private val port: String
) {

    @GetMapping("/.well-known/openid-configuration")
    fun discovery(): Map<String, Any> {
        val base = "http://localhost:$port$contextPath"
        return mapOf(
            "issuer" to issuer,
            "authorization_endpoint" to "$base/oauth2/authorize",
            "token_endpoint" to "$base/oauth2/token",
            "jwks_uri" to "$base/.well-known/jwks.json",
            "userinfo_endpoint" to "$base/oauth2/userinfo",
            "introspection_endpoint" to "$base/oauth2/introspect",
            "response_types_supported" to listOf("code"),
            "grant_types_supported" to listOf("authorization_code", "refresh_token", "client_credentials"),
            "scopes_supported" to listOf("openid", "profile", "email"),
            "subject_types_supported" to listOf("public"),
            "id_token_signing_alg_values_supported" to listOf("RS256")
        )
    }

    @GetMapping("/oauth2/userinfo")
    fun userInfo(@RequestHeader("Authorization") authHeader: String): Map<String, Any> {
        val token = authHeader.removePrefix("Bearer ").trim()
        if (!jwtService.validateToken(token) || jwtService.extractTokenType(token) != "access") {
            throw IllegalArgumentException("Invalid access token")
        }
        val username = jwtService.extractUsername(token)
        val user = userRepository.findByUsername(username) ?: throw IllegalArgumentException("User not found")
        return mapOf(
            "sub" to user.username,
            "email" to user.email,
            "email_verified" to user.emailVerified,
            "name" to listOfNotNull(user.firstName, user.lastName).joinToString(" ").trim()
        )
    }
}
