package com.joel.proyecto2026.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.joel.proyecto2026.network.ProductDto
import com.joel.proyecto2026.ui.viewmodel.CarritoViewModel
import com.joel.proyecto2026.ui.viewmodel.HomeViewModel
import com.joel.proyecto2026.ui.viewmodel.ItemCarrito

private val FondoInicio = Color(0xFF020817)
private val FondoMedio = Color(0xFF071B38)
private val Azul = Color(0xFF2684FF)
private val AzulSuave = Color(0xFF113B75)
private val Borde = Color.White.copy(alpha = 0.12f)
private val TextoSecundario = Color.White.copy(alpha = 0.68f)

@Composable
fun CarritoVentasScreen(
    carritoViewModel: CarritoViewModel,
    homeViewModel: HomeViewModel? = null,
    onBack: () -> Unit = {},
    onFinalizarVenta: () -> Unit = {},
    onProductClick: ((ProductDto) -> Unit)? = null
) {
    var metodoPago by remember { mutableStateOf("Efectivo") }
    var filtroSeleccionado by remember { mutableStateOf("Todos") }
    var mostrarProductos by remember { mutableStateOf(false) }
    var tituloProductos by remember { mutableStateOf("Productos") }
    val articulos = carritoViewModel.articulos
    val subtotal = carritoViewModel.subtotal()
    val iva = subtotal * 0.12
    val total = subtotal + iva
    fun abrirListado(filtro: String) {
        filtroSeleccionado = filtro
        tituloProductos = if (filtro == "Todos") "Componentes" else filtro
        mostrarProductos = true
        homeViewModel?.search(queryFiltroVentas(filtro))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(FondoInicio, FondoMedio, FondoInicio)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            HeaderVentas(
                cantidad = carritoViewModel.totalArticulos(),
                onBack = onBack
            )

            Spacer(modifier = Modifier.height(12.dp))
            BarraBusqueda(onBuscarClick = { abrirListado("Todos") })
            Spacer(modifier = Modifier.height(10.dp))
            FiltrosVentas(
                seleccionado = filtroSeleccionado,
                onFiltroClick = { abrirListado(it) }
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (articulos.isEmpty()) {
                CarritoVacio(modifier = Modifier.weight(1f))
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 14.dp)
                ) {
                    items(
                        items = articulos,
                        key = { "${it.producto.title}-${it.producto.source}" }
                    ) { item ->
                        ItemVentaCard(
                            item = item,
                            onAumentar = { carritoViewModel.aumentarCantidad(item.producto) },
                            onDisminuir = { carritoViewModel.disminuirCantidad(item.producto) },
                            onEliminar = { carritoViewModel.eliminarItem(item.producto) }
                        )
                    }
                }
            }

            ResumenVentas(
                subtotal = subtotal,
                iva = iva,
                total = total,
                metodoPago = metodoPago,
                onMetodoPago = { metodoPago = it },
                onVaciar = { carritoViewModel.vaciar() },
                onFinalizarVenta = {
                    carritoViewModel.vaciar()
                    onFinalizarVenta()
                }
            )

            Spacer(modifier = Modifier.height(12.dp))
            NavegacionVentas(onBack = onBack)
        }

        if (mostrarProductos) {
            DialogoProductosVentas(
                titulo = tituloProductos,
                homeViewModel = homeViewModel,
                onDismiss = { mostrarProductos = false },
                onProductClick = { producto ->
                    mostrarProductos = false
                    onProductClick?.invoke(producto)
                }
            )
        }
    }
}

@Composable
private fun HeaderVentas(
    cantidad: Int,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White, modifier = Modifier.size(30.dp))
        }
        Text(
            text = "Ventas",
            color = Color.White,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        BadgedBox(
            badge = {
                if (cantidad > 0) {
                    Badge(containerColor = Azul) { Text(cantidad.toString(), color = Color.White) }
                }
            }
        ) {
            Icon(Icons.Filled.ShoppingCart, contentDescription = "Carrito", tint = Color.White, modifier = Modifier.size(34.dp))
        }
    }
}

@Composable
private fun BarraBusqueda(onBuscarClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .height(54.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, Borde, RoundedCornerShape(14.dp))
                .background(Color.Black.copy(alpha = 0.14f))
                .clickable { onBuscarClick() }
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Search, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                "Buscar productos",
                color = Color.White.copy(alpha = 0.48f),
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(
            modifier = Modifier
                .height(54.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, Borde, RoundedCornerShape(14.dp))
                .background(Color.Black.copy(alpha = 0.14f))
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.QrCodeScanner, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Escanear", color = Color.White, fontSize = 15.sp, maxLines = 1)
        }
    }
}

