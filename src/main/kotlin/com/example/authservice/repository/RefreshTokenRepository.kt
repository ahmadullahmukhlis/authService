package com.example.authservice.repository

import com.example.authservice.entity.RefreshTokenEntity
import org.springframework.data.jpa.repository.JpaRepository

interface RefreshTokenRepository : JpaRepository<RefreshTokenEntity, Long> {
    fun findByTokenHash(tokenHash: String): RefreshTokenEntity?
    fun findAllByUser_Id(userId: Long): List<RefreshTokenEntity>
}
