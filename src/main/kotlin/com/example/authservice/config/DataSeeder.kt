package com.example.authservice.config

import com.example.authservice.entity.ClientEntity
import com.example.authservice.entity.UserEntity
import com.example.authservice.repository.ClientRepository
import com.example.authservice.repository.UserRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.security.KeyPairGenerator
import java.util.Base64

@Component
class DataSeeder(
    private val clientRepository: ClientRepository,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) : ApplicationRunner {

    @Transactional
    override fun run(args: ApplicationArguments) {
        val client = ensureDefaultClient()
        ensureDefaultAdmin(client)
    }

    private fun ensureDefaultClient(): ClientEntity {
        val existing = clientRepository.findByClientId(DEFAULT_CLIENT_ID)
        if (existing != null) return existing

        val keyPair = generateRsaKeyPair()
        val publicKeyPem = toPem("PUBLIC KEY", keyPair.public.encoded)
        val privateKeyPem = toPem("PRIVATE KEY", keyPair.private.encoded)

        val client = ClientEntity(
            name = DEFAULT_CLIENT_NAME,
            clientId = DEFAULT_CLIENT_ID,
            publicKey = publicKeyPem,
            privateKey = privateKeyPem,
            redirectUris = null,
            allowedGrantTypes = null,
            allowedScopes = null,
            requirePkce = true
        )
        return clientRepository.save(client)
    }

    private fun ensureDefaultAdmin(client: ClientEntity) {
        if (userRepository.existsByEmail(DEFAULT_ADMIN_EMAIL) || userRepository.existsByUsername(DEFAULT_ADMIN_USERNAME)) {
            return
        }

        val user = UserEntity(
            username = DEFAULT_ADMIN_USERNAME,
            firstName = DEFAULT_ADMIN_FIRST_NAME,
            lastName = DEFAULT_ADMIN_LAST_NAME,
            email = DEFAULT_ADMIN_EMAIL,
            password = passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD)!!,
            enabled = true
        )
        user.client = client
        userRepository.save(user)
    }

    private fun generateRsaKeyPair() =
        KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()

    private fun toPem(type: String, bytes: ByteArray): String {
        val encoded = Base64.getEncoder().encodeToString(bytes)
        val body = encoded.chunked(64).joinToString("\n")
        return "-----BEGIN $type-----\n$body\n-----END $type-----"
    }

    private companion object {
        const val DEFAULT_CLIENT_ID = "authservice"
        const val DEFAULT_CLIENT_NAME = "authservice"

        const val DEFAULT_ADMIN_USERNAME = "admin@gmail.com"
        const val DEFAULT_ADMIN_EMAIL = "admin@gmail.com"
        const val DEFAULT_ADMIN_PASSWORD = "admin@admin"
        const val DEFAULT_ADMIN_FIRST_NAME = "admin"
        const val DEFAULT_ADMIN_LAST_NAME = "admin"
    }
}
