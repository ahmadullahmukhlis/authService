package com.example.authservice.service

import com.example.authservice.entity.ClientEntity
import org.springframework.stereotype.Service

@Service
class ClientPolicyService {

    fun redirectAllowed(client: ClientEntity, redirectUri: String): Boolean {
        val list = client.redirectUris
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?: emptyList()
        return list.contains(redirectUri)
    }

    fun grantAllowed(client: ClientEntity, grantType: String): Boolean {
        val list = client.allowedGrantTypes
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?: emptyList()
        return list.isEmpty() || list.contains(grantType)
    }

    fun scopeAllowed(client: ClientEntity, scope: String): Boolean {
        val allowed = client.allowedScopes
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?: emptyList()
        if (allowed.isEmpty()) return true
        val requested = scope.split(" ").map { it.trim() }.filter { it.isNotEmpty() }
        return requested.all { allowed.contains(it) }
    }
}
