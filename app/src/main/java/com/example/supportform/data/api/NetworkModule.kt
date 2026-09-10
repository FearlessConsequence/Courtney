package com.example.supportform.data.api

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object NetworkModule {
    private const val BASE_URL = "http://10.0.2.2:8080"

    fun provideHttpClient(): HttpClient {
        return HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }
    }

    fun provideAuthApi(): AuthApi {
        return AuthApi(provideHttpClient(), BASE_URL)
    }

    fun provideTicketsApi(): TicketsApi {
        return TicketsApi(provideHttpClient(), BASE_URL)
    }
}