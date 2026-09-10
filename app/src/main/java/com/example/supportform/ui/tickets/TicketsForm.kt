package com.example.supportclient.ui.tickets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.example.supportform.data.model.Ticket

// Модель
data class TicketUI(
    val id: Int,
    val title: String,
    val status: String,
    val lastComment: String,
    val updatedAt: String
)

@Composable
fun TicketsForm(
    onTicketClick: (String) -> Unit,
    onLogoutClick: () -> Unit,
    tickets: List<Ticket> // теперь принимает список
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Мои обращения", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            TextButton(onClick = onLogoutClick) { Text("Выйти") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(tickets) { ticket ->
                TicketCard(
                    ticket = ticket,
                    onClick = { onTicketClick(ticket.id) }
                )
            }
        }
    }
}

@Composable
fun TicketCard(ticket: Ticket, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(ticket.title, fontWeight = FontWeight.Medium)
            Text("Статус: ${translateStatus(ticket.status)}")
            Text("Обновлено: ${formatDate(ticket.updatedAt)}")
        }
    }
}

// Чип статуса
@Composable
fun StatusChip(status: String) {
    val color = when (status) {
        "OPEN" -> Color(0xFFD50032)
        "IN_PROGRESS" -> Color(0xFF008C95)
        "CLOSED" -> Color(0xFF647882)
        else -> Color.Gray
    }

    Surface(
        color = color,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = status.replace("_", " "),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 12.sp,
            color = Color.White
        )
    }
}

fun formatDate(dateStr: String): String {
    return try {
        val input = java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
        val output = java.time.format.DateTimeFormatter.ofPattern("dd.MM HH:mm")
        val parsed = java.time.OffsetDateTime.parse(dateStr, input)
        parsed.format(output)
    } catch (e: Exception) {
        dateStr
    }
}

fun translateStatus(status: String): String {
    return when (status) {
        "OPEN" -> "Открыто"
        "IN_PROGRESS" -> "В работе"
        "RESOLVED" -> "Закрыто"
        "CLOSED" -> "Закрыто"
        else -> status
    }
}