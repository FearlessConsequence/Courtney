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
    onTicketClick: (Int) -> Unit,
    onLogoutClick: () -> Unit
) {
    // Заглушка
    val tickets = listOf(
        TicketUI(1, "Не работает принтер", "OPEN", "Ждём ответа от мастера", "12.09 14:30"),
        TicketUI(2, "Проблема с Wi-Fi", "IN_PROGRESS", "Оператор подключился", "10.09 09:15"),
        TicketUI(3, "Сброс пароля", "CLOSED", "Готово", "08.09 16:20")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        // Заголовок, кнопка выхода
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Мои обращения",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onLogoutClick) {
                Text("Выйти")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Список обращений
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tickets) { ticket ->
                TicketCard(
                    ticket = ticket,
                    onClick = { onTicketClick(ticket.id) }
                )
            }
        }
    }
}

// Карточка обращения
@Composable
fun TicketCard(
    ticket: TicketUI,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Строка: заголовок + статус
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = ticket.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                StatusChip(status = ticket.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Последний комментарий
            Text(
                text = ticket.lastComment,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Время обновления
            Text(
                text = ticket.updatedAt,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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