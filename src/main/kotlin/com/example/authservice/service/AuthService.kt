package com.example.authservice.service

import com.example.authservice.dto.user.LoginDto
import com.example.authservice.dto.response.Response
import com.example.authservice.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val refreshTokenStore: RefreshTokenStore
) {

    fun login(loginDto: LoginDto): Response {
        val user = if (loginDto.username.contains("@")) {
            userRepository.findByEmail(loginDto.username)
        } else {
            userRepository.findByUsername(loginDto.username)
        }

        if (user == null) return Response(false, "User not found", null)
        if (!passwordEncoder.matches(loginDto.password, user.password)) return Response(false, "Invalid password", null)
        if (!user.enabled) return Response(false, "User is disabled", null)

        val accessToken = jwtService.generateAccessToken(user.username)
        val refreshToken = jwtService.generateRefreshToken(user.username)

        refreshTokenStore.addToken(user.username, refreshToken)

        val loginResponse = mapOf(
            "user" to user,
            "accessToken" to accessToken,
            "refreshToken" to refreshToken
        )

        return Response(true, "Login successful", loginResponse)
    }

    fun refreshToken(refreshToken: String): Response {
        if (!refreshTokenStore.isValid(refreshToken)) {
            return Response(false, "Invalid refresh token", null)
        }

        val username = refreshTokenStore.getUsername(refreshToken) ?: return Response(false, "Invalid refresh token", null)
        val newAccessToken = jwtService.generateAccessToken(username)
        val newRefreshToken = jwtService.generateRefreshToken(username)

        refreshTokenStore.removeToken(refreshToken)
        refreshTokenStore.addToken(username, newRefreshToken)

        val response = mapOf(
            "accessToken" to newAccessToken,
            "refreshToken" to newRefreshToken
        )

        return Response(true, "Token refreshed", response)
    }

    fun logout(refreshToken: String): Response {
        if (!refreshTokenStore.isValid(refreshToken)) {
            return Response(false, "Invalid refresh token", null)
        }

        refreshTokenStore.removeToken(refreshToken)
        return Response(true, "Logout successful", null)
    }
}
