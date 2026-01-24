package com.example.authservice.service

import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class RefreshTokenStore {
    private val store = ConcurrentHashMap<String, String>() // refreshToken -> username

    fun addToken(username: String, refreshToken: String) {
        store[refreshToken] = username
    }

    fun removeToken(refreshToken: String) {
        store.remove(refreshToken)
    }

    fun isValid(refreshToken: String): Boolean {
        return store.containsKey(refreshToken)
    }

    fun getUsername(refreshToken: String): String? {
        return store[refreshToken]
    }
}
