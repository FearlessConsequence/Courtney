package com.example.supportform.ui

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.supportclient.ui.tickets.TicketsForm
import com.example.supportform.data.api.AuthApi
import com.example.supportform.data.api.NetworkModule
import com.example.supportform.data.api.UnauthorizedException
import com.example.supportform.data.api.WebSocketManager
import com.example.supportform.data.database.AppDatabase
import com.example.supportform.data.model.TicketDetail
import com.example.supportform.data.repository.TicketRepository
import com.example.supportform.data.repository.toModel
import com.example.supportform.ui.detail.DetailForm
import com.example.supportform.ui.login.LoginForm
import com.example.supportform.ui.login.LoginViewModel
import com.example.supportform.utils.RefreshManager
import com.example.supportform.utils.TokenManager
import kotlinx.coroutines.launch


suspend fun <T> withRefresh(
    tokenManager: TokenManager,
    authApi: AuthApi,
    navController: androidx.navigation.NavController,
    block: suspend (String) -> T
): T? {
    val token = tokenManager.getAccessToken() ?: return null
    return try {
        block(token)
    } catch (e: Exception) {
        if (!isUnauthorized(e)) {
            println("⚠️ Сетевая ошибка, оставляем токен: ${e.message}")
            return null
        }

        val refreshed = RefreshManager.refreshIfNeeded(tokenManager, authApi, token)
        if (refreshed) {
            val newToken = tokenManager.getAccessToken()
            if (newToken != null) {
                try {
                    block(newToken)
                } catch (e2: Exception) {
                    println("❌ Повторный запрос не удался: ${e2.message}")
                    null
                }
            } else null
        } else {
            tokenManager.clearTokens()
            navController.navigate("login") {
                popUpTo("login") { inclusive = true }
            }
            null
        }
    }
}

fun isUnauthorized(e: Exception): Boolean {
    return e is UnauthorizedException
}


@Composable
fun AppNavigation(context: Context) {
    val navController = rememberNavController()
    val authApi = NetworkModule.provideAuthApi()
    val tokenManager = TokenManager(context)
    val viewModel = LoginViewModel(authApi, tokenManager)
    val db = remember { AppDatabase.getInstance(context) }
    val ticketsApi = remember { NetworkModule.provideTicketsApi() }
    val repository = remember { TicketRepository(ticketsApi, db) }

    val wsManager = remember { WebSocketManager("ws://10.0.2.2:8080") }
    val scope = rememberCoroutineScope()

    val startDestination = if (tokenManager.getAccessToken() != null) "tickets" else "login"

    // Подключение WebSocket один раз
    LaunchedEffect(Unit) {
        val token = tokenManager.getAccessToken()
        if (token != null) {
            wsManager.connect(
                scope = scope,
                getToken = { tokenManager.getAccessToken() },
                onCommentCreated = { ticketId ->
                    scope.launch {
                        try {
                            val tk = tokenManager.getAccessToken() ?: return@launch
                            repository.refreshTicketDetail(tk, ticketId)
                        } catch (e: Exception) {
                            println("❌ Ошибка обновления: ${e.message}")
                        }
                    }
                },
                onStatusChanged = { ticketId ->
                    scope.launch {
                        try {
                            val tk = tokenManager.getAccessToken() ?: return@launch
                            repository.refreshTicketDetail(tk, ticketId)
                        } catch (e: Exception) {
                            println("❌ Ошибка обновления: ${e.message}")
                        }
                    }
                }
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            wsManager.disconnect()
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            LoginForm(
                viewModel = viewModel,
                onLoginSuccess = {
                    val token = tokenManager.getAccessToken()
                    if (token != null) {
                        wsManager.connect(
                            scope = scope,
                            getToken = { tokenManager.getAccessToken() },
                            onCommentCreated = { ticketId ->
                                scope.launch {
                                    try {
                                        val tk = tokenManager.getAccessToken() ?: return@launch
                                        repository.refreshTicketDetail(tk, ticketId)
                                    } catch (e: Exception) {
                                        println("❌ Ошибка обновления: ${e.message}")
                                    }
                                }
                            },
                            onStatusChanged = { ticketId ->
                                scope.launch {
                                    try {
                                        val tk = tokenManager.getAccessToken() ?: return@launch
                                        repository.refreshTicketDetail(tk, ticketId)
                                    } catch (e: Exception) {
                                        println("❌ Ошибка обновления: ${e.message}")
                                    }
                                }
                            }
                        )
                    }
                    navController.navigate("tickets")
                }
            )
        }

        composable("tickets") {
            var isLoading by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                withRefresh(tokenManager, authApi, navController) { token ->
                    repository.refreshTickets(token)
                }
                isLoading = false
            }

            val ticketsFlow = remember { repository.getTicketsFlow() }
            val tickets by ticketsFlow.collectAsState(initial = emptyList())

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                TicketsForm(
                    tickets = tickets.map { it.toModel() },
                    onTicketClick = { ticketId ->
                        navController.navigate("detail/$ticketId")
                    },
                    onLogoutClick = {
                        wsManager.disconnect()
                        tokenManager.clearTokens()
                        navController.navigate("login") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }
        }

        composable("detail/{ticketId}") { backStackEntry ->
            val ticketId = backStackEntry.arguments?.getString("ticketId") ?: return@composable
            var isLoading by remember { mutableStateOf(true) }

            LaunchedEffect(ticketId) {
                withRefresh(tokenManager, authApi, navController) { token ->
                    repository.refreshTicketDetail(token, ticketId)
                }
                isLoading = false
            }

            val ticketFlow = remember(ticketId) { repository.getTicketFlow(ticketId) }
            val commentsFlow = remember(ticketId) { repository.getCommentsFlow(ticketId) }

            val ticket by ticketFlow.collectAsState(initial = null)
            val comments by commentsFlow.collectAsState(initial = emptyList())

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (ticket != null) {
                val detail = TicketDetail(
                    id = ticket!!.id,
                    title = ticket!!.title,
                    description = ticket!!.description,
                    status = ticket!!.status,
                    updatedAt = ticket!!.updatedAt,
                    version = ticket!!.version,
                    comments = comments.map { it.toModel() }
                )

                DetailForm(
                    ticket = detail,
                    onBackClick = {
                        navController.navigate("tickets") {
                            popUpTo("tickets") { inclusive = true }
                        }
                    },
                    onSendComment = { text, onResult ->
                        scope.launch {
                            val result = withRefresh(tokenManager, authApi, navController) { token ->
                                repository.sendComment(token, ticketId, text)
                            }
                            onResult(result != null)
                        }
                    }
                )
            }
        }
    }
}