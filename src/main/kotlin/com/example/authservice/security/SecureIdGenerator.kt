package com.example.authservice.security

import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.util.Base64

@Component
class SecureIdGenerator {

    private val random = SecureRandom()

    fun generateId(bytes: Int = 24): String {
        val buffer = ByteArray(bytes)
        random.nextBytes(buffer)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer)
    }
}
