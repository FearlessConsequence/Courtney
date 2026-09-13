package com.example.supportform.data.api

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.header
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

class WebSocketManager(
    private val baseUrl: String
) {
    private var job: Job? = null
    private var isConnected = false

    fun connect(
        scope: CoroutineScope,
        getToken: suspend () -> String?,
        onCommentCreated: (ticketId: String) -> Unit,
        onStatusChanged: (ticketId: String) -> Unit
    ) {
        if (isConnected) return
        isConnected = true

        job = scope.launch {
            var delayMs = 1000L
            while (isActive) {
                try {
                    val currentToken = getToken()
                    if (currentToken == null) {
                        println("⚠️ Нет токена для WS, ждём")
                        delay(2000)
                        continue
                    }

                    println("🔌 Подключаемся к WebSocket")
                    val client = HttpClient { install(WebSockets) }
                    client.webSocket(
                        urlString = "$baseUrl/ws",
                        request = {
                            header("Authorization", "Bearer $currentToken")
                        }
                    ) {
                        println("✅ WebSocket подключён")
                        delayMs = 1000L

                        for (frame in incoming) {
                            if (frame is Frame.Text) {
                                val text = frame.readText()
                                println("📨 WS: $text")
                                handleEvent(text, onCommentCreated, onStatusChanged)
                            }
                        }
                    }
                } catch (e: ClosedReceiveChannelException) {
                    println("❌ WS закрыт")
                } catch (e: Exception) {
                    println("❌ WS ошибка: ${e.message}")
                }

                isConnected = false
                println("⏳ Переподключение через $delayMs мс")
                delay(delayMs)
                delayMs = (delayMs * 2).coerceAtMost(30_000L)
            }
        }
    }

    private fun handleEvent(
        json: String,
        onCommentCreated: (String) -> Unit,
        onStatusChanged: (String) -> Unit
    ) {
        try {
            val obj = Json.parseToJsonElement(json)
            val type = obj.jsonObject["type"]?.toString()?.replace("\"", "") ?: return
            val ticketObj = obj.jsonObject["ticket"]
            val ticketId = ticketObj?.jsonObject?.get("id")?.toString()?.replace("\"", "") ?: return

            when (type) {
                "comment.created" -> {
                    println("📨 Новый комментарий в тикете $ticketId")
                    onCommentCreated(ticketId)
                }
                "ticket.status_changed" -> {
                    println("📨 Статус тикета $ticketId изменён")
                    onStatusChanged(ticketId)
                }
            }
        } catch (e: Exception) {
            println("❌ Ошибка парсинга WS: ${e.message}")
        }
    }

    fun disconnect() {
        job?.cancel()
        job = null
        isConnected = false
        println("🔌 WebSocket отключён")
    }
}