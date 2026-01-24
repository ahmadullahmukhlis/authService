package com.example.authservice.service

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.SecretKey

@Service
class JwtService {

    private val secretKey: SecretKey = Keys.hmacShaKeyFor(
        "my-super-secret-key-for-jwt-2026-must-be-at-least-32-characters-long".toByteArray()
    )

    private val ACCESS_TOKEN_EXPIRATION = 15 * 60 * 1000
    private val REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000

    fun generateAccessToken(username: String): String {
        return createToken(username, ACCESS_TOKEN_EXPIRATION)
    }

    fun generateRefreshToken(username: String): String {
        return createToken(username, REFRESH_TOKEN_EXPIRATION)
    }

    private fun createToken(username: String, expirationMillis: Int): String {
        val now = Date()
        val expiry = Date(now.time + expirationMillis)
        return Jwts.builder()
            .subject(username)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(secretKey)
            .compact()
    }

    fun validateToken(token: String): Boolean {
        return try {
            getClaims(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun extractUsername(token: String): String {
        return getClaims(token).subject
    }

    private fun getClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}
