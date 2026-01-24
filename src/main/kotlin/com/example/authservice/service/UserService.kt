package com.example.authservice.service

import com.example.authservice.dto.response.Response
import com.example.authservice.dto.user.CreateUserRequest
import com.example.authservice.dto.user.UserResponse
import com.example.authservice.dto.user.toResponse
import com.example.authservice.entity.UserEntity
import com.example.authservice.repository.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

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
            password = passwordEncoder.encode(request.password)!!,
            enabled = true
        )

        val savedUser = userRepository.save(user)
        return Response(status = true, message = "User has been created", data = savedUser.toResponse())
    }

    @Transactional
    fun update(id: Long, request: CreateUserRequest): Response {
        val user = userRepository.findByIdOrNull(id)
        return if (user != null) {
            user.username = request.username
            user.email = request.email

            // If password is not null/blank, encode it.
            // We use !! because passwordEncoder (Java) might return a nullable String! platform type.
            if (!request.password.isNullOrBlank()) {
                user.password = passwordEncoder.encode(request.password)!!
            }

            val updatedUser = userRepository.save(user)
            Response(status = true, message = "User has been updated", data = updatedUser.toResponse())
        } else {
            Response(status = false, message = "User not found", data = null)
        }
    }


    fun edit(id: String): Response {
        val user = userRepository.findByuserHid(id)
        return if (user != null) {
            Response(status = true, message = "User found", data = user.toResponse())
        } else {
            Response(status = false, message = "User not found", data = null)
        }
    }

    fun getAllUsers(): List<UserResponse> {
        return userRepository.findAll().map { it.toResponse() }
    }

    @Transactional
    fun enabled(id: Long): Response {
        val user = userRepository.findByIdOrNull(id)
            ?: return Response(status = false, message = "The user is not found", data = null)

        user.enabled = true
        val savedUser = userRepository.save(user)
        return Response(status = true, message = "The user has been enabled", data = savedUser.toResponse())
    }
}
