package com.example.supportform.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.supportform.data.api.AuthApi
import com.example.supportform.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            try {
                println("📤 Отправляем логин: $username")
                val response = authApi.login(username, password)
                println("✅ Логин успешен")
                println("   Access: ${response.accessToken.take(20)}...")
                println("   Refresh: ${response.refreshToken.take(20)}...")
                tokenManager.saveTokens(response.accessToken, response.refreshToken)
                _state.value = _state.value.copy(isLoading = false, isSuccess = true)
            } catch (e: Exception) {
                println("❌ Ошибка логина: ${e.message}")
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Ошибка входа"
                )
            }
        }
    }

    fun resetState() {
        _state.value = LoginState()
    }
}

data class LoginState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)