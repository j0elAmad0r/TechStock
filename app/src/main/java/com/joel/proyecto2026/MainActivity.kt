package com.joel.proyecto2026

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import com.joel.proyecto2026.ui.screens.LoginScreen
import com.joel.proyecto2026.ui.viewmodel.LoginViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.joel.proyecto2026.ui.theme.Proyecto2026Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Proyecto2026Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val loginVm: LoginViewModel = viewModel()
                    LoginScreen(vm = loginVm, onLoginSuccess = { /* TODO: navegar a Home */ })
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Proyecto2026Theme {
        Greeting("Android")
    }
}