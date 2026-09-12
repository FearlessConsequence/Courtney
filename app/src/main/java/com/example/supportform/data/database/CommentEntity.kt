package com.example.supportform.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey val id: String,
    val ticketId: String,
    val text: String,
    val authorName: String,
    val authorRole: String,
    val sequence: Long,
    val createdAt: String
)