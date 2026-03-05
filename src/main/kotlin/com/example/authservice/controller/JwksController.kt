package com.example.authservice.controller

import com.example.authservice.security.RsaKeyProvider
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/.well-known")
class JwksController(
    private val rsaKeyProvider: RsaKeyProvider
) {

    @GetMapping("/jwks.json")
    fun jwks(): Map<String, Any> {
        return mapOf("keys" to listOf(rsaKeyProvider.jwk()))
    }
}
