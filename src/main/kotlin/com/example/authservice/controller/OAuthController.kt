package com.example.authservice.controller

import com.example.authservice.dto.response.Response
import com.example.authservice.repository.ClientRepository
import com.example.authservice.repository.UserRepository
import com.example.authservice.security.ClientAssertionService
import com.example.authservice.security.PkceUtils
import com.example.authservice.service.AuthorizationCodeService
import com.example.authservice.service.ClientPolicyService
import com.example.authservice.service.JwtService
import com.example.authservice.service.RefreshTokenService
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/oauth2")
class OAuthController(
    private val clientRepository: ClientRepository,
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
    private val authorizationCodeService: AuthorizationCodeService,
    private val refreshTokenService: RefreshTokenService,
    private val clientAssertionService: ClientAssertionService,
    private val clientPolicyService: ClientPolicyService
) {

    @GetMapping("/authorize")
    fun authorize(
        @RequestHeader("Authorization") authHeader: String,
        @RequestParam("response_type") responseType: String,
        @RequestParam("client_id") clientId: String,
        @RequestParam("redirect_uri") redirectUri: String,
        @RequestParam("scope", required = false, defaultValue = "") scope: String,
        @RequestParam("state", required = false) state: String?,
        @RequestParam("code_challenge", required = false) codeChallenge: String?,
        @RequestParam("code_challenge_method", required = false) codeChallengeMethod: String?,
        response: HttpServletResponse
    ) {
        if (responseType != "code") throw IllegalArgumentException("response_type must be code")

        val client = clientRepository.findByClientId(clientId) ?: throw IllegalArgumentException("Invalid client")
        if (!clientPolicyService.redirectAllowed(client, redirectUri)) throw IllegalArgumentException("Invalid redirect_uri")
        if (!clientPolicyService.grantAllowed(client, "authorization_code")) throw IllegalArgumentException("Grant not allowed")
        if (!clientPolicyService.scopeAllowed(client, scope)) throw IllegalArgumentException("Scope not allowed")
        if (client.requirePkce && codeChallenge.isNullOrBlank()) throw IllegalArgumentException("PKCE required")

        val token = authHeader.removePrefix("Bearer ").trim()
        if (!jwtService.validateToken(token) || jwtService.extractTokenType(token) != "access") {
            throw IllegalArgumentException("Invalid access token")
        }
        val username = jwtService.extractUsername(token)
        val user = userRepository.findByUsername(username) ?: throw IllegalArgumentException("User not found")
        if (user.client?.clientId != clientId) throw IllegalArgumentException("User not allowed for this client")

        val code = authorizationCodeService.create(
            clientId = clientId,
            username = username,
            redirectUri = redirectUri,
            scope = scope,
            codeChallenge = codeChallenge,
            codeChallengeMethod = codeChallengeMethod
        )

        val location = buildString {
            append(redirectUri)
            append("?code=").append(code)
            if (!state.isNullOrBlank()) {
                append("&state=").append(state)
            }
        }
        response.status = 302
        response.setHeader("Location", location)
    }

    @PostMapping("/token", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun token(
        @RequestParam("grant_type") grantType: String,
        @RequestParam("client_id", required = false) clientIdParam: String?,
        @RequestParam("code", required = false) code: String?,
        @RequestParam("redirect_uri", required = false) redirectUri: String?,
        @RequestParam("code_verifier", required = false) codeVerifier: String?,
        @RequestParam("refresh_token", required = false) refreshToken: String?,
        @RequestParam("scope", required = false, defaultValue = "") scope: String?,
        @RequestHeader("X-Client-Id", required = false) clientIdHeader: String?,
        @RequestHeader("X-Client-Assertion", required = false) clientAssertion: String?
    ): Response {
        val clientId = clientIdHeader ?: clientIdParam ?: ""
        if (grantType == "authorization_code" || grantType == "client_credentials" || grantType == "refresh_token") {
            clientAssertionService.validate(clientId, clientAssertion ?: "")
        }

        val client = clientRepository.findByClientId(clientId) ?: return Response(false, "Invalid client", null)
        if (!clientPolicyService.grantAllowed(client, grantType)) return Response(false, "Grant not allowed", null)

        return when (grantType) {
            "authorization_code" -> {
                if (code.isNullOrBlank() || redirectUri.isNullOrBlank()) return Response(false, "Invalid request", null)
                val authCode = authorizationCodeService.consume(code, clientId, redirectUri)
                if (client.requirePkce) {
                    val ok = PkceUtils.verify(codeVerifier ?: "", authCode.codeChallenge ?: "", authCode.codeChallengeMethod)
                    if (!ok) return Response(false, "Invalid code_verifier", null)
                }
                val user = userRepository.findByUsername(authCode.username)
                    ?: return Response(false, "User not found", null)
                val accessToken = jwtService.generateAccessToken(user)
                val newRefreshToken = refreshTokenService.create(user, clientId)
                val response = mutableMapOf<String, Any>(
                    "token_type" to "Bearer",
                    "access_token" to accessToken,
                    "refresh_token" to newRefreshToken,
                    "expires_in" to 900
                )
                val requestedScope = authCode.scope
                response["scope"] = requestedScope
                if (requestedScope.split(" ").contains("openid")) {
                    response["id_token"] = jwtService.generateIdToken(user)
                }
                Response(true, "Token issued", response)
            }
            "client_credentials" -> {
                if (!clientPolicyService.scopeAllowed(client, scope ?: "")) {
                    return Response(false, "Scope not allowed", null)
                }
                val accessToken = jwtService.generateClientAccessToken(clientId, scope ?: "")
                val response = mapOf(
                    "token_type" to "Bearer",
                    "access_token" to accessToken,
                    "expires_in" to 900,
                    "scope" to (scope ?: "")
                )
                Response(true, "Token issued", response)
            }
            "refresh_token" -> {
                if (refreshToken.isNullOrBlank()) return Response(false, "Invalid request", null)
                val (newRefreshToken, user) = try {
                    refreshTokenService.rotate(refreshToken, clientId)
                } catch (ex: Exception) {
                    return Response(false, "Invalid refresh token", null)
                }
                val accessToken = jwtService.generateAccessToken(user)
                val response = mapOf(
                    "token_type" to "Bearer",
                    "access_token" to accessToken,
                    "refresh_token" to newRefreshToken,
                    "expires_in" to 900
                )
                Response(true, "Token refreshed", response)
            }
            else -> Response(false, "Unsupported grant_type", null)
        }
    }

    @PostMapping("/introspect", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun introspect(
        @RequestParam("token") token: String,
        @RequestHeader("X-Client-Id") clientId: String,
        @RequestHeader("X-Client-Assertion") clientAssertion: String
    ): Map<String, Any> {
        clientAssertionService.validate(clientId, clientAssertion)
        val active = jwtService.validateToken(token)
        if (!active) return mapOf("active" to false)
        val claims = jwtService.getAllClaims(token)
        return mapOf(
            "active" to true,
            "sub" to claims.subject,
            "iss" to claims.issuer,
            "aud" to claims.audience,
            "exp" to claims.expiration.time / 1000,
            "iat" to claims.issuedAt.time / 1000,
            "scope" to (claims["scope"] ?: "")
        )
    }
}
