package com.example.authservice.dto.user

import jakarta.validation.constraints.NotBlank

data class EmailVerifyRequest(
    @field:NotBlank(message = "Code is required")
    val code: String
)
