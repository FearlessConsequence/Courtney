package com.example.supportform.data.api

import com.example.supportform.data.model.LoginRequest
import com.example.supportform.data.model.LoginResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.call.body
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AuthApi(
    private val client: HttpClient,
    private val baseUrl: String
) {
    suspend fun login(username: String, password: String): LoginResponse {
        return client.post("$baseUrl/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(username, password))
        }.body()
    }
}