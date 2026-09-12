package com.example.supportform.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Ticket(
    val id: String,
    @SerialName("subject") val title: String,
    val description: String,
    val status: String,
    @SerialName("updatedAt") val updatedAt: String,
    @SerialName("userId") val userId: String = "",
    val version: Long = 0,
    @SerialName("lastComment") val lastComment: Comment? = null,
    val createdAt: String? = null,
    val comments: List<Comment>? = null
)

@Serializable
data class TicketDetail(
    val id: String,
    @SerialName("subject") val title: String,
    val description: String,
    val status: String,
    @SerialName("updatedAt") val updatedAt: String,
    val version: Long = 0,
    val comments: List<Comment> = emptyList()
)

@Serializable
data class Comment(
    val id: String,
    val text: String,
    val author: Author,
    @SerialName("createdAt") val createdAt: String,
    val sequence: Long = 0
)

@Serializable
data class Author(
    val id: String,
    val login: String,
    val displayName: String,
    val role: String
)

@Serializable
data class TicketsResponse(
    val items: List<Ticket>
)

@Serializable
data class CommentsResponse(
    val items: List<Comment>
)

@Serializable
data class SendCommentRequest(
    val text: String,
    @SerialName("clientRequestId") val clientRequestId: String
)

@Serializable
data class CommentResponse(
    val id: String = "",
    val text: String = "",
    val author: Author? = null,
    val createdAt: String = "",
    val sequence: Long = 0
)