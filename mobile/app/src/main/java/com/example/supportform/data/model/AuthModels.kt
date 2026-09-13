package com.example.supportform.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val login: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    @SerialName("accessTokenExpiresAt") val accessTokenExpiresAt: String,
    @SerialName("refreshTokenExpiresAt") val refreshTokenExpiresAt: String,
    val user: User,
    @SerialName("tokenType") val tokenType: String
)

@Serializable
data class User(
    val id: String,
    val login: String,
    @SerialName("displayName") val displayName: String,
    val role: String
)
@Serializable
data class RefreshRequest(
    val refreshToken: String
)