package com.example.authservice.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.Base64
import java.util.UUID

@Component
class RsaKeyProvider(
    @Value("\${jwt.rsa.private-key:}") private val privateKeyPem: String,
    @Value("\${jwt.rsa.public-key:}") private val publicKeyPem: String
) {

    val keyId: String = UUID.randomUUID().toString()

    private val keyPair: KeyPair = loadOrGenerate()

    fun publicKey(): RSAPublicKey = keyPair.public as RSAPublicKey

    fun privateKey(): RSAPrivateKey = keyPair.private as RSAPrivateKey

    fun jwk(): Map<String, String> {
        val pub = publicKey()
        val n = Base64.getUrlEncoder().withoutPadding().encodeToString(pub.modulus.toByteArrayUnsigned())
        val e = Base64.getUrlEncoder().withoutPadding().encodeToString(pub.publicExponent.toByteArrayUnsigned())
        return mapOf(
            "kty" to "RSA",
            "kid" to keyId,
            "use" to "sig",
            "alg" to "RS256",
            "n" to n,
            "e" to e
        )
    }

    private fun loadOrGenerate(): KeyPair {
        return if (privateKeyPem.isNotBlank() && publicKeyPem.isNotBlank()) {
            val priv = PemUtils.parseRsaPrivateKey(privateKeyPem)
            val pub = PemUtils.parseRsaPublicKey(publicKeyPem)
            KeyPair(pub, priv)
        } else {
            val generator = KeyPairGenerator.getInstance("RSA")
            generator.initialize(2048)
            generator.generateKeyPair()
        }
    }
}

private fun ByteArray.toByteArrayUnsigned(): ByteArray {
    return if (this.isNotEmpty() && this[0].toInt() == 0) this.copyOfRange(1, this.size) else this
}
