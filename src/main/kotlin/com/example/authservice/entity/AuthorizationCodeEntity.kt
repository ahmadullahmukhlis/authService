package com.example.authservice.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "authorization_codes")
class AuthorizationCodeEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(unique = true, nullable = false, length = 256)
    var code: String,

    @Column(name = "client_id", nullable = false)
    var clientId: String,

    @Column(name = "username", nullable = false)
    var username: String,

    @Column(name = "redirect_uri", nullable = false, columnDefinition = "TEXT")
    var redirectUri: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    var scope: String,

    @Column(name = "code_challenge", columnDefinition = "TEXT")
    var codeChallenge: String? = null,

    @Column(name = "code_challenge_method", length = 10)
    var codeChallengeMethod: String? = null,

    @Column(name = "expires_at", nullable = false)
    var expiresAt: LocalDateTime,

    @Column(nullable = false)
    var used: Boolean = false
)
