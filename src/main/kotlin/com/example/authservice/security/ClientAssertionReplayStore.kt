package com.example.authservice.security

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class ClientAssertionReplayStore {

    private val seen = ConcurrentHashMap<String, Long>()

    fun isReplay(jti: String, expMillis: Long): Boolean {
        cleanupExpired()
        val existing = seen.putIfAbsent(jti, expMillis)
        return existing != null
    }

    private fun cleanupExpired() {
        val now = System.currentTimeMillis()
        seen.entries.removeIf { it.value <= now }
    }
}
