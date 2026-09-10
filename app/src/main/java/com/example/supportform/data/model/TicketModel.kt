package com.example.supportform.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName


@Serializable
data class TicketDetail(
    val id: String,
    val subject: String,
    val description: String,
    val status: String,
    @SerialName("updatedAt") val updatedAt: String,
    val comments:  List<Comment> = emptyList()
)
@Serializable
data class Ticket(
    val id: String,
    @SerialName("subject") val title: String,
    val description: String,
    val status: String,
    @SerialName("updatedAt") val updatedAt: String,
    val createdAt: String? = null,
    val comments: List<Comment>? = null
)

@Serializable
data class Comment(
    val id: String,
    val text: String,
    val author: String,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class TicketsResponse(
    val items: List<Ticket>
)