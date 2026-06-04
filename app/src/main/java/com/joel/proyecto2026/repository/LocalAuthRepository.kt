package com.joel.proyecto2026.repository

import android.content.Context
import androidx.core.content.edit
import com.joel.proyecto2026.models.LoginCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class LocalAuthRepository(context: Context) : AuthRepository {

    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val BASE_URL = "https://horologic-subreniform-angelika.ngrok-free.dev/techstock"

    override fun getRememberedEmail(): String? = preferences.getString(KEY_REMEMBERED_EMAIL, null)

    override fun getLoggedUserName(): String? = preferences.getString(KEY_FULL_NAME, null)

    override fun getLoggedUserRole(): String? = preferences.getString(KEY_ROLE, null)

    override fun isLoggedIn(): Boolean = preferences.getBoolean(KEY_LOGGED_IN, false)

    override suspend fun register(fullName: String, email: String, password: String): Boolean = withContext(Dispatchers.IO) {
        val cleanFullName = fullName.trim()
        val cleanEmail = email.trim()

        if (cleanFullName.isBlank() || cleanEmail.isBlank() || password.length < 6) return@withContext false

        try {
            val url = URL("$BASE_URL/registro.php")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            val jsonParam = JSONObject()
            jsonParam.put("nombre", cleanFullName)
            jsonParam.put("correo", cleanEmail)
            jsonParam.put("password", password)

            val os = OutputStreamWriter(connection.outputStream)
            os.write(jsonParam.toString())
            os.flush()
            os.close()

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val responseStr = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(responseStr)
                return@withContext jsonResponse.getBoolean("success")
            }
            return@withContext false
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    override suspend fun login(credentials: LoginCredentials): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL/login.php")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            val jsonParam = JSONObject()
            jsonParam.put("correo", credentials.email.trim())
            jsonParam.put("password", credentials.password)

            val os = OutputStreamWriter(connection.outputStream)
            os.write(jsonParam.toString())
            os.flush()
            os.close()

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val responseStr = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(responseStr)

                if (jsonResponse.getBoolean("success")) {
                    val data = jsonResponse.getJSONObject("data")

                    preferences.edit {
                        putBoolean(KEY_LOGGED_IN, true)
                        putString(KEY_FULL_NAME, data.getString("nombre"))
                        putString(KEY_EMAIL, data.getString("correo"))
                        putString(KEY_ROLE, data.getString("rol"))

                        if (credentials.rememberMe) {
                            putString(KEY_REMEMBERED_EMAIL, credentials.email.trim())
                        } else {
                            remove(KEY_REMEMBERED_EMAIL)
                        }
                    }
                    return@withContext true
                }
            }
            return@withContext false
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    override fun logout() {
        preferences.edit {
            putBoolean(KEY_LOGGED_IN, false)
            remove(KEY_FULL_NAME)
            remove(KEY_EMAIL)
            remove(KEY_ROLE)
        }
    }

    private companion object {
        const val PREFS_NAME = "techstock_auth"
        const val KEY_FULL_NAME = "full_name"
        const val KEY_EMAIL = "email"
        const val KEY_ROLE = "role"
        const val KEY_LOGGED_IN = "logged_in"
        const val KEY_REMEMBERED_EMAIL = "remembered_email"
    }
}