package com.example.authservice.controller

import com.example.authservice.dto.response.Response
import com.example.authservice.dto.user.LoginDto
import com.example.authservice.service.AuthService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping()
class LoginController(
    private val authService: AuthService
) {

    @PostMapping("/login")
    fun login(
        @RequestHeader("X-Client-Id", required = false) clientId: String?,
        @RequestHeader("X-Client-Assertion", required = false) clientAssertion: String?,
        @Valid @RequestBody request: LoginDto
    ): Response {
        return authService.login(clientId, clientAssertion, request)
    }

    @PostMapping("/refresh")
    fun refresh(
        @RequestHeader("X-Client-Id") clientId: String,
        @RequestHeader("X-Client-Assertion") clientAssertion: String,
        @RequestParam refreshToken: String
    ): Response {
        return authService.refreshToken(clientId, clientAssertion, refreshToken)
    }

    @PostMapping("/logout")
    fun logout(
        @RequestHeader("X-Client-Id") clientId: String,
        @RequestHeader("X-Client-Assertion") clientAssertion: String,
        @RequestParam refreshToken: String
    ): Response {
        return authService.logout(clientId, clientAssertion, refreshToken)
    }
}
