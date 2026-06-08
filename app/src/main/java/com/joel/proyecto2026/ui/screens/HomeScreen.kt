package com.joel.proyecto2026.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.annotation.DrawableRes
import coil.compose.AsyncImage
import com.joel.proyecto2026.R
import com.joel.proyecto2026.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import androidx.compose.runtime.LaunchedEffect
import com.joel.proyecto2026.network.ProductDto
import com.joel.proyecto2026.ui.theme.PrimaryLight
import com.joel.proyecto2026.ui.theme.PrimaryLight2
import androidx.compose.ui.window.Dialog

private data class CategoryItem(
    val title: String,
    val badge: String,
    val accent: Color
)

private data class FeaturedItem(
    val name: String,
    val price: String,
    val stockLabel: String,
    val accent: Color,
    val badge: String,
    val imageUrl: String? = null,
    val product: ProductDto? = null
)

@DrawableRes
private fun iconoCategoriaRes(titulo: String): Int {
    val t = titulo.lowercase()
    return when {
        t.contains("procesador") || t.contains("cpu") -> R.drawable.cpu
        t.contains("memoria") || t.contains("ram") -> R.drawable.ram
        t.contains("tarjeta") || t.contains("gpu") -> R.drawable.gpu
        t.contains("placa") || t.contains("madre") -> R.drawable.cate
        t.contains("almacen") || t.contains("ssd") || t.contains("hdd") -> R.drawable.ssd
        t.contains("perif") || t.contains("mouse") || t.contains("raton") || t.contains("teclado") -> R.drawable.raton
        t.contains("gabinete") || t.contains("case") -> R.drawable.gabiente
        else -> R.drawable.cate
    }
}

@Composable
fun PantallaInicio(
    userName: String?,
    userRole: String? = null,
    homeViewModel: HomeViewModel? = null, // Ya casi no lo usaremos aquí, pero lo mantenemos para no romper la app
    onOpenInventory: (() -> Unit)? = null,
    onOpenProviders: (() -> Unit)? = null,
    onOpenSettings: (() -> Unit)? = null,
    cantidadCarrito: Int = 0,
    onAbrirCarrito: (() -> Unit)? = null,
    onProductClick: ((ProductDto) -> Unit)? = null,
    onProfileClick: (() -> Unit)? = null
) {
    val colors = listOf(Color(0xFF4DA3FF), Color(0xFF38D996), Color(0xFFFFA64D), Color(0xFF5DB8FF), Color(0xFF7EC8FF))

    var productosLocales by remember { mutableStateOf<List<ProductDto>>(emptyList()) }
    var cargandoCatalogo by remember { mutableStateOf(true) }
    var search by remember { mutableStateOf("") }
    var showProductsDialog by remember { mutableStateOf(false) }
    var selectedCategoryTitle by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    // 1. CARGAMOS LA BASE DE DATOS
    LaunchedEffect(Unit) {
        productosLocales = obtenerCatalogoDeBD()
        cargandoCatalogo = false
    }

    val safeRole = userRole?.trim()?.lowercase() ?: ""
    val isAdmin = safeRole == "1" || safeRole.contains("administrador")

    // 2. FILTRADO INSTANTÁNEO LOCAL (Ya no usamos la API para buscar)
    val productosFiltrados = if (search.isBlank()) {
        productosLocales
    } else {
        productosLocales.filter {
            it.title?.contains(search, ignoreCase = true) == true ||
                    it.source?.contains(search, ignoreCase = true) == true
        }
    }

    val categories = listOf(
        CategoryItem("Procesadores", "CPU", Color(0xFF4DA3FF)),
        CategoryItem("Memorias", "RAM", Color(0xFF38D996)),
        CategoryItem("Tarjetas Gráficas", "GPU", Color(0xFFFFA64D)),
        CategoryItem("Placa Madre", "MB", Color(0xFF5DB8FF)),
        CategoryItem("Almacenamiento", "SSD", Color(0xFF7EC8FF)),
        CategoryItem("Periféricos", "PER", Color(0xFF4DA3FF)),
        CategoryItem("Gabinete", "Gabo", Color(0xFF38D996))
    )

    // 3. CONVERTIMOS LOS PRODUCTOS DE MYSQL A TARJETAS
    val featuredItems = if (productosFiltrados.isNotEmpty()) {
        productosFiltrados.mapIndexed { idx, p ->
            FeaturedItem(
                name = p.title ?: "-",
                price = when {
                    !p.price.isNullOrBlank() -> p.price
                    p.extractedPrice != null -> "$${"%.2f".format(p.extractedPrice)}"
                    else -> "-"
                },
                stockLabel = p.reviews?.let { "En stock: ${it / 10}" } ?: "Sin stock",
                accent = colors[idx % colors.size],
                badge = (p.source ?: p.title ?: "#").take(3).uppercase(),
                imageUrl = p.thumbnail,
                product = p
            )
        }
    } else {
        emptyList()
    }

    val bottomItems = mutableListOf(
        Triple("Inicio", Icons.Filled.Home, true)
    )

    if (isAdmin) {
        bottomItems.add(Triple("Proveedor", Icons.Filled.Category, false))
        bottomItems.add(Triple("Inventario", Icons.Filled.Inventory, false))
    }

    bottomItems.add(Triple("Carrito", Icons.Filled.ShoppingCart, false))
    bottomItems.add(Triple("Perfil", Icons.Filled.Person, false))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF030A1D),
                        Color(0xFF0A1A43),
                        Color(0xFF040C23)
                    )
                )
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        // ... (Círculos de fondo decorativos) ...
        Box(modifier = Modifier.align(Alignment.TopEnd).size(180.dp).background(Brush.radialGradient(colors = listOf(PrimaryLight.copy(alpha = 0.22f), Color.Transparent)), shape = CircleShape))
        Box(modifier = Modifier.align(Alignment.TopStart).size(220.dp).background(Brush.radialGradient(colors = listOf(PrimaryLight2.copy(alpha = 0.18f), Color.Transparent)), shape = CircleShape))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .padding(top = 4.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = Color.Transparent,
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Brush.linearGradient(listOf(PrimaryLight, PrimaryLight2))),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo),
                                contentDescription = "Logo TechStock",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(46.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "TechStock",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Tienda e inventario de componentes",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.72f)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "☰",
                            color = Color.White,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onOpenSettings?.invoke() }
                                .background(Color.White.copy(alpha = 0.08f))
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        androidx.compose.material3.IconButton(onClick = { onAbrirCarrito?.invoke() }) {
                            BadgedBox(badge = {
                                if (cantidadCarrito > 0) Badge { Text(cantidadCarrito.toString()) }
                            }) {
                                Icon(Icons.Filled.ShoppingCart, contentDescription = "Carrito", tint = Color.White)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0B111E).copy(alpha = 0.94f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = search,
                            onValueChange = { search = it },
                            singleLine = true,
                            placeholder = { Text("Buscar en el inventario local") },
                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (cargandoCatalogo) {
                        Text(
                            text = "Cargando catálogo local...",
                            color = Color.White.copy(alpha = 0.72f),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        categories.forEach { category ->
                            Card(
                                modifier = Modifier
                                    .size(width = 118.dp, height = 122.dp)
                                    .clickable {
                                        selectedCategoryTitle = category.title
                                        showProductsDialog = true
                                    },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF121824)),
                                shape = RoundedCornerShape(20.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(category.accent.copy(alpha = 0.16f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(id = iconoCategoriaRes(category.title)),
                                            contentDescription = category.title,
                                            modifier = Modifier.size(24.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                    Text(
                                        text = category.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("★", color = Color(0xFF3B82F6))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Productos Disponibles",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (featuredItems.isEmpty() && !cargandoCatalogo) {
                        Text(
                            text = "No se encontraron productos en el inventario.",
                            color = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            featuredItems.forEach { item ->
                                Card(
                                    modifier = Modifier
                                        .size(width = 148.dp, height = 210.dp)
                                        .clickable { item.product?.let { onProductClick?.invoke(it) } },
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF121824)),
                                    shape = RoundedCornerShape(18.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(78.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(Color(0xFF111827)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (!item.imageUrl.isNullOrBlank() && item.imageUrl != "producto_default") {
                                                AsyncImage(
                                                    model = item.imageUrl,
                                                    contentDescription = item.name,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .size(52.dp)
                                                        .clip(RoundedCornerShape(16.dp))
                                                        .background(item.accent.copy(alpha = 0.18f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = item.badge,
                                                        color = item.accent,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }

                                        Column {
                                            Text(
                                                text = item.name,
                                                color = Color.White,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium,
                                                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
                                                maxLines = 3,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = item.price,
                                                color = item.accent,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = item.stockLabel,
                                                color = Color(0xFF38D996),
                                                style = MaterialTheme.typography.bodyMedium,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF101726)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MetricTile(
                                title = "En stock",
                                value = productosLocales.sumOf { (it.reviews ?: 0) / 10 }.toString(),
                                subtitle = "unidades",
                                accent = Color(0xFF4DA3FF)
                            )
                            Divider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.height(64.dp).width(1.dp))
                            MetricTile(
                                title = "Catálogo",
                                value = productosLocales.size.toString(),
                                subtitle = "productos",
                                accent = Color(0xFFFFA64D)
                            )
                            Divider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.height(64.dp).width(1.dp))
                            MetricTile(
                                title = "Nuevos",
                                value = "Hoy",
                                subtitle = "actualizado",
                                accent = Color(0xFF38D996)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (isAdmin) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF2B77FF), Color(0xFF56C4FF))
                                        ),
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                    .clickable { onOpenInventory?.invoke() }
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("▣", color = Color.White)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Ver Inventario",
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0E1420)),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            bottomItems.forEach { (label, icon, selected) ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(
                                                if (selected) PrimaryLight.copy(alpha = 0.22f) else Color.Transparent
                                            )
                                            .clickable {
                                                when (label) {
                                                    "Proveedor" -> onOpenProviders?.invoke()
                                                    "Inventario" -> onOpenInventory?.invoke()
                                                    "Perfil" -> onProfileClick?.invoke()
                                                    "Carrito" -> onAbrirCarrito?.invoke()
                                                    else -> { /* no-op */ }
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = label,
                                            tint = if (selected) Color(0xFF4DA3FF) else Color.White.copy(alpha = 0.56f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = label,
                                        color = if (selected) Color.White else Color.White.copy(alpha = 0.58f),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (userName.isNullOrBlank()) "Sesion local activa" else "Bienvenido, $userName",
                        color = Color.White.copy(alpha = 0.72f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // 4. DIÁLOGO CONECTADO A MYSQL
        if (showProductsDialog) {
            Dialog(onDismissRequest = { showProductsDialog = false }) {
                Card(
                    modifier = Modifier.padding(12.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0B111E))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = selectedCategoryTitle.ifBlank { "Productos" },
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { showProductsDialog = false }) {
                                Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // Filtramos los locales por categoría
                        val productosEnCategoria = productosLocales.filter {
                            it.source?.contains(selectedCategoryTitle, ignoreCase = true) == true
                        }.ifEmpty { productosLocales.take(8) } // Si no hay en esa categoría, mostramos algunos al azar

                        when {
                            cargandoCatalogo -> {
                                Text(
                                    text = "Cargando productos...",
                                    color = Color.White.copy(alpha = 0.72f)
                                )
                            }
                            productosLocales.isNotEmpty() -> {
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    items(productosEnCategoria) { product ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    showProductsDialog = false
                                                    onProductClick?.invoke(product)
                                                },
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF121824)),
                                            shape = RoundedCornerShape(18.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(64.dp)
                                                        .clip(RoundedCornerShape(14.dp))
                                                        .background(Color(0xFF111827)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    if (!product.thumbnail.isNullOrBlank() && product.thumbnail != "producto_default") {
                                                        AsyncImage(
                                                            model = product.thumbnail,
                                                            contentDescription = product.title,
                                                            modifier = Modifier.fillMaxSize(),
                                                            contentScale = ContentScale.Crop
                                                        )
                                                    } else {
                                                        Text(
                                                            text = (product.source ?: product.title ?: "#").take(3).uppercase(),
                                                            color = Color.White
                                                        )
                                                    }
                                                }

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = product.title ?: "-",
                                                        color = Color.White,
                                                        fontWeight = FontWeight.SemiBold,
                                                        maxLines = 2,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = when {
                                                            !product.price.isNullOrBlank() -> product.price
                                                            product.extractedPrice != null -> "$${"%.2f".format(product.extractedPrice)}"
                                                            else -> "-"
                                                        },
                                                        color = Color(0xFF4DA3FF),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = "Categoría: ${product.source ?: ""}",
                                                        color = Color.White.copy(alpha = 0.64f),
                                                        style = MaterialTheme.typography.bodySmall,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = product.reviews?.let { "En stock: ${it / 10}" } ?: "",
                                                        color = Color(0xFF38D996),
                                                        style = MaterialTheme.typography.bodySmall
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            else -> {
                                Text(
                                    text = "No hay productos para mostrar aún.",
                                    color = Color.White.copy(alpha = 0.72f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Toca fuera para cerrar",
                            color = Color.White.copy(alpha = 0.48f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricTile(
    title: String,
    value: String,
    subtitle: String,
    accent: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 8.dp)) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+",
                color = accent,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(title, color = Color.White.copy(alpha = 0.72f), style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(subtitle, color = Color.White.copy(alpha = 0.72f), style = MaterialTheme.typography.bodySmall)
    }
}

@Preview(showBackground = true)
@Composable
private fun VistaPreviaPantallaInicio() {
    PantallaInicio(
        userName = "Joel",
        homeViewModel = null,
        onOpenInventory = {},
        onOpenProviders = {},
        onOpenSettings = {},
        onProductClick = {},
        onProfileClick = {}
    )
}

suspend fun obtenerCatalogoDeBD(): List<ProductDto> = withContext(Dispatchers.IO) {
    val BASE_URL = "https://horologic-subreniform-angelika.ngrok-free.dev/techstock"
    val lista = mutableListOf<ProductDto>()
    try {
        val url = URL("$BASE_URL/productos.php")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            val responseStr = connection.inputStream.bufferedReader().use { it.readText() }
            val jsonResponse = JSONObject(responseStr)

            if (jsonResponse.getBoolean("success")) {
                val dataArray = jsonResponse.getJSONArray("data")
                for (i in 0 until dataArray.length()) {
                    val item = dataArray.getJSONObject(i)
                    val stockActual = item.optInt("stock_actual", 0)

                    lista.add(
                        ProductDto(
                            title = item.optString("nombre_producto"),
                            extractedPrice = item.optDouble("precio"),
                            thumbnail = item.optString("imagen_url"),
                            source = item.optString("categoria"),
                            // TRUCO: Como tu UI divide reviews / 10 para el stock,
                            // lo multiplicamos aquí para que la pantalla muestre el stock EXACTO
                            reviews = stockActual * 10
                        )
                    )
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return@withContext lista
}