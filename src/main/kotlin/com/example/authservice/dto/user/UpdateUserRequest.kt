package com.example.authservice.dto.user

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class UpdateUserRequest(

    @field:NotBlank(message = "Username is required")
    val username: String,

    @field:NotBlank(message = "first name is required")
    val firstName: String?,

    @field:NotBlank(message = "last name is required")
    val lastName: String,

    @field:Email(message = "Email must be valid")
    @field:NotBlank(message = "Email is required")
    val email: String,

    // Optional on update
    val password: String? = null,

    val clientId: String? = null
)
