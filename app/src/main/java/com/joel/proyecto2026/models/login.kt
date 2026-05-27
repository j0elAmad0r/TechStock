package com.joel.proyecto2026.models

data class LoginCredentials(
    val email: String = "",
    val password: String = "",
    val rememberMe: Boolean = false
)