package com.example.authservice.dto.RoleGroup

import com.example.authservice.entity.RoleEntity
import jakarta.validation.constraints.NotBlank

data class RoleRequest(
    @field:NotBlank(message = "Role name is required")
    val name: String,
    val roleGroupId: Long? = null,
    val permissionIds: List<Long> = emptyList()
)

data class RoleResponse(
    val id: Long?,
    val name: String,
    val roleGroupId: Long?,
    val roleGroupName: String?,
    val permissions: List<PermissionSimpleResponse> = emptyList()
)

data class PermissionSimpleResponse(val id: Long?, val name: String?)

fun RoleEntity.toResponse(): RoleResponse {
    return RoleResponse(
        id = this.id,
        name = this.name,
        roleGroupId = this.roleGroup?.id,
        roleGroupName = this.roleGroup?.name,
        permissions = this.permissions.map { PermissionSimpleResponse(it.id, it.name) }
    )
}
