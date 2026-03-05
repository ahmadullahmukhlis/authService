package com.example.authservice.service

import com.example.authservice.entity.UserEntity
import com.example.authservice.security.RsaKeyProvider
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date

@Service
class JwtService(
    private val rsaKeyProvider: RsaKeyProvider,
    @Value("\${app.auth.issuer:authservice}") private val issuer: String,
    @Value("\${app.auth.audience:authservice}") private val audience: String
) {

    private val ACCESS_TOKEN_EXPIRATION = 15 * 60 * 1000
    private val REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000

    fun generateAccessToken(user: UserEntity): String {
        return createToken(user, ACCESS_TOKEN_EXPIRATION, "access")
    }

    fun generateRefreshToken(user: UserEntity): String {
        return createToken(user, REFRESH_TOKEN_EXPIRATION, "refresh")
    }

    fun generateIdToken(user: UserEntity): String {
        val now = Date()
        val expiry = Date(now.time + ACCESS_TOKEN_EXPIRATION)
        return Jwts.builder()
            .header().keyId(rsaKeyProvider.keyId).and()
            .subject(user.username)
            .issuer(issuer)
            .audience().add(audience).and()
            .claim("typ", "id")
            .claim("email", user.email)
            .claim("name", listOfNotNull(user.firstName, user.lastName).joinToString(" ").trim())
            .issuedAt(now)
            .expiration(expiry)
            .signWith(rsaKeyProvider.privateKey(), Jwts.SIG.RS256)
            .compact()
    }

    fun generateClientAccessToken(clientId: String, scope: String): String {
        val now = Date()
        val expiry = Date(now.time + ACCESS_TOKEN_EXPIRATION)
        return Jwts.builder()
            .header().keyId(rsaKeyProvider.keyId).and()
            .subject(clientId)
            .issuer(issuer)
            .audience().add(audience).and()
            .claim("typ", "access")
            .claim("scope", scope)
            .claim("client_id", clientId)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(rsaKeyProvider.privateKey(), Jwts.SIG.RS256)
            .compact()
    }

    private fun createToken(user: UserEntity, expirationMillis: Int, tokenType: String): String {
        val now = Date()
        val expiry = Date(now.time + expirationMillis)
        val roles = user.roles.map { it.name }
        val permissions = user.roles
            .flatMap { it.permissions }
            .map { it.permission }
            .distinct()
        return Jwts.builder()
            .header().keyId(rsaKeyProvider.keyId).and()
            .subject(user.username)                 // store username
            .issuer(issuer)
            .audience().add(audience).and()
            .claim("typ", tokenType)
            .claim("roles", roles)
            .claim("permissions", permissions)
            .claim("client_id", user.client?.clientId)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(rsaKeyProvider.privateKey(), Jwts.SIG.RS256)
            .compact()
    }

    fun validateToken(token: String): Boolean {
        return try {
            val claims = getClaims(token)
            val audiences = claims.audience
            claims.issuer == issuer && audiences != null && audiences.contains(audience)
        } catch (e: Exception) {
            false
        }
    }

    fun extractUsername(token: String): String {
        return getClaims(token).subject
    }

    fun extractRoles(token: String): List<String> {
        val roles = getClaims(token)["roles"]
        return if (roles is List<*>) roles.filterIsInstance<String>() else emptyList()
    }

    fun extractPermissions(token: String): List<String> {
        val permissions = getClaims(token)["permissions"]
        return if (permissions is List<*>) permissions.filterIsInstance<String>() else emptyList()
    }

    fun extractTokenType(token: String): String? {
        return getClaims(token)["typ"] as? String
    }

    fun getAllClaims(token: String): Claims {
        return getClaims(token)
    }

    private fun getClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(rsaKeyProvider.publicKey())
            .build()
            .parseSignedClaims(token)
            .payload
    }
}