@Composable
private fun FiltrosVentas(
    seleccionado: String,
    onFiltroClick: (String) -> Unit
) {
    val filtros = listOf("Todos", "Componentes", "Accesorios", "Periféricos", "Redes")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Borde, RoundedCornerShape(12.dp))
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        filtros.forEach { filtro ->
            val estaSeleccionado = filtro == seleccionado
            Box(
                modifier = Modifier
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (estaSeleccionado) AzulSuave.copy(alpha = 0.55f) else Color.Transparent)
                    .border(
                        width = if (estaSeleccionado) 1.dp else 0.dp,
                        color = if (estaSeleccionado) Azul else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onFiltroClick(filtro) }
                    .padding(horizontal = 22.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = filtro,
                    color = if (estaSeleccionado) Azul else TextoSecundario,
                    fontSize = 16.sp,
                    fontWeight = if (estaSeleccionado) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun DialogoProductosVentas(
    titulo: String,
    homeViewModel: HomeViewModel?,
    onDismiss: () -> Unit,
    onProductClick: (ProductDto) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.padding(12.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0B111E))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = titulo,
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                when {
                    homeViewModel == null -> {
                        Text(
                            text = "No hay productos disponibles.",
                            color = TextoSecundario,
                            fontSize = 14.sp
                        )
                    }

                    homeViewModel.isLoading.value -> {
                        Text(
                            text = "Cargando productos...",
                            color = TextoSecundario,
                            fontSize = 14.sp
                        )
                    }

                    !homeViewModel.errorMessage.value.isNullOrBlank() -> {
                        Text(
                            text = "No se pudieron cargar los productos.",
                            color = Color(0xFFFFA64D),
                            fontSize = 14.sp
                        )
                    }

                    homeViewModel.featured.isNotEmpty() -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 2.dp)
                        ) {
                            items(homeViewModel.featured.take(10)) { product ->
                                ProductoListadoVentas(
                                    product = product,
                                    onClick = { onProductClick(product) }
                                )
                            }
                        }
                    }

                    else -> {
                        Text(
                            text = "No hay productos para mostrar.",
                            color = TextoSecundario,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductoListadoVentas(
    product: ProductDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121824)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF111827)),
                contentAlignment = Alignment.Center
            ) {
                if (!product.thumbnail.isNullOrBlank()) {
                    AsyncImage(
                        model = product.thumbnail,
                        contentDescription = product.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Filled.Inventory2, contentDescription = null, tint = Azul, modifier = Modifier.size(28.dp))
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.title ?: "Producto",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = precioTexto(product),
                    color = Azul,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Proveedor: ${product.source ?: "-"}",
                    color = TextoSecundario,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                product.reviews?.let { reviews ->
                    Text(
                        text = "En stock: ${reviews / 10}",
                        color = Color(0xFF38D996),
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun ItemVentaCard(
    item: ItemCarrito,
    onAumentar: () -> Unit,
    onDisminuir: () -> Unit,
    onEliminar: () -> Unit
) {
    val producto = item.producto
    val precio = obtenerPrecioProducto(producto)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 112.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF06111F).copy(alpha = 0.92f)),
        border = BorderStroke(1.dp, Borde)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ImagenProducto(producto = producto)
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1.15f)) {
                Text(
                    text = producto.title ?: "Producto sin nombre",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = producto.source ?: "Producto de tienda",
                    color = TextoSecundario,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "SKU: ${crearSku(producto)}",
                    color = TextoSecundario,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$${"%.2f".format(precio)}",
                    color = Azul,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            ControlCantidad(
                cantidad = item.cantidad,
                onAumentar = onAumentar,
                onDisminuir = onDisminuir
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.height(86.dp)
            ) {
                IconButton(onClick = onEliminar, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = Color.White.copy(alpha = 0.66f), modifier = Modifier.size(20.dp))
                }
                Text(
                    text = "$${"%.2f".format(precio * item.cantidad)}",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ImagenProducto(producto: ProductDto) {
    Box(
        modifier = Modifier
            .size(width = 106.dp, height = 76.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.radialGradient(listOf(Color(0xFF113B75).copy(alpha = 0.42f), Color.Transparent))),
        contentAlignment = Alignment.Center
    ) {
        if (!producto.thumbnail.isNullOrBlank()) {
            AsyncImage(
                model = producto.thumbnail,
                contentDescription = producto.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } else {
            Icon(Icons.Filled.Inventory2, contentDescription = null, tint = Azul, modifier = Modifier.size(36.dp))
        }
    }
}

@Composable
private fun ControlCantidad(
    cantidad: Int,
    onAumentar: () -> Unit,
    onDisminuir: () -> Unit
) {
    Row(
        modifier = Modifier
            .height(46.dp)
            .clip(RoundedCornerShape(9.dp))
            .border(1.dp, Borde, RoundedCornerShape(9.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onDisminuir, modifier = Modifier.size(width = 38.dp, height = 42.dp)) {
            Icon(Icons.Filled.Remove, contentDescription = "Disminuir", tint = TextoSecundario, modifier = Modifier.size(18.dp))
        }
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(42.dp)
                .border(1.dp, Borde),
            contentAlignment = Alignment.Center
        ) {
            Text(cantidad.toString(), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        IconButton(onClick = onAumentar, modifier = Modifier.size(width = 38.dp, height = 42.dp)) {
            Icon(Icons.Filled.Add, contentDescription = "Aumentar", tint = Azul, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun ResumenVentas(
    subtotal: Double,
    iva: Double,
    total: Double,
    metodoPago: String,
    onMetodoPago: (String) -> Unit,
    onVaciar: () -> Unit,
    onFinalizarVenta: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF06111F).copy(alpha = 0.94f)),
        border = BorderStroke(1.dp, Borde)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                CuponBox(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1.15f)) {
                    FilaResumen("Subtotal", "$${"%.2f".format(subtotal)}")
                    FilaResumen("IVA (12%)", "$${"%.2f".format(iva)}")
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Borde))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Total", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Text("$${"%.2f".format(total)}", color = Azul, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onFinalizarVenta,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Azul)
            ) {
                Icon(Icons.Filled.Bolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Finalizar venta", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onVaciar,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = TextoSecundario),
                border = BorderStroke(1.dp, Azul)
            ) {
                Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Vaciar carrito", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text("Método de pago", color = TextoSecundario, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                MetodoPagoButton("Efectivo", metodoPago == "Efectivo", Modifier.weight(1f), onMetodoPago)
                MetodoPagoButton("Tarjeta", metodoPago == "Tarjeta", Modifier.weight(1f), onMetodoPago)
                MetodoPagoButton("Transferencia", metodoPago == "Transferencia", Modifier.weight(1f), onMetodoPago)
            }
        }
    }
}

@Composable
private fun CuponBox(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Borde, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.LocalOffer, contentDescription = null, tint = TextoSecundario, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text("Agregar", color = Azul, fontSize = 15.sp, modifier = Modifier.weight(1f), maxLines = 1)
        Text(">", color = TextoSecundario, fontSize = 22.sp)
    }
}

@Composable
private fun MetodoPagoButton(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit
) {
    val icon = when (label) {
        "Efectivo" -> Icons.Filled.Payments
        "Tarjeta" -> Icons.Filled.CreditCard
        else -> Icons.Filled.SwapHoriz
    }
    Row(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, if (selected) Azul else Borde, RoundedCornerShape(12.dp))
            .background(if (selected) AzulSuave.copy(alpha = 0.55f) else Color.Transparent)
            .clickable { onClick(label) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = null, tint = if (selected) Azul else TextoSecundario, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = if (label == "Transferencia") "Transf." else label,
            color = if (selected) Azul else TextoSecundario,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FilaResumen(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = TextoSecundario, fontSize = 15.sp, modifier = Modifier.weight(1f), maxLines = 1)
        Text(value, color = TextoSecundario, fontSize = 15.sp, maxLines = 1)
    }
}

@Composable
private fun CarritoVacio(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF06111F).copy(alpha = 0.92f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Borde)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Filled.ShoppingCart, contentDescription = null, tint = Azul, modifier = Modifier.size(42.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text("Tu carrito está vacío", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(
                "Agrega productos desde el detalle para iniciar una venta.",
                color = TextoSecundario,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun NavegacionVentas(onBack: () -> Unit) {
    val items = listOf(
        Triple("Inicio", Icons.Filled.Home, false),
        Triple("Categorías", Icons.Filled.Apps, false),
        Triple("Inventario", Icons.Filled.Inventory2, false),
        Triple("Ventas", Icons.Filled.ShoppingCart, true),
        Triple("Perfil", Icons.Filled.Person, false)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Borde, RoundedCornerShape(16.dp))
            .background(Color(0xFF06111F).copy(alpha = 0.94f)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { (label, icon, selected) ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp)
                    .clickable { if (label == "Inicio") onBack() },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(icon, contentDescription = label, tint = if (selected) Azul else TextoSecundario, modifier = Modifier.size(27.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    label,
                    color = if (selected) Azul else TextoSecundario,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun obtenerPrecioProducto(producto: ProductDto): Double {
    return producto.extractedPrice
        ?: producto.price
            ?.replace("$", "")
            ?.replace(",", "")
            ?.trim()
            ?.toDoubleOrNull()
        ?: 0.0
}

private fun precioTexto(producto: ProductDto): String {
    return when {
        !producto.price.isNullOrBlank() -> producto.price
        producto.extractedPrice != null -> "$${"%.2f".format(producto.extractedPrice)}"
        else -> "$0.00"
    }
}

private fun queryFiltroVentas(filtro: String): String {
    return when (filtro) {
        "Todos" -> "componentes de computadora"
        "Componentes" -> "procesadores memorias tarjetas madre"
        "Accesorios" -> "accesorios computadora"
        "Periféricos" -> "mouse teclado audifonos monitor"
        "Redes" -> "router switch cable ethernet"
        else -> filtro
    }
}

private fun crearSku(producto: ProductDto): String {
    val base = producto.title.orEmpty()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(3)
        .joinToString("-") { it.take(4).uppercase() }
    return base.ifBlank { "PROD-001" }
}
