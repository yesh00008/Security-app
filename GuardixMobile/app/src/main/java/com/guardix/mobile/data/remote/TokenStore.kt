package com.guardix.mobile.data.remote

import android.content.Context
import androidx.core.content.edit

private const val PREFS_NAME = "guardix_api_prefs"
private const val KEY_TOKEN = "token"
private const val KEY_ISSUED_AT = "issued_at"

class TokenStore(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun isValid(maxTokenAgeMinutes: Long = 55): Boolean {
        val issuedAt = prefs.getLong(KEY_ISSUED_AT, 0L)
        if (issuedAt == 0L) return false
        val ageMinutes = (System.currentTimeMillis() - issuedAt) / 60000L
        return ageMinutes < maxTokenAgeMinutes
    }

    fun setToken(token: String) {
        prefs.edit {
            putString(KEY_TOKEN, token)
            putLong(KEY_ISSUED_AT, System.currentTimeMillis())
        }
    }

    fun clear() {
        prefs.edit {
            remove(KEY_TOKEN)
            remove(KEY_ISSUED_AT)
        }
    }
}

