package com.example.supportform.data.repository

import com.example.supportform.data.api.TicketsApi
import com.example.supportform.data.database.AppDatabase
import com.example.supportform.data.database.CommentEntity
import com.example.supportform.data.database.TicketEntity
import com.example.supportform.data.model.Author
import com.example.supportform.data.model.Comment
import com.example.supportform.data.model.Ticket
import kotlinx.coroutines.flow.Flow

class TicketRepository(
    private val api: TicketsApi,
    private val db: AppDatabase
) {
    private val ticketDao = db.ticketDao()
    private val commentDao = db.commentDao()

    fun getTicketsFlow(): Flow<List<TicketEntity>> = ticketDao.getAllTickets()

    fun getTicketFlow(ticketId: String): Flow<TicketEntity?> =
        ticketDao.getTicketById(ticketId)

    fun getCommentsFlow(ticketId: String): Flow<List<CommentEntity>> =
        commentDao.getCommentsByTicket(ticketId)

    suspend fun refreshTickets(token: String) {
        val response = api.getTickets(token)
        val entities = response.items.map { it.toEntity() }
        ticketDao.insertTickets(entities)
        println("💾 Сохранили ${entities.size} обращений в Room")
    }

    suspend fun refreshTicketDetail(token: String, ticketId: String) {
        val ticket = api.getTicketDetail(token, ticketId)
        val comments = api.getComments(token, ticketId)

        ticketDao.insertTicket(ticket.toEntity())
        commentDao.insertComments(comments.map { it.toEntity(ticketId) })
        println("💾 Сохранили тикет и ${comments.size} комментариев в Room")
    }

    suspend fun sendComment(token: String, ticketId: String, text: String) {
        val comment = api.sendComment(token, ticketId, text)
        commentDao.insertComment(comment.toEntity(ticketId))
        println("💾 Сохранили новый комментарий в Room")
    }
}


fun Ticket.toEntity() = TicketEntity(
    id = id,
    userId = userId,
    title = title,
    description = description,
    status = status,
    updatedAt = updatedAt,
    version = version,
    lastComment = lastComment?.text
)

fun com.example.supportform.data.model.TicketDetail.toEntity() = TicketEntity(
    id = id,
    userId = "",
    title = title,
    description = description,
    status = status,
    updatedAt = updatedAt,
    version = version,
    lastComment = null
)

fun Comment.toEntity(ticketId: String) = CommentEntity(
    id = id,
    ticketId = ticketId,
    text = text,
    authorName = author.displayName,
    authorRole = author.role,
    sequence = sequence,
    createdAt = createdAt
)

fun TicketEntity.toModel() = Ticket(
    id = id,
    userId = userId,
    title = title,
    description = description,
    status = status,
    updatedAt = updatedAt,
    version = version,
    lastComment = null,
    createdAt = null,
    comments = null
)

fun CommentEntity.toModel() = Comment(
    id = id,
    text = text,
    author = Author(
        id = "",
        login = "",
        displayName = authorName,
        role = authorRole
    ),
    createdAt = createdAt,
    sequence = sequence
)