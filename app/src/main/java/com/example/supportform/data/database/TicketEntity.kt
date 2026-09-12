package com.example.supportform.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tickets")
data class TicketEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val description: String,
    val status: String,
    val updatedAt: String,
    val version: Long,
    val lastComment: String?
)