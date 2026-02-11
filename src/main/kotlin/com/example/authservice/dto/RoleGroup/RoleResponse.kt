package com.example.authservice.dto.RoleGroup;


import com.example.authservice.entity.RoleEntity

import java.util.List;

data class RoleResponse(
    val id: Long?,
    val name: String,
    val roleGroupId: Long?,
    val roleGroupName: String?,
    val permissions:List<PermissionSimpleResponse>= emptyList()
)

data class PermissionSimpleResponse(
    val id: Long?,
    val name: String
)

/**
 * Extension function to convert RoleEntity to RoleResponse.
 * This fixed version ensures non-null values for the response fields.
 */
fun RoleEntity.toResponse(): RoleResponse {
    return RoleResponse(
        id = this.id,
        name = this.name,
        roleGroupId = this.roleGroup?.id,
        // provide a fallback name if the group is null
        roleGroupName = this.roleGroup?.name ?: "No Group Assigned",
        // safely map the permissions set to a list of DTOs
        permissions = this.permissions.map { permission ->
            PermissionSimpleResponse(
                id = permission.id,
                name = permission.name ?: "Unknown Permission"
            )
        }
    )
}
