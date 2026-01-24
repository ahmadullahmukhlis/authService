package com.example.authservice.controller

import com.example.authservice.dto.user.CreateUserRequest
import com.example.authservice.service.JwtService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class LoginController (private val  jwtService: JwtService) {
    @PostMapping
    fun createUser(@Valid @RequestBody request: CreateUserRequest): Response {}
}