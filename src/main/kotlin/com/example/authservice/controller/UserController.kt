package com.example.authservice.controller

import com.example.authservice.dto.response.Response
import com.example.authservice.dto.user.CreateUserRequest
import com.example.authservice.dto.user.UserResponse
import com.example.authservice.service.UserService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {

    /**
     * POST /api/users
     * Returns the Response object directly.
     * Spring handles the 200 OK status by default.
     */
    @PostMapping
    fun createUser(@Valid @RequestBody request: CreateUserRequest): Response {
        return userService.createUser(request)
    }

    /**
     * GET /api/users
     * Returns the List of UserResponse directly.
     */
    @GetMapping
    fun getAllUsers(): List<UserResponse> {
        return userService.getAllUsers()
    }

    /**
     * GET /api/users/{id}/edit
     */
    @GetMapping("/{id}/edit")
    fun editUser(@PathVariable id: String): Response {
        return userService.edit(id)
    }

    /**
     * PUT /api/users/{id}
     */
    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable id: Long,
        @Valid @RequestBody request: CreateUserRequest
    ): Response {
        return userService.update(id, request)
    }

    /**
     * PATCH /api/users/{id}/enable
     */
    @PatchMapping("/{id}/enable")
    fun enableUser(@PathVariable id: Long): Response {
        return userService.enabled(id)
    }
}
