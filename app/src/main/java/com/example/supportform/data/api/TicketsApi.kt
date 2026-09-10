package com.example.supportform.data.api

import com.example.supportform.data.model.TicketDetail
import com.example.supportform.data.model.TicketsResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header

class TicketsApi(
    private val client: HttpClient,
    private val baseUrl: String
) {
    suspend fun getTickets(token: String): TicketsResponse {
        return client.get("$baseUrl/api/tickets") {
            header("Authorization", "Bearer $token")
        }.body()
    }

    suspend fun getTicketDetail(token: String, ticketId: String): TicketDetail {
        return client.get("$baseUrl/api/tickets/$ticketId") {
            header("Authorization", "Bearer $token")
        }.body()
    }
}