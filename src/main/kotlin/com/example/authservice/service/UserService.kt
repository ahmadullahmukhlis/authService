package com.example.authservice.service

import com.example.authservice.dto.response.Response
import com.example.authservice.dto.user.CreateUserRequest
import com.example.authservice.dto.user.UpdateUserRequest
import com.example.authservice.dto.user.UserResponse
import com.example.authservice.dto.user.toResponse
import com.example.authservice.entity.UserEntity
import com.example.authservice.repository.ClientRepository
import com.example.authservice.repository.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val clientRepository: ClientRepository
) {

    private val uploadDir = Paths.get("uploads")

    /**
     * CREATE USER
     */
    @Transactional
    fun createUser(request: CreateUserRequest, photo: MultipartFile?): Response {

        // Check username/email exists
        if (userRepository.existsByUsername(request.username)) {
            return Response(false, "Username already exists", null)
        }
        if (userRepository.existsByEmail(request.email)) {
            return Response(false, "Email already exists", null)
        }

        // Handle photo
        val photoPath = storePhoto(photo)

        val user = UserEntity(
            username = request.username,
            firstName = request.firstName,
            lastName = request.lastName,
            email = request.email,
            password = passwordEncoder.encode(request.password)!!,
            enabled = true,
            Photo = photoPath
        )

        if (!request.clientId.isNullOrBlank()) {
            val client = clientRepository.findByClientId(request.clientId!!)
                ?: return Response(false, "Client not found", null)
            user.client = client
        }

        val savedUser = userRepository.save(user)
        return Response(true, "User has been created", savedUser.toResponse())
    }

    /**
     * UPDATE USER (without photo for now)
     */
    @Transactional
    fun update(id: Long, request: UpdateUserRequest, photo: MultipartFile? = null): Response {
        val user = userRepository.findByIdOrNull(id) ?: return Response(false, "User not found", null)

        val targetClientId = request.clientId ?: user.client?.clientId
        if (user.client?.clientId != null && user.client?.clientId != targetClientId) {
            return Response(false, "User not allowed for this client", null)
        }

        user.username = request.username
        user.email = request.email
        user.firstName = request.firstName
        user.lastName = request.lastName

        // Update photo only if provided
        val photoPath = storePhoto(photo)
        if (photoPath != null) {
            user.Photo = photoPath
        }

        if (!request.password.isNullOrBlank()) {
            if (request.password.length < 8) {
                return Response(false, "Password must be at least 8 characters", null)
            }
            user.password = passwordEncoder.encode(request.password)!!
        }

        if (!request.clientId.isNullOrBlank()) {
            val client = clientRepository.findByClientId(request.clientId!!)
                ?: return Response(false, "Client not found", null)
            user.client = client
        }

        val updatedUser = userRepository.save(user)
        return Response(true, "User has been updated", updatedUser.toResponse())
    }

    /**
     * GET USER BY HID
     */
    fun edit(id: String, clientId: String): Response {
        val user = userRepository.findByuserHid(id)
        return if (user != null && user.client?.clientId == clientId) {
            Response(true, "User found", user.toResponse())
        } else {
            Response(false, "User not found", null)
        }
    }

    /**
     * GET ALL USERS
     */
    fun getAllUsers(clientId: String): List<UserResponse> {
        return userRepository.findAllByClient_ClientId(clientId).map { it.toResponse() }
    }

    /**
     * ENABLE USER
     */
    @Transactional
    fun enabled(id: Long): Response {
        val user = userRepository.findByIdOrNull(id)
            ?: return Response(false, "The user is not found", null)

        user.enabled = true
        val savedUser = userRepository.save(user)
        return Response(true, "The user has been enabled", savedUser.toResponse())
    }

    /**
     * IMAGE VALIDATION
     */
    private fun validateImage(file: MultipartFile) {
        val allowedTypes = listOf("image/jpeg", "image/png", "image/webp")

        if (file.isEmpty) throw RuntimeException("Photo is empty")
        if (file.size > 5 * 1024 * 1024) throw RuntimeException("Max image size is 5MB")
        if (file.contentType !in allowedTypes) throw RuntimeException("Only JPG, PNG, WEBP allowed")
    }

    private fun storePhoto(photo: MultipartFile?): String? {
        if (photo == null || photo.isEmpty) return null

        validateImage(photo)

        // ensure upload folder exists
        Files.createDirectories(uploadDir)

        val ext = photo.originalFilename?.substringAfterLast('.', "jpg")
        val fileName = UUID.randomUUID().toString() + "." + ext

        val filePath = uploadDir.resolve(fileName)
        photo.transferTo(filePath.toFile())

        // store relative path in DB
        return "images/$fileName"
    }
}
