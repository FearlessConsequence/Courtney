package com.example.supportform.utils

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("tokens", Context.MODE_PRIVATE)

    fun saveTokens(access: String, refresh: String) {
        println("💾 Сохраняем токены")
        println("   Access: ${access.take(20)}...")
        println("   Refresh: ${refresh.take(20)}...")
        prefs.edit()
            .putString("access_token", access)
            .putString("refresh_token", refresh)
            .apply()
    }

    fun getAccessToken(): String? {
        val token = prefs.getString("access_token", null)
        println("🔑 Читаем access: ${token?.take(20)}...")
        return token
    }

    fun getRefreshToken(): String? {
        val token = prefs.getString("refresh_token", null)
        println("🔑 Читаем refresh: ${token?.take(20)}...")
        return token
    }

    fun clearTokens() {
        prefs.edit().clear().apply()
    }


}