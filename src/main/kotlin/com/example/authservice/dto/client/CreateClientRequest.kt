package com.example.authservice.dto.client

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateClientRequest(
    @field:NotBlank(message = "Name must not be blank")
    @field:Size(max = 100, message = "Name must be at most 100 characters")
    val name: String,

    @field:NotBlank(message = "Public key must not be blank")
    val publicKey: String,

    val redirectUris: List<String> = emptyList(),
    val allowedGrantTypes: List<String> = emptyList(),
    val allowedScopes: List<String> = emptyList(),
    val requirePkce: Boolean = true
)
