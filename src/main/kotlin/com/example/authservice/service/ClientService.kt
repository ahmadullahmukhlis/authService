package com.example.authservice.service

import com.example.authservice.dto.client.ClientResponse
import com.example.authservice.dto.client.CreateClientRequest
import com.example.authservice.dto.client.toResponse
import com.example.authservice.dto.response.Response
import com.example.authservice.entity.ClientEntity
import com.example.authservice.repository.ClientRepository
import com.example.authservice.security.SecureIdGenerator
import com.example.authservice.security.PemUtils
import org.springframework.stereotype.Service

@Service
class ClientService(
    private val clientRespository: ClientRepository,
    private val idGenerator: SecureIdGenerator
) {

    fun index(): List<ClientResponse> =
        clientRespository.findAll().map { it.toResponse() }

    fun show(id: String): Response {
        val client = clientRespository.findByClientId(id)
        return if (client == null) {
            Response(false, "Client not found", null)
        } else {
            Response(true, "Client found", client.toResponse())
        }
    }

    fun create(request: CreateClientRequest): Response {

        // 🔐 Ensure unique clientId
        var clientId: String
        do {
            clientId = idGenerator.generateId()
        } while (clientRespository.findByClientId(clientId) != null)

        // Validate public key format
        try {
            PemUtils.parseRsaPublicKey(request.publicKey)
        } catch (ex: Exception) {
            return Response(false, "Invalid public key format", null)
        }

        val client = ClientEntity(
            name = request.name,
            clientId = clientId,
            publicKey = request.publicKey.trim(),
            privateKey = "",
            redirectUris = request.redirectUris.joinToString(","),
            allowedGrantTypes = request.allowedGrantTypes.joinToString(","),
            allowedScopes = request.allowedScopes.joinToString(","),
            requirePkce = request.requirePkce
        )

        clientRespository.save(client)

        return Response(
            status = true,
            message = "Client created successfully",
            data = client.toResponse()
        )
    }

    fun update(id: String, request: CreateClientRequest): Response {
        val client = clientRespository.findByClientId(id)
            ?: return Response(false, "Client not found", null)

        try {
            PemUtils.parseRsaPublicKey(request.publicKey)
        } catch (ex: Exception) {
            return Response(false, "Invalid public key format", null)
        }
        client.name = request.name
        client.publicKey = request.publicKey.trim()
        client.redirectUris = request.redirectUris.joinToString(",")
        client.allowedGrantTypes = request.allowedGrantTypes.joinToString(",")
        client.allowedScopes = request.allowedScopes.joinToString(",")
        client.requirePkce = request.requirePkce
        clientRespository.save(client)

        return Response(true, "Client updated", client.toResponse())
    }
    fun loadwithUser(id: String): Response {
        val client = clientRespository.findByClientId(id);
        if (client == null) {
            return Response(false, "Client not found", null)
        }
        return Response(
            status = true,
            message = "Client  found",
            data = client.toResponse(true)
        )
    }
}
