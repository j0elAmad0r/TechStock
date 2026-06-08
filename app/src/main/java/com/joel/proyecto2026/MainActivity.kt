package com.joel.proyecto2026
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import com.joel.proyecto2026.ui.screens.PantallaInicioSesion
import com.joel.proyecto2026.ui.screens.PantallaInicio
import com.joel.proyecto2026.ui.screens.PantallaInventario
import com.joel.proyecto2026.ui.screens.PantallaProveedores
import com.joel.proyecto2026.ui.screens.PantallaConfiguracion
import com.joel.proyecto2026.ui.screens.DialogoDetalleProducto
import com.joel.proyecto2026.ui.screens.CarritoVentasScreen
import com.joel.proyecto2026.repository.BestBuyRepositoryImpl
import com.joel.proyecto2026.ui.viewmodel.HomeViewModel
import com.joel.proyecto2026.ui.viewmodel.HomeViewModelFactory
import com.joel.proyecto2026.ui.screens.PantallaRegistro
import com.joel.proyecto2026.repository.LocalAuthRepository
import com.joel.proyecto2026.ui.viewmodel.LoginViewModel
import com.joel.proyecto2026.ui.viewmodel.LoginViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import com.joel.proyecto2026.ui.theme.Proyecto2026Theme
import com.joel.proyecto2026.network.ProductDto
import com.joel.proyecto2026.ui.screens.PantallaPerfil
import com.joel.proyecto2026.ui.screens.AdminDashboardScreen // <-- Import agregado

private enum class PantallaApp {
    Loading,
    Login,
    Register,
    Home,
    Inventory,
    Providers,
    Profile,
    Settings,
    Checkout
}

//Kaktuz estuvo aqui

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val authRepository = LocalAuthRepository(applicationContext)
        setContent {
            Proyecto2026Theme {
                var currentScreen by remember { mutableStateOf(PantallaApp.Loading) }
                val loginVm: LoginViewModel = viewModel(
                    factory = LoginViewModelFactory(authRepository)
                )

                LaunchedEffect(Unit) {
                    currentScreen = if (authRepository.isLoggedIn()) {
                        PantallaApp.Home
                    } else {
                        PantallaApp.Login
                    }
                }

                val bestBuyRepo = remember { BestBuyRepositoryImpl(BuildConfig.SERPAPI_API_KEY) }
                val homeVm: HomeViewModel = viewModel(factory = HomeViewModelFactory(bestBuyRepo))
                var productoSeleccionado by remember { mutableStateOf<ProductDto?>(null) }
                val carritoVm: com.joel.proyecto2026.ui.viewmodel.CarritoViewModel = viewModel()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Envolvemos todo en una Box que respete el padding del Scaffold
                    Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {

                        when (currentScreen) {
                            PantallaApp.Loading -> Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Cargando...")
                            }

                            PantallaApp.Login -> PantallaInicioSesion(
                                vm = loginVm,
                                onLoginSuccess = { currentScreen = PantallaApp.Home },
                                onRegisterClick = { currentScreen = PantallaApp.Register }
                            )

                            PantallaApp.Register -> PantallaRegistro(
                                authRepository = authRepository,
                                onBackToLogin = { currentScreen = PantallaApp.Login },
                                onRegisterSuccess = { registeredEmail ->
                                    loginVm.onEmailChange(registeredEmail)
                                    currentScreen = PantallaApp.Home
                                }
                            )

                            PantallaApp.Home -> {
                                // Lógica de enrutamiento basada en el rol
                                val role = authRepository.getLoggedUserRole()?.trim()?.lowercase() ?: ""
                                val isAdmin = role == "1" || role.contains("administrador")

                                if (isAdmin) {
                                    AdminDashboardScreen(
                                        userName = authRepository.getLoggedUserName(),
                                        onOpenInventory = { currentScreen = PantallaApp.Inventory },
                                        onOpenProviders = { currentScreen = PantallaApp.Providers },
                                        onOpenStatistics = { /* TODO: Pantalla de estadísticas en Fase 4 */ },
                                        onProfileClick = { currentScreen = PantallaApp.Profile }
                                    )
                                } else {
                                    PantallaInicio(
                                        userName = authRepository.getLoggedUserName(),
                                        userRole = authRepository.getLoggedUserRole(),
                                        homeViewModel = homeVm,
                                        onOpenInventory = { currentScreen = PantallaApp.Inventory },
                                        onOpenProviders = { currentScreen = PantallaApp.Providers },
                                        onOpenSettings = { currentScreen = PantallaApp.Settings },
                                        cantidadCarrito = carritoVm.totalArticulos(),
                                        onAbrirCarrito = { currentScreen = PantallaApp.Checkout },
                                        onProductClick = { productoSeleccionado = it },
                                        onProfileClick = { currentScreen = PantallaApp.Profile }
                                    )
                                }
                            }

                            PantallaApp.Inventory -> {
                                PantallaInventario(
                                    onBackToHome = { currentScreen = PantallaApp.Home },
                                    homeViewModel = homeVm,
                                    onProductoClick = { productoSeleccionado = it }
                                )
                            }

                            PantallaApp.Providers -> {
                                PantallaProveedores(
                                    homeViewModel = homeVm,
                                    onBack = { currentScreen = PantallaApp.Home }
                                )
                            }

                            PantallaApp.Profile -> {
                                PantallaPerfil(
                                    onBack = { currentScreen = PantallaApp.Home },
                                    onLogout = {
                                        loginVm.logout()
                                        homeVm.categories.clear()
                                        homeVm.featured.clear()
                                        currentScreen = PantallaApp.Login
                                    }
                                )
                            }

                            PantallaApp.Settings -> {
                                PantallaConfiguracion(
                                    onBack = { currentScreen = PantallaApp.Home },
                                    onLogout = {
                                        loginVm.logout()
                                        homeVm.categories.clear()
                                        homeVm.featured.clear()
                                        currentScreen = PantallaApp.Login
                                    }
                                )
                            }

                            PantallaApp.Checkout -> {
                                CarritoVentasScreen(
                                    carritoViewModel = carritoVm,
                                    homeViewModel = homeVm,
                                    onBack = { currentScreen = PantallaApp.Home },
                                    onFinalizarVenta = { currentScreen = PantallaApp.Home },
                                    onProductClick = { productoSeleccionado = it }
                                )
                            }
                        }

                        productoSeleccionado?.let { producto ->
                            DialogoDetalleProducto(
                                producto = producto,
                                onDismiss = { productoSeleccionado = null },
                                onAgregarAlCarrito = { p, q -> carritoVm.agregarAlCarrito(p, q) },
                                onComprarAhora = { p, q ->
                                    carritoVm.agregarAlCarrito(p, q)
                                    productoSeleccionado = null
                                    currentScreen = PantallaApp.Checkout
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}