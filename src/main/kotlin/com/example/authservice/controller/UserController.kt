package com.example.authservice.controller

import com.example.authservice.dto.response.Response
import com.example.authservice.dto.user.CreateUserRequest
import com.example.authservice.dto.user.UserResponse
import com.example.authservice.dto.user.UpdateUserRequest
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

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun createUserJson(
        @RequestHeader("X-Client-Id") clientId: String,
        @RequestHeader("X-Client-Assertion") clientAssertion: String,
        @Valid @RequestBody request: CreateUserRequest
    ): Response {
        clientAssertionService.validate(clientId, clientAssertion)
        val finalRequest = if (request.clientId.isNullOrBlank()) {
            request.copy(clientId = clientId)
        } else {
            request
        }
        return userService.createUser(finalRequest, null)
    }

    // CREATE USER WITH PHOTO (multipart form fields)
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createUserForm(
        @RequestHeader("X-Client-Id") clientId: String,
        @RequestHeader("X-Client-Assertion") clientAssertion: String,
        @Valid @ModelAttribute request: CreateUserRequest,
        @RequestParam("photo", required = false) photo: MultipartFile?
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

    @PutMapping("/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun updateUserJson(
        @RequestHeader("X-Client-Id") clientId: String,
        @RequestHeader("X-Client-Assertion") clientAssertion: String,
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateUserRequest
    ): Response {
        clientAssertionService.validate(clientId, clientAssertion)
        val finalRequest = if (request.clientId.isNullOrBlank()) {
            request.copy(clientId = clientId)
        } else {
            request
        }
        return userService.update(id, finalRequest)
    }

    @PutMapping("/{id}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun updateUserForm(
        @RequestHeader("X-Client-Id") clientId: String,
        @RequestHeader("X-Client-Assertion") clientAssertion: String,
        @PathVariable id: Long,
        @Valid @ModelAttribute request: UpdateUserRequest,
        @RequestParam("photo", required = false) photo: MultipartFile?
    ): Response {
        clientAssertionService.validate(clientId, clientAssertion)
        val finalRequest = if (request.clientId.isNullOrBlank()) {
            request.copy(clientId = clientId)
        } else {
            request
        }
        return userService.update(id, finalRequest, photo)
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
