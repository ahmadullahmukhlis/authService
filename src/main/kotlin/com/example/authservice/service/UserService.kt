package com.example.authservice.service

import com.example.authservice.dto.response.Response
import com.example.authservice.dto.user.CreateUserRequest
import com.example.authservice.dto.user.UserResponse
import com.example.authservice.dto.user.toResponse
import com.example.authservice.entity.UserEntity
import com.example.authservice.repository.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository
) {

    /**
     * Creates a new user after checking if username or email already exists.
     */
    @Transactional
    fun createUser(request: CreateUserRequest): Response {
        if (userRepository.existsByUsername(request.username)) {
            return Response(status = false, message = "Username already exists", data = null)
        }

        if (userRepository.existsByEmail(request.email)) {
            return Response(status = false, message = "Email already exists", data = null)
        }

        val user = UserEntity(
            username = request.username,
            email = request.email,
            password = request.password, // ⚠️ Note: You should use BCryptPasswordEncoder here
            enabled = true
        )

        val savedUser = userRepository.save(user)
        return Response(status = true, message = "User has been created", data = savedUser.toResponse())
    }

    /**
     * Fetches a single user for editing purposes.
     */
    fun edit(id: String): Response {
        val user = userRepository.findByuserHid(id)
        return if (user != null) {
            Response(status = true, message = "User found", data = user.toResponse())
        } else {
            Response(status = false, message = "User not found", data = null)
        }
    }

    /**
     * Updates an existing user's details.
     */
    @Transactional
    fun update(id: Long, request: CreateUserRequest): Response {
        val user = userRepository.findByIdOrNull(id)
        return if (user != null) {
            // Update the fields on the existing object
            user.username = request.username
            user.email = request.email
            user.password = request.password

            val updatedUser = userRepository.save(user)
            Response(status = true, message = "User has been updated", data = updatedUser.toResponse())
        } else {
            Response(status = false, message = "User not found", data = null)
        }
    }

    /**
     * Returns a list of all users mapped to UserResponse DTOs.
     */
    fun getAllUsers(): List<UserResponse> {
        return userRepository.findAll().map { it.toResponse() }
    }

    /**
     * Enables a user account by ID.
     */
    @Transactional
    fun enabled(id: Long): Response {
        val user = userRepository.findByIdOrNull(id)

        if (user == null) {
            return Response(status = false, message = "The user is not found", data = null)
        }

        user.enabled = true
        val savedUser = userRepository.save(user)

        return Response(
            status = true,
            message = "The user has been enabled",
            data = savedUser.toResponse()
        )
    }
}
