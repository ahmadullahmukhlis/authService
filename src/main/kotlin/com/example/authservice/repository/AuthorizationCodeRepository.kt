package com.example.authservice.repository

import com.example.authservice.entity.AuthorizationCodeEntity
import org.springframework.data.jpa.repository.JpaRepository

interface AuthorizationCodeRepository : JpaRepository<AuthorizationCodeEntity, Long> {
    fun findByCode(code: String): AuthorizationCodeEntity?
}
