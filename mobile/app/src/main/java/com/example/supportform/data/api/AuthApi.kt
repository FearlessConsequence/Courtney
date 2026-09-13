package com.example.supportform.data.api

import com.example.supportform.data.model.LoginRequest
import com.example.supportform.data.model.LoginResponse
import com.example.supportform.data.model.RefreshRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.call.body
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class AuthApi(
    private val client: HttpClient,
    private val baseUrl: String
) {
    suspend fun login(username: String, password: String): LoginResponse {
        val response = client.post("$baseUrl/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(username, password))
        }

        if (response.status.value == 401) {
            throw Exception("Неверный логин или пароль")
        }
        if (response.status.value == 400) {
            throw Exception("Проверьте логин и пароль")
        }
        if (!response.status.isSuccess()) {
            throw Exception("Ошибка сервера: ${response.status.value}")
        }

        return response.body()
    }

    suspend fun refresh(refreshToken: String): LoginResponse {
        return client.post("$baseUrl/api/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(RefreshRequest(refreshToken))
        }.body()
    }
}