package com.example.authservice.controller

import com.example.authservice.dto.response.Response
import com.example.authservice.dto.user.CreateUserRequest
import com.example.authservice.dto.user.UserResponse
import com.example.authservice.security.ClientAssertionService
import com.example.authservice.service.UserService
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService,
    private val clientAssertionService: ClientAssertionService
) {

    // CREATE USER WITH PHOTO
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createUser(
        @RequestHeader("X-Client-Id") clientId: String,
        @RequestHeader("X-Client-Assertion") clientAssertion: String,
        @Valid @RequestPart("user") request: CreateUserRequest,
        @RequestPart("photo", required = false) photo: MultipartFile?
    ): Response {
        clientAssertionService.validate(clientId, clientAssertion)
        val finalRequest = if (request.clientId.isNullOrBlank()) {
            request.copy(clientId = clientId)
        } else {
            request
        }
        return userService.createUser(finalRequest, photo)
    }

    @GetMapping
    fun getAllUsers(
        @RequestHeader("X-Client-Id") clientId: String,
        @RequestHeader("X-Client-Assertion") clientAssertion: String
    ): List<UserResponse> {
        clientAssertionService.validate(clientId, clientAssertion)
        return userService.getAllUsers(clientId)
    }

    @GetMapping("/{id}/edit")
    fun editUser(
        @RequestHeader("X-Client-Id") clientId: String,
        @RequestHeader("X-Client-Assertion") clientAssertion: String,
        @PathVariable id: String
    ): Response {
        clientAssertionService.validate(clientId, clientAssertion)
        return userService.edit(id, clientId)
    }

    @PutMapping("/{id}")
    fun updateUser(
        @RequestHeader("X-Client-Id") clientId: String,
        @RequestHeader("X-Client-Assertion") clientAssertion: String,
        @PathVariable id: Long,
        @Valid @RequestBody request: CreateUserRequest
    ): Response {
        clientAssertionService.validate(clientId, clientAssertion)
        val finalRequest = if (request.clientId.isNullOrBlank()) {
            request.copy(clientId = clientId)
        } else {
            request
        }
        return userService.update(id, finalRequest)
    }

    @PatchMapping("/{id}/enable")
    fun enableUser(
        @RequestHeader("X-Client-Id") clientId: String,
        @RequestHeader("X-Client-Assertion") clientAssertion: String,
        @PathVariable id: Long
    ): Response {
        clientAssertionService.validate(clientId, clientAssertion)
        return userService.enabled(id)
    }
}
