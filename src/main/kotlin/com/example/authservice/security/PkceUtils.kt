package com.example.authservice.security

import java.security.MessageDigest
import java.util.Base64

object PkceUtils {
    fun verify(codeVerifier: String, codeChallenge: String, method: String?): Boolean {
        return when (method?.uppercase()) {
            "S256" -> {
                val digest = MessageDigest.getInstance("SHA-256").digest(codeVerifier.toByteArray())
                val encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
                encoded == codeChallenge
            }
            "PLAIN", null, "" -> codeVerifier == codeChallenge
            else -> false
        }
    }
}
