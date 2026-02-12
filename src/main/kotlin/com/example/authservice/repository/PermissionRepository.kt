package com.example.authservice.repository

import com.example.authservice.entity.PermissionEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PermissionRepository : JpaRepository<PermissionEntity, Long> {

    /**
     * Find a permission by its unique name (e.g., 'READ_PRIVILEGE')
     */
    fun findByName(name: String): PermissionEntity?

    /**
     * Checks if a permission exists by name
     */
    fun existsByName(name: String): Boolean
}
