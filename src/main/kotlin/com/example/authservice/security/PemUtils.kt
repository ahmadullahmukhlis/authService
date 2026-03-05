package com.example.authservice.security

import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

object PemUtils {

    fun parseRsaPublicKey(pem: String): RSAPublicKey {
        val clean = stripPem(pem)
        val decoded = Base64.getDecoder().decode(clean)
        val spec = X509EncodedKeySpec(decoded)
        val key = KeyFactory.getInstance("RSA").generatePublic(spec)
        return key as RSAPublicKey
    }

    fun parseRsaPrivateKey(pem: String): RSAPrivateKey {
        val clean = stripPem(pem)
        val decoded = Base64.getDecoder().decode(clean)
        val spec = PKCS8EncodedKeySpec(decoded)
        val key = KeyFactory.getInstance("RSA").generatePrivate(spec)
        return key as RSAPrivateKey
    }

    private fun stripPem(pem: String): String {
        return pem
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\s".toRegex(), "")
            .trim()
    }
}
