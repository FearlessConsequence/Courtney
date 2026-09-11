package com.example.supportform.data.api

import com.example.supportform.data.model.Comment
import com.example.supportform.data.model.CommentResponse
import com.example.supportform.data.model.CommentsResponse
import com.example.supportform.data.model.SendCommentRequest
import com.example.supportform.data.model.TicketDetail
import com.example.supportform.data.model.TicketsResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import kotlinx.serialization.json.Json
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

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
        println("📤 Запрашиваем детали тикета: $ticketId")
        val response = client.get("$baseUrl/api/tickets/$ticketId") {
            header("Authorization", "Bearer $token")
        }.body<TicketDetail>()
        println("📦 Получено комментариев: ${response.comments.size}")
        return response
    }

    suspend fun sendComment(token: String, ticketId: String, text: String): CommentResponse {
        val request = SendCommentRequest(
            text = text,
            clientRequestId = java.util.UUID.randomUUID().toString()
        )
        val response = client.post("$baseUrl/api/tickets/$ticketId/comments") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        val raw = response.body<String>()
        println("📦 Ответ на комментарий: $raw")
        return Json { ignoreUnknownKeys = true }.decodeFromString(raw)
    }

    suspend fun getComments(token: String, ticketId: String): List<Comment> {
        println("📤 Запрашиваем комментарии: $ticketId")
        val response = client.get("$baseUrl/api/tickets/$ticketId/comments") {
            header("Authorization", "Bearer $token")
        }.body<CommentsResponse>()
        println("📦 Получено комментариев: ${response.items.size}")
        return response.items
    }
}