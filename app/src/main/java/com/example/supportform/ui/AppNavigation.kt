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
                onLoginClick = { username, password ->
                    viewModel.login(username, password)
                }
            )

            val state by viewModel.state.collectAsState()
            LaunchedEffect(state.isSuccess) {
                if (state.isSuccess) {
                    navController.navigate("tickets") {
                        popUpTo("login") { inclusive = true }
                    }
                    viewModel.resetState()
                }
            }
        }

        composable("tickets") {
            val ticketsApi = NetworkModule.provideTicketsApi()
            val token = tokenManager.getAccessToken()
            var tickets by remember { mutableStateOf<List<Ticket>>(emptyList()) }
            var isLoading by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                val token = tokenManager.getAccessToken()
                println("🔑 Токен перед запросом списка: ${token?.take(20)}...")

                if (token != null) {
                    try {
                        println("📤 Запрашиваем список...")
                        val response = ticketsApi.getTickets(token)
                        println("📦 Получено ${response.items.size} обращений")
                        tickets = response.items
                    } catch (e: Exception) {
                        println("❌ Ошибка загрузки: ${e.message}")
                    }
                } else {
                    println("⚠️ Токен null — выходим на логин")
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
            val token = tokenManager.getAccessToken()
            var detail by remember { mutableStateOf<TicketDetail?>(null) }
            var isLoading by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                if (token != null) {
                    try {
                        val api = NetworkModule.provideTicketsApi()
                        detail = api.getTicketDetail(token, ticketId)
                    } catch (e: Exception) {
                        println("Ошибка загрузки деталей: ${e.message}")
                    }
                }
                isLoading = false
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (detail != null) {
                DetailForm(
                    ticket = detail!!,
                    onBackClick = { navController.popBackStack() },
                    onSendComment = { text ->
                        println("Отправлен комментарий: $text")
                    }
                )
            }
        }
    }
}