package com.example.supportform.ui

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.supportclient.ui.tickets.TicketsForm
import com.example.supportform.data.api.NetworkModule
import com.example.supportform.data.model.Ticket
import com.example.supportform.data.model.TicketDetail
import com.example.supportform.ui.detail.DetailForm
import com.example.supportform.ui.login.LoginForm
import com.example.supportform.ui.login.LoginViewModel
import com.example.supportform.utils.TokenManager
import androidx.compose.runtime.rememberCoroutineScope
import com.example.supportform.data.api.AuthApi
import com.example.supportform.utils.RefreshManager
import kotlinx.coroutines.launch


suspend fun <T> withRefresh(
    tokenManager: TokenManager,
    authApi: AuthApi,
    navController: androidx.navigation.NavController,
    block: suspend (String) -> T
): T? {
    var token = tokenManager.getAccessToken() ?: return null
    return try {
        block(token)
    } catch (e: Exception) {
        // Если упало — пробуем refresh и повторяем один раз
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
            // Refresh не удался — на экран входа
            tokenManager.clearTokens()
            navController.navigate("login") {
                popUpTo("login") { inclusive = true }
            }
            null
        }
    }
}

@Composable
fun AppNavigation(context: Context) {
    val navController = rememberNavController()
    val authApi = NetworkModule.provideAuthApi()
    val tokenManager = TokenManager(context)
    val viewModel = LoginViewModel(authApi, tokenManager)

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginForm(
                viewModel = viewModel,
                onLoginSuccess = {
                    navController.navigate("tickets") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("tickets") {
            var tickets by remember { mutableStateOf<List<Ticket>>(emptyList()) }
            var isLoading by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                withRefresh(tokenManager, authApi, navController) { token ->
                    val api = NetworkModule.provideTicketsApi()
                    val response = api.getTickets(token)
                    tickets = response.items
                    println("📦 Получено ${response.items.size} обращений")
                }
                isLoading = false
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                TicketsForm(
                    tickets = tickets,
                    onTicketClick = { ticketId ->
                        println("Клик по тикету: $ticketId")
                        navController.navigate("detail/$ticketId")
                    },
                    onLogoutClick = {
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
            var detail by remember { mutableStateOf<TicketDetail?>(null) }
            var isLoading by remember { mutableStateOf(true) }

            LaunchedEffect(ticketId) {
                withRefresh(tokenManager, authApi, navController) { token ->
                    val api = NetworkModule.provideTicketsApi()
                    val ticket = api.getTicketDetail(token, ticketId)
                    val comments = api.getComments(token, ticketId)
                    detail = ticket.copy(comments = comments)
                    println("✅ Загружено: ${comments.size} комментариев")
                }
                isLoading = false
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (detail != null) {
                val scope = rememberCoroutineScope()

                DetailForm(
                    ticket = detail!!,
                    onBackClick = {
                        navController.navigate("tickets") {
                            popUpTo("tickets") { inclusive = true }
                        }
                    },
                    onSendComment = { text ->
                        scope.launch {
                            withRefresh(tokenManager, authApi, navController) { token ->
                                val api = NetworkModule.provideTicketsApi()
                                api.sendComment(token, ticketId, text)
                                val ticket = api.getTicketDetail(token, ticketId)
                                val comments = api.getComments(token, ticketId)
                                detail = ticket.copy(comments = comments)
                                println("✅ Комментарий отправлен")
                            }
                        }
                    }
                )
            }
        }
    }
}