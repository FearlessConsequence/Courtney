package com.example.supportform.utils

import com.example.supportform.data.api.AuthApi
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object RefreshManager {

    private val mutex = Mutex()

    suspend fun refreshIfNeeded(
        tokenManager: TokenManager,
        authApi: AuthApi,
        oldAccessToken: String
    ): Boolean {
        return mutex.withLock {
            // Проверяем — может кто-то уже обновил токен, пока мы ждали
            val currentAccess = tokenManager.getAccessToken()
            if (currentAccess != null && currentAccess != oldAccessToken) {
                println("✅ Токен уже обновлён другим запросом")
                return@withLock true
            }

            val refresh = tokenManager.getRefreshToken() ?: return@withLock false

            try {
                println("🔄 Обновляем токены через refresh")
                val response = authApi.refresh(refresh)
                tokenManager.saveTokens(response.accessToken, response.refreshToken)
                println("✅ Токены обновлены")
                true
            } catch (e: Exception) {
                println("❌ Refresh не удался: ${e.message}")
                false
            }
        }
    }
}