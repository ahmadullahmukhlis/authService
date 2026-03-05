package com.example.authservice.security

import com.example.authservice.repository.ClientRepository
import io.jsonwebtoken.Jwts
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date

@Service
class ClientAssertionService(
    private val clientRepository: ClientRepository,
    private val replayStore: ClientAssertionReplayStore,
    @Value("\${app.client-assertion.audience:authservice}") private val audience: String
) {

    fun validate(clientId: String, assertion: String) {
        if (clientId.isBlank()) {
            throw IllegalArgumentException("Missing X-Client-Id")
        }
        if (assertion.isBlank()) {
            throw IllegalArgumentException("Missing X-Client-Assertion")
        }

        val client = clientRepository.findByClientId(clientId)
            ?: throw IllegalArgumentException("Invalid clientId")

        if (client.publicKey.isBlank()) {
            throw IllegalArgumentException("Client has no public key")
        }

        val publicKey = try {
            PemUtils.parseRsaPublicKey(client.publicKey)
        } catch (ex: Exception) {
            throw IllegalArgumentException("Invalid client public key")
        }

        val claims = Jwts.parser()
            .verifyWith(publicKey)
            .build()
            .parseSignedClaims(assertion)
            .payload

        if (claims.issuer != clientId || claims.subject != clientId) {
            throw IllegalArgumentException("Invalid client assertion issuer/subject")
        }

        val audiences = claims.audience
        if (audiences == null || !audiences.contains(audience)) {
            throw IllegalArgumentException("Invalid client assertion audience")
        }

        val now = Date()
        if (claims.expiration == null || claims.expiration.before(now)) {
            throw IllegalArgumentException("Client assertion expired")
        }

        val jti = claims.id ?: throw IllegalArgumentException("Client assertion missing jti")
        if (replayStore.isReplay(jti, claims.expiration.time)) {
            throw IllegalArgumentException("Client assertion replayed")
        }
    }
}
