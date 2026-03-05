package com.example.authservice.service

import com.example.authservice.dto.response.Response
import com.example.authservice.dto.user.LoginDto
import com.example.authservice.dto.user.toResponse
import com.example.authservice.repository.UserRepository
import com.example.authservice.security.ClientAssertionService
import com.example.authservice.security.TotpService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val refreshTokenService: RefreshTokenService,
    private val clientAssertionService: ClientAssertionService,
    private val totpService: TotpService
) {

    fun login(clientId: String?, clientAssertion: String?, loginDto: LoginDto): Response {
        if (!clientId.isNullOrBlank() || !clientAssertion.isNullOrBlank()) {
            if (clientId.isNullOrBlank() || clientAssertion.isNullOrBlank()) {
                return Response(false, "Client headers incomplete", null)
            }
            clientAssertionService.validate(clientId, clientAssertion)
        }

        val user = if (loginDto.username.contains("@")) {
            userRepository.findByEmail(loginDto.username)
        } else {
            userRepository.findByUsername(loginDto.username)
        }
        if (user == null) return Response(false, "User not found", null)

        if (user == null) return Response(false, "User not found", null)
        if (!passwordEncoder.matches(loginDto.password, user.password)) return Response(false, "Invalid password", null)
        if (!user.enabled) return Response(false, "User is disabled", null)
        if (user.client?.clientId == null) {
            return Response(false, "User has no client assigned", null)
        }
        if (!clientId.isNullOrBlank() && user.client?.clientId != clientId) {
            return Response(false, "User not allowed for this client", null)
        }
        if (user.mfaEnabled) {
            val code = loginDto.mfaCode ?: return Response(false, "MFA code required", null)
            if (!totpService.verifyCode(user.mfaSecret ?: "", code)) {
                return Response(false, "Invalid MFA code", null)
            }
        }

        val accessToken = jwtService.generateAccessToken(user)
        val effectiveClientId = clientId ?: user.client?.clientId
            ?: return Response(false, "User has no client assigned", null)
        val refreshToken = refreshTokenService.create(user, effectiveClientId)

        val loginResponse = mapOf(
            "user" to user.toResponse(),
            "accessToken" to accessToken,
            "refreshToken" to refreshToken
        )

        return Response(true, "Login successful", loginResponse)
    }

    fun refreshToken(clientId: String, clientAssertion: String, refreshToken: String): Response {
        clientAssertionService.validate(clientId, clientAssertion)

        val (newRefreshToken, user) = try {
            refreshTokenService.rotate(refreshToken, clientId)
        } catch (ex: Exception) {
            return Response(false, "Invalid refresh token", null)
        }

        val newAccessToken = jwtService.generateAccessToken(user)

        val response = mapOf(
            "accessToken" to newAccessToken,
            "refreshToken" to newRefreshToken
        )

        return Response(true, "Token refreshed", response)
    }

    fun logout(clientId: String, clientAssertion: String, refreshToken: String): Response {
        clientAssertionService.validate(clientId, clientAssertion)

        refreshTokenService.revoke(refreshToken, clientId)
        return Response(true, "Logout successful", null)
    }
}
