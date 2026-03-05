package com.example.authservice.entity

import jakarta.persistence.*

@Entity
@Table(name = "clients")
class ClientEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(unique = true, nullable = false)
    var name: String,

    @Column(name = "client_hid", nullable = false, unique = true, length = 512)
    var clientId: String = "",

    @Column(nullable = false, columnDefinition = "TEXT")
    var publicKey: String = "",

    @Column(nullable = false, columnDefinition = "TEXT")
    var privateKey: String = "",

    @Column(name = "redirect_uris", columnDefinition = "TEXT")
    var redirectUris: String? = null,

    @Column(name = "allowed_grant_types", columnDefinition = "TEXT")
    var allowedGrantTypes: String? = null,

    @Column(name = "allowed_scopes", columnDefinition = "TEXT")
    var allowedScopes: String? = null,

    @Column(name = "require_pkce", nullable = false)
    var requirePkce: Boolean = true,

    @OneToMany(mappedBy = "client", fetch = FetchType.LAZY)
    var users: List<UserEntity> = emptyList()
)
