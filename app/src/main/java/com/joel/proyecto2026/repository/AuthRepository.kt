package com.joel.proyecto2026.repository

import com.joel.proyecto2026.models.LoginCredentials

interface AuthRepository {
    fun getRememberedEmail(): String?
    fun getLoggedUserName(): String?
    fun getLoggedUserRole(): String?
    fun isLoggedIn(): Boolean


    suspend fun register(fullName: String, email: String, password: String): Boolean
    suspend fun login(credentials: LoginCredentials): Boolean
    fun logout()
}