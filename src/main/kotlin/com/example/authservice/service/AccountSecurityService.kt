package com.example.authservice.service

import com.example.authservice.dto.response.Response
import com.example.authservice.dto.user.EmailVerifyRequest
import com.example.authservice.dto.user.MfaVerifyRequest
import com.example.authservice.repository.UserRepository
import com.example.authservice.security.HashUtils
import com.example.authservice.security.TotpService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class AccountSecurityService(
    private val userRepository: UserRepository,
    private val totpService: TotpService,
    @Value("\${app.email-verification.minutes:10}") private val emailVerificationMinutes: Long
) {

    fun startMfa(username: String): Response {
        val user = userRepository.findByUsername(username) ?: return Response(false, "User not found", null)
        val secret = totpService.generateSecret()
        user.mfaSecret = secret
        userRepository.save(user)
        val otpauth = totpService.otpauthUri(user.username, "authservice", secret)
        return Response(true, "MFA secret generated", mapOf("secret" to secret, "otpauth" to otpauth))
    }

    fun verifyMfa(username: String, request: MfaVerifyRequest): Response {
        val user = userRepository.findByUsername(username) ?: return Response(false, "User not found", null)
        val secret = user.mfaSecret ?: return Response(false, "MFA not initialized", null)
        val ok = totpService.verifyCode(secret, request.code)
        if (!ok) return Response(false, "Invalid code", null)
        user.mfaEnabled = true
        userRepository.save(user)
        return Response(true, "MFA enabled", null)
    }

    fun disableMfa(username: String, request: MfaVerifyRequest): Response {
        val user = userRepository.findByUsername(username) ?: return Response(false, "User not found", null)
        val secret = user.mfaSecret ?: return Response(false, "MFA not initialized", null)
        val ok = totpService.verifyCode(secret, request.code)
        if (!ok) return Response(false, "Invalid code", null)
        user.mfaEnabled = false
        user.mfaSecret = null
        userRepository.save(user)
        return Response(true, "MFA disabled", null)
    }

    fun requestEmailVerification(username: String): Response {
        val user = userRepository.findByUsername(username) ?: return Response(false, "User not found", null)
        val code = (100000..999999).random().toString()
        user.emailVerificationCodeHash = HashUtils.sha256(code)
        user.emailVerificationExpiresAt = LocalDateTime.now().plusMinutes(emailVerificationMinutes)
        userRepository.save(user)
        // In production, send email. For now return code.
        return Response(true, "Verification code generated", mapOf("code" to code))
    }

    fun confirmEmailVerification(username: String, request: EmailVerifyRequest): Response {
        val user = userRepository.findByUsername(username) ?: return Response(false, "User not found", null)
        val expiresAt = user.emailVerificationExpiresAt
        if (expiresAt == null || expiresAt.isBefore(LocalDateTime.now())) {
            return Response(false, "Verification code expired", null)
        }
        val hash = HashUtils.sha256(request.code)
        if (hash != user.emailVerificationCodeHash) {
            return Response(false, "Invalid verification code", null)
        }
        user.emailVerified = true
        user.emailVerificationCodeHash = null
        user.emailVerificationExpiresAt = null
        userRepository.save(user)
        return Response(true, "Email verified", null)
    }
}
