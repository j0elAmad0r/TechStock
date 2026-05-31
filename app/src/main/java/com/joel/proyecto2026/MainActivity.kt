package com.joel.proyecto2026
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import com.joel.proyecto2026.ui.screens.LoginScreen
import com.joel.proyecto2026.ui.screens.HomeScreen
import com.joel.proyecto2026.ui.screens.InventoryScreen
import com.joel.proyecto2026.ui.screens.ProvidersScreen
import com.joel.proyecto2026.ui.screens.SettingsScreen
import com.joel.proyecto2026.ui.screens.ProductDetailDialog
import com.joel.proyecto2026.repository.BestBuyRepositoryImpl
import com.joel.proyecto2026.ui.viewmodel.HomeViewModel
import com.joel.proyecto2026.ui.viewmodel.HomeViewModelFactory
import com.joel.proyecto2026.ui.screens.RegisterScreen
import com.joel.proyecto2026.repository.LocalAuthRepository
import com.joel.proyecto2026.ui.viewmodel.LoginViewModel
import com.joel.proyecto2026.ui.viewmodel.LoginViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import com.joel.proyecto2026.ui.theme.Proyecto2026Theme
import com.joel.proyecto2026.network.ProductDto
import com.joel.proyecto2026.ui.screens.ProfileScreen

private enum class AppScreen {
    Loading,
    Login,
    Register,
    Home,
    Inventory,
    Providers,
    Profile,
    Settings
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val authRepository = LocalAuthRepository(applicationContext)
        setContent {
            Proyecto2026Theme {
                var currentScreen by remember { mutableStateOf(AppScreen.Loading) }
                val loginVm: LoginViewModel = viewModel(
                    factory = LoginViewModelFactory(authRepository)
                )

                LaunchedEffect(Unit) {
                    currentScreen = if (authRepository.isLoggedIn()) {
                        AppScreen.Home
                    } else {
                        AppScreen.Login
                    }
                }

                val bestBuyRepo = remember { BestBuyRepositoryImpl(BuildConfig.SERPAPI_API_KEY) }
                val homeVm: HomeViewModel = viewModel(factory = HomeViewModelFactory(bestBuyRepo))
                var productoSeleccionado by remember { mutableStateOf<ProductDto?>(null) }

                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    when (currentScreen) {
                        AppScreen.Loading -> Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Cargando...")
                        }

                        AppScreen.Login -> LoginScreen(
                            vm = loginVm,
                            onLoginSuccess = { currentScreen = AppScreen.Home },
                            onRegisterClick = { currentScreen = AppScreen.Register }
                        )

                        AppScreen.Register -> RegisterScreen(
                            authRepository = authRepository,
                            onBackToLogin = { currentScreen = AppScreen.Login },
                            onRegisterSuccess = { registeredEmail ->
                                // prefill email and go to Home
                                loginVm.onEmailChange(registeredEmail)
                                currentScreen = AppScreen.Home
                            }
                        )

                        AppScreen.Home -> {
                            HomeScreen(
                                userName = authRepository.getLoggedUserName(),
                                homeViewModel = homeVm,
                                onOpenInventory = { currentScreen = AppScreen.Inventory },
                                onOpenProviders = { currentScreen = AppScreen.Providers },
                                onOpenSettings = { currentScreen = AppScreen.Settings },
                                onProductClick = { productoSeleccionado = it },
                                onProfileClick = { currentScreen = AppScreen.Profile }
                            )
                        }

                        AppScreen.Inventory -> {
                            InventoryScreen(
                                onBackToHome = { currentScreen = AppScreen.Home },
                                homeViewModel = homeVm,
                                onProductoClick = { productoSeleccionado = it }
                            )
                        }

                        AppScreen.Providers -> {
                            ProvidersScreen(
                                homeViewModel = homeVm,
                                onBack = { currentScreen = AppScreen.Home }
                            )
                        }

                        AppScreen.Profile -> {
                            ProfileScreen(onBack = { currentScreen = AppScreen.Home })
                        }

                        AppScreen.Settings -> {
                            SettingsScreen(onBack = { currentScreen = AppScreen.Home })
                        }
                    }

                    productoSeleccionado?.let { product ->
                        ProductDetailDialog(
                            product = product,
                            onDismiss = { productoSeleccionado = null }
                        )
                    }
                }
            }
        }
    }
}