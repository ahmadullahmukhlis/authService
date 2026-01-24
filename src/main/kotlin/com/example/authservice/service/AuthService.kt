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
    private val jwtService: JwtService
) {

    fun login(loginDto: LoginDto): Response {
        // Determine if input is email or username
        val user = if (loginDto.username.contains("@")) {
            userRepository.findByEmail(loginDto.username)
        } else {
            userRepository.findByUsername(loginDto.username)
        }

        // User existence check
        if (user == null) {
            return Response(status = false, message = "User not found", data = null)
        }

        // Password validation
        if (!passwordEncoder.matches(loginDto.password, user.password)) {
            return Response(status = false, message = "Invalid password", data = null)
        }

        // User enabled check
        if (!user.enabled) {
            return Response(status = false, message = "User is disabled", data = null)
        }

        // Generate JWT tokens
        val accessToken = jwtService.generateAccessToken(user.username)
        val refreshToken = jwtService.generateRefreshToken(user.username)

        // Build response with tokens
        val loginResponse = mapOf(
            "user" to user,
            "accessToken" to accessToken,
            "refreshToken" to refreshToken
        )

        return Response(status = true, message = "Login successful", data = loginResponse)
    }
}
