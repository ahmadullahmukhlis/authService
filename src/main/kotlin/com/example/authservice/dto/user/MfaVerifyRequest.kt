package com.example.authservice.dto.user

import jakarta.validation.constraints.NotBlank

data class MfaVerifyRequest(
    @field:NotBlank(message = "Code is required")
    val code: String
)
