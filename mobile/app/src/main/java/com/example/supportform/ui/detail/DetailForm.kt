package com.example.supportform.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.unit.sp
import com.example.supportform.data.model.Comment
import com.example.supportform.data.model.TicketDetail
import com.example.supportform.ui.theme.Teal
import com.example.supportform.ui.theme.Red
import com.example.supportform.ui.theme.GrayBlue

@Composable
fun DetailForm(
    ticket: TicketDetail,
    onBackClick: () -> Unit,
    onSendComment: (String, (Boolean) -> Unit) -> Unit
) {
    val commentText = remember { mutableStateOf("") }
    var sendError by remember { mutableStateOf<String?>(null) }
    var isSending by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .imePadding()
    ) {
        // Верхняя панель
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
                TextButton(
                    onClick = onBackClick,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text("← Назад")
                }

                Text(
                    text = ticket.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                StatusChipDetail(status = ticket.status)

                Spacer(modifier = Modifier.height(12.dp))

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
                .padding(horizontal = 16.dp)
        ) {
            items(ticket.comments) { comment ->
                CommentItem(comment = comment)
            }
        }

        // Ошибка отправки
        if (sendError != null) {
            Text(
                text = sendError!!,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        // Поле ввода
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
                    onValueChange = { commentText.value = it },
                    label = { Text("Введите сообщение...") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 3,
                    enabled = !isSending
                )

                Button(
                    onClick = {
                        if (commentText.value.isNotBlank() && !isSending) {
                            val text = commentText.value
                            isSending = true
                            sendError = null

                            onSendComment(text) { success ->
                                isSending = false
                                if (success) {
                                    commentText.value = ""
                                } else {
                                    sendError = "Не удалось отправить. Попробуйте ещё раз."
                                }
                            }
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Teal
                    ),
                    enabled = !isSending
                ) {
                    if (isSending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White
                        )
                    } else {
                        Text("📤")
                    }
                }
            }
        }
    }
}

@Composable
fun CommentItem(comment: Comment) {
    val isMine = comment.author.role == "CLIENT"
    val alignment = if (isMine) Alignment.End else Alignment.Start
    val color = if (isMine) Teal else GrayBlue

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = alignment
    ) {
        Row(
            horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
        ) {
            Text(
                text = comment.author.displayName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = color
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = formatDate(comment.createdAt),
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Surface(
            color = if (isMine) Teal.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMine) 16.dp else 4.dp,
                bottomEnd = if (isMine) 4.dp else 16.dp
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

@Composable
fun StatusChipDetail(status: String) {
    val color = when (status) {
        "OPEN" -> Red
        "IN_PROGRESS" -> Teal
        "RESOLVED", "CLOSED" -> GrayBlue
        else -> Color.Gray
    }

    Surface(
        color = color,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.wrapContentWidth()
    ) {
        Text(
            text = when (status) {
                "OPEN" -> "Открыто"
                "IN_PROGRESS" -> "В работе"
                "RESOLVED", "CLOSED" -> "Закрыто"
                else -> status
            },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 12.sp,
            color = Color.White,
            maxLines = 1,
            softWrap = false
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