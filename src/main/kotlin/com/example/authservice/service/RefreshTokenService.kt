package com.example.authservice.service

import com.example.authservice.entity.RefreshTokenEntity
import com.example.authservice.entity.UserEntity
import com.example.authservice.repository.RefreshTokenRepository
import com.example.authservice.security.HashUtils
import com.example.authservice.security.SecureIdGenerator
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class RefreshTokenService(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val idGenerator: SecureIdGenerator,
    @Value("\${app.refresh-token.days:30}") private val refreshDays: Long
) {

    fun create(user: UserEntity, clientId: String): String {
        val token = idGenerator.generateId(48)
        val tokenHash = HashUtils.sha256(token)
        val expiresAt = LocalDateTime.now().plusDays(refreshDays)

        val entity = RefreshTokenEntity(
            tokenHash = tokenHash,
            user = user,
            clientId = clientId,
            expiresAt = expiresAt
        )
        refreshTokenRepository.save(entity)
        return token
    }

    fun rotate(oldToken: String, clientId: String): Pair<String, UserEntity> {
        val oldHash = HashUtils.sha256(oldToken)
        val existing = refreshTokenRepository.findByTokenHash(oldHash)
            ?: throw IllegalArgumentException("Invalid refresh token")

        if (existing.clientId != clientId) {
            throw IllegalArgumentException("Invalid refresh token")
        }
        if (existing.revokedAt != null || existing.expiresAt.isBefore(LocalDateTime.now())) {
            throw IllegalArgumentException("Invalid refresh token")
        }

        val newToken = idGenerator.generateId(48)
        val newHash = HashUtils.sha256(newToken)
        val newEntity = RefreshTokenEntity(
            tokenHash = newHash,
            user = existing.user,
            clientId = clientId,
            expiresAt = LocalDateTime.now().plusDays(refreshDays)
        )
        refreshTokenRepository.save(newEntity)

        existing.revokedAt = LocalDateTime.now()
        existing.replacedByHash = newHash
        refreshTokenRepository.save(existing)

        return newToken to existing.user
    }

    fun revoke(token: String, clientId: String) {
        val hash = HashUtils.sha256(token)
        val existing = refreshTokenRepository.findByTokenHash(hash) ?: return
        if (existing.clientId != clientId) return
        if (existing.revokedAt == null) {
            existing.revokedAt = LocalDateTime.now()
            refreshTokenRepository.save(existing)
        }
    }
}
