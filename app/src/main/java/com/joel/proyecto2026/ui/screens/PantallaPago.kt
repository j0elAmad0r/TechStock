package com.joel.proyecto2026.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.joel.proyecto2026.ui.viewmodel.CarritoViewModel
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun PantallaPago(
    carritoViewModel: CarritoViewModel,
    onBack: () -> Unit = {}
) {
    var purchaseSuccess by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Checkout", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(12.dp))

        if (purchaseSuccess) {
            // pantalla simple de éxito
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Compra realizada con éxito", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = {
                    // volver al listado y cerrar checkout
                    onBack()
                }) {
                    Text(text = "Volver a la tienda")
                }
            }
            return
        }

        LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
            items(carritoViewModel.articulos) { item ->
                Card(modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)) {
                    Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(text = item.producto.title ?: "-", style = MaterialTheme.typography.titleMedium)
                            Text(text = "Cantidad: ${item.cantidad}")
                        }
                        Text(text = item.producto.price ?: "-", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        val totalText = "Items: ${carritoViewModel.totalArticulos()}"
        Text(text = totalText, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            // Simula el pago: limpia el carrito y muestra pantalla de éxito
            carritoViewModel.vaciar()
            purchaseSuccess = true
        }) {
            Text(text = "Confirmar compra")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PantallaPagoPreview() {
    val vm: CarritoViewModel = viewModel()
    // añadir elementos de ejemplo para la vista previa
    vm.agregarAlCarrito(com.joel.proyecto2026.network.ProductDto(title = "Muestra", price = "$9.99"), 2)
    PantallaPago(carritoViewModel = vm)
}
