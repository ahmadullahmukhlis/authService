package com.example.authservice.dto.client

import com.example.authservice.dto.user.UserResponse
import com.example.authservice.dto.user.toResponse
import com.example.authservice.entity.ClientEntity

data class ClientResponse(
    val id: String,
    val name: String,
    val publicKey: String?,
    val redirectUris: List<String> = emptyList(),
    val allowedGrantTypes: List<String> = emptyList(),
    val allowedScopes: List<String> = emptyList(),
    val requirePkce: Boolean = true,
    val users: List<UserResponse> = emptyList()
)

// Add a flag to optionally map users
fun ClientEntity.toResponse(withUsers: Boolean = false): ClientResponse {
    return ClientResponse(
        id = this.clientId,
        name = this.name,
        publicKey = this.publicKey,
        redirectUris = this.redirectUris?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList(),
        allowedGrantTypes = this.allowedGrantTypes?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList(),
        allowedScopes = this.allowedScopes?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList(),
        requirePkce = this.requirePkce,
        users = if (withUsers) {
            try {
                // Safely map users; if null or empty, return empty list
                this.users?.map { it.toResponse() } ?: emptyList()
            } catch (ex: Exception) {
                // Catch LazyInitializationException or any other
                emptyList()
            }
        } else {
            emptyList()
        }
    )
}
