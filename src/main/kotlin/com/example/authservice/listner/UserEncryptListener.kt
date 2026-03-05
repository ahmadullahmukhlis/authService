package com.example.authservice.listner

import com.example.authservice.security.SecureIdGenerator
import com.example.authservice.entity.UserEntity
import jakarta.persistence.PrePersist
import org.springframework.stereotype.Component

@Component
class UserEncryptListener(private val idGenerator: SecureIdGenerator) {

    @PrePersist
    fun generateUserHid(user: UserEntity) {
        if (user.userHid.isEmpty()) {
            user.userHid = idGenerator.generateId()
        }
    }
}
