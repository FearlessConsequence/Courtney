package com.example.supportform.data.api

import com.example.supportform.data.model.Comment
import com.example.supportform.data.model.CommentsResponse
import com.example.supportform.data.model.SendCommentRequest
import com.example.supportform.data.model.TicketDetail
import com.example.supportform.data.model.TicketsResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType

class TicketsApi(
    private val client: HttpClient,
    private val baseUrl: String
) {

    suspend fun getTickets(token: String): TicketsResponse {
        val response = client.get("$baseUrl/api/tickets") {
            header("Authorization", "Bearer $token")
        }
        checkStatus(response)
        return response.body()
    }

    suspend fun getTicketDetail(token: String, ticketId: String): TicketDetail {
        println("📤 Запрашиваем детали тикета: $ticketId")
        val response = client.get("$baseUrl/api/tickets/$ticketId") {
            header("Authorization", "Bearer $token")
        }
        checkStatus(response)
        return response.body()
    }

    suspend fun getComments(token: String, ticketId: String): List<Comment> {
        println("📤 Запрашиваем комментарии: $ticketId")
        val response = client.get("$baseUrl/api/tickets/$ticketId/comments") {
            header("Authorization", "Bearer $token")
        }
        checkStatus(response)
        val body: CommentsResponse = response.body()
        println("📦 Получено комментариев: ${body.items.size}")
        return body.items
    }

    suspend fun sendComment(token: String, ticketId: String, text: String): Comment {
        val response = client.post("$baseUrl/api/tickets/$ticketId/comments") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(SendCommentRequest(
                text = text,
                clientRequestId = java.util.UUID.randomUUID().toString()
            ))
        }
        checkStatus(response)
        return response.body()
    }

    // Проверка HTTP-статуса перед парсингом
    private fun checkStatus(response: HttpResponse) {
        when (response.status.value) {
            401 -> throw UnauthorizedException()
            in 500..599 -> throw Exception("Server error: ${response.status.value}")
        }
    }
}