package com.example.authservice.dto.user

import com.example.authservice.entity.UserEntity
import java.time.LocalDateTime

data class UserResponse (
    val id: String? = null,
    val firstName : String,
    val lastName : String,
    val photo: String?=null,
    val username: String,
    val email: String,
    val enabled: Boolean,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?,

)

fun UserEntity.toResponse(): UserResponse {
    return UserResponse(
        id = this.userHid,
        firstName = this.FirstName,
        lastName = this.LastName,
        username = this.username,
        photo = this.Photo,
        email = this.email,
        enabled = this.enabled,    // Error was here
        createdAt = this.createdAt, // Error was here
        updatedAt = this.updatedAt  // Error was here
    )
}
