package com.joel.proyecto2026.repository

import android.content.Context
import androidx.core.content.edit
import com.joel.proyecto2026.models.LoginCredentials

class LocalAuthRepository(context: Context) : AuthRepository {

    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun getRememberedEmail(): String? {
        return preferences.getString(KEY_REMEMBERED_EMAIL, null)
    }

    override fun getLoggedUserName(): String? {
        return preferences.getString(KEY_FULL_NAME, null)
    }

    override fun isLoggedIn(): Boolean {
        return preferences.getBoolean(KEY_LOGGED_IN, false)
    }

    override fun register(fullName: String, email: String, password: String): Boolean {
        val cleanFullName = fullName.trim()
        val cleanEmail = email.trim()

        if (cleanFullName.isBlank() || cleanEmail.isBlank() || password.length < 6) {
            return false
        }

        preferences.edit {
            putString(KEY_FULL_NAME, cleanFullName)
            putString(KEY_EMAIL, cleanEmail)
            putString(KEY_PASSWORD, password)
            putBoolean(KEY_LOGGED_IN, false)
            putString(KEY_REMEMBERED_EMAIL, cleanEmail)
        }

        return true
    }

    override fun login(credentials: LoginCredentials): Boolean {
        val storedEmail = preferences.getString(KEY_EMAIL, null)
        val storedPassword = preferences.getString(KEY_PASSWORD, null)

        val isValid = storedEmail != null && storedPassword != null &&
            storedEmail.equals(credentials.email.trim(), ignoreCase = true) &&
            storedPassword == credentials.password

        if (!isValid) {
            return false
        }

        preferences.edit {
            putBoolean(KEY_LOGGED_IN, true)
            if (credentials.rememberMe) {
                putString(KEY_REMEMBERED_EMAIL, storedEmail)
            } else {
                remove(KEY_REMEMBERED_EMAIL)
            }
        }

        return true
    }

    override fun logout() {
        preferences.edit {
            putBoolean(KEY_LOGGED_IN, false)
        }
    }

    private companion object {
        const val PREFS_NAME = "techstock_auth"
        const val KEY_FULL_NAME = "full_name"
        const val KEY_EMAIL = "email"
        const val KEY_PASSWORD = "password"
        const val KEY_LOGGED_IN = "logged_in"
        const val KEY_REMEMBERED_EMAIL = "remembered_email"
    }
}