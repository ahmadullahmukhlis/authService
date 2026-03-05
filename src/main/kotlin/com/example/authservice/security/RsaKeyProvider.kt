package com.example.authservice.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.Base64
import java.util.UUID

@Component
class RsaKeyProvider(
    @Value("${'$'}{jwt.rsa.private-key:}") private val privateKeyPem: String,
    @Value("${'$'}{jwt.rsa.public-key:}") private val publicKeyPem: String
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
            val privateKey = PemUtils.parseRsaPrivateKey(privateKeyPem)
            val publicKey = PemUtils.parseRsaPublicKey(publicKeyPem)
            KeyPair(publicKey, privateKey)
        } else {
            val generator = KeyPairGenerator.getInstance("RSA")
            generator.initialize(2048)
            generator.generateKeyPair()
        }
    }
}

private fun BigInteger.toByteArrayUnsigned(): ByteArray {
    val bytes = this.toByteArray()
    return if (bytes.isNotEmpty() && bytes[0].toInt() == 0) bytes.copyOfRange(1, bytes.size) else bytes
}
