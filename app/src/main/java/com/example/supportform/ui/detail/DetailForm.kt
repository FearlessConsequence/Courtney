package com.example.supportform.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.supportform.ui.theme.GrayBlue
import com.example.supportform.ui.theme.Red
import com.example.supportform.ui.theme.Teal

// модели
data class CommentUI(
    val id: Int,
    val author: String,
    val text: String,
    val time: String,
    val isMine: Boolean
)

data class TicketDetailUI(
    val id: Int,
    val title: String,
    val description: String,
    val status: String,
    val comments: List<CommentUI>
)

@Composable
fun DetailForm(
    onBackClick: () -> Unit,
    onSendComment: (String) -> Unit
) {
    // заглушки
    val ticket = TicketDetailUI(
        id = 1,
        title = "Не работает принтер",
        description = "Не могу распечатать документ, принтер не реагирует.",
        status = "OPEN",
        comments = listOf(
            CommentUI(1, "Оператор", "Здравствуйте! Проверьте подключение к сети.", "12.09 12:30", false),
            CommentUI(2, "Вы", "Проверил, всё подключено.", "12.09 12:35", true),
            CommentUI(3, "Оператор", "Вызовите мастера по телефону 123.", "12.09 12:40", false)
        )
    )

    val commentText = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Кнопка "назад", заголовок, статус обращения
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 4.dp,
            tonalElevation = 3.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Кнопка назад
                TextButton(
                    onClick = onBackClick,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text("← Назад")
                }

                // Заголовок и статус
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = ticket.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    StatusChipDetail(status = ticket.status)
                }

                // Описание
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = ticket.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Список комментариев
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            reverseLayout = false
        ) {
            items(ticket.comments) { comment ->
                CommentItem(comment = comment)
            }
        }

        // Поле ввода комментария
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 8.dp,
            tonalElevation = 3.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = commentText.value,
                    onValueChange = { newText -> commentText.value = newText },
                    label = { Text("Введите сообщение...") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 3
                )

                Button(
                    onClick = {
                        if (commentText.value.isNotBlank()) {
                            onSendComment(commentText.value)
                            commentText.value = ""
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Teal
                    )
                ) {
                    Text("📤")
                }
            }
        }
    }
}

// Комментарий
@Composable
fun CommentItem(comment: CommentUI) {
    val alignment = if (comment.isMine) Alignment.End else Alignment.Start
    val color = if (comment.isMine) Teal else GrayBlue

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = alignment
    ) {
        // Автор сообщения и время
        Row(
            horizontalArrangement = if (comment.isMine) Arrangement.End else Arrangement.Start
        ) {
            Text(
                text = comment.author,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = color
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = comment.time,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Текст комментария
        Surface(
            color = if (comment.isMine) Teal.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (comment.isMine) 16.dp else 4.dp,
                bottomEnd = if (comment.isMine) 4.dp else 16.dp
            ),
            modifier = Modifier
                .widthIn(max = 280.dp)
                .padding(8.dp)
        ) {
            Text(
                text = comment.text,
                fontSize = 14.sp,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

// Чип статуса
@Composable
fun StatusChipDetail(status: String) {
    val color = when (status) {
        "OPEN" -> Red
        "IN_PROGRESS" -> Teal
        "CLOSED" -> GrayBlue
        else -> Color.Gray
    }

    Surface(
        color = color,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = when (status) {
                "OPEN" -> "Открыто"
                "IN_PROGRESS" -> "В работе"
                "CLOSED" -> "Закрыто"
                else -> status
            },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 12.sp,
            color = Color.White
        )
    }
}