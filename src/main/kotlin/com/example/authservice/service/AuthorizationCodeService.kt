package com.example.authservice.service

import com.example.authservice.entity.AuthorizationCodeEntity
import com.example.authservice.repository.AuthorizationCodeRepository
import com.example.authservice.security.SecureIdGenerator
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class AuthorizationCodeService(
    private val repository: AuthorizationCodeRepository,
    private val idGenerator: SecureIdGenerator,
    @Value("\${app.auth-code.minutes:5}") private val codeMinutes: Long
) {

    fun create(
        clientId: String,
        username: String,
        redirectUri: String,
        scope: String,
        codeChallenge: String?,
        codeChallengeMethod: String?
    ): String {
        val code = idGenerator.generateId(32)
        val entity = AuthorizationCodeEntity(
            code = code,
            clientId = clientId,
            username = username,
            redirectUri = redirectUri,
            scope = scope,
            codeChallenge = codeChallenge,
            codeChallengeMethod = codeChallengeMethod,
            expiresAt = LocalDateTime.now().plusMinutes(codeMinutes),
            used = false
        )
        repository.save(entity)
        return code
    }

    fun consume(code: String, clientId: String, redirectUri: String): AuthorizationCodeEntity {
        val entity = repository.findByCode(code) ?: throw IllegalArgumentException("Invalid authorization code")
        if (entity.used) throw IllegalArgumentException("Invalid authorization code")
        if (entity.clientId != clientId) throw IllegalArgumentException("Invalid authorization code")
        if (entity.redirectUri != redirectUri) throw IllegalArgumentException("Invalid authorization code")
        if (entity.expiresAt.isBefore(LocalDateTime.now())) throw IllegalArgumentException("Authorization code expired")

        entity.used = true
        repository.save(entity)
        return entity
    }
}
