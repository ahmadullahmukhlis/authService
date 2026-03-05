package com.example.authservice.controller

import com.example.authservice.dto.response.Response
import com.example.authservice.dto.user.EmailVerifyRequest
import com.example.authservice.dto.user.MfaVerifyRequest
import com.example.authservice.service.AccountSecurityService
import com.example.authservice.service.JwtService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/account")
class AccountSecurityController(
    private val accountSecurityService: AccountSecurityService,
    private val jwtService: JwtService
) {

    @PostMapping("/mfa/setup")
    fun setupMfa(@RequestHeader("Authorization") authHeader: String): Response {
        val username = extractUsername(authHeader)
        return accountSecurityService.startMfa(username)
    }

    @PostMapping("/mfa/verify")
    fun verifyMfa(
        @RequestHeader("Authorization") authHeader: String,
        @Valid @RequestBody request: MfaVerifyRequest
    ): Response {
        val username = extractUsername(authHeader)
        return accountSecurityService.verifyMfa(username, request)
    }

    @PostMapping("/mfa/disable")
    fun disableMfa(
        @RequestHeader("Authorization") authHeader: String,
        @Valid @RequestBody request: MfaVerifyRequest
    ): Response {
        val username = extractUsername(authHeader)
        return accountSecurityService.disableMfa(username, request)
    }

    @PostMapping("/email/verify/request")
    fun requestEmailVerification(@RequestHeader("Authorization") authHeader: String): Response {
        val username = extractUsername(authHeader)
        return accountSecurityService.requestEmailVerification(username)
    }

    @PostMapping("/email/verify/confirm")
    fun confirmEmailVerification(
        @RequestHeader("Authorization") authHeader: String,
        @Valid @RequestBody request: EmailVerifyRequest
    ): Response {
        val username = extractUsername(authHeader)
        return accountSecurityService.confirmEmailVerification(username, request)
    }

    private fun extractUsername(authHeader: String): String {
        val token = authHeader.removePrefix("Bearer ").trim()
        if (!jwtService.validateToken(token)) throw IllegalArgumentException("Invalid access token")
        if (jwtService.extractTokenType(token) != "access") throw IllegalArgumentException("Invalid access token")
        return jwtService.extractUsername(token)
    }
}
