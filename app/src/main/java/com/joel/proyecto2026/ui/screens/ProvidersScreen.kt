package com.joel.proyecto2026.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.joel.proyecto2026.network.ProductDto
import com.joel.proyecto2026.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

// --- MODELOS LOCALES ---
data class CategoriaLocal(val id: Int, val nombre: String)
data class ProductoLocalSimplificado(val id: Int, val nombre: String, val marca: String)

// --- LÓGICA DE UI DE PROVEEDORES ---
private data class ProviderUi(
    val name: String,
    val products: List<ProductDto>,
    val accent: Color,
    val imageUrl: String? = products.firstNotNullOfOrNull {
        it.sourceIcon?.takeIf(String::isNotBlank) ?: it.thumbnail?.takeIf(String::isNotBlank)
    }
) {
    val initials: String get() = name.take(2).uppercase()
    val productCount: Int get() = products.size
    val lowStockProducts: List<ProductDto> get() = products.take(3)
}

private fun buildProvidersFromApi(products: List<ProductDto>): List<ProviderUi> {
    val cleanProducts = products.filter { !it.source.isNullOrBlank() && !it.title.isNullOrBlank() }
    val grouped = cleanProducts.groupBy { it.source!!.trim() }
    val accents = listOf(Color(0xFF4DA3FF), Color(0xFF38D996), Color(0xFF7C4DFF), Color(0xFFFFA64D), Color(0xFFFF4D6D))

    return grouped.entries.mapIndexed { index, entry ->
        ProviderUi(entry.key, entry.value, accents[index % accents.size])
    }.sortedByDescending { it.productCount }
}

@Composable
fun PantallaProveedores(
    homeViewModel: HomeViewModel? = null,
    onBack: (() -> Unit)? = null
) {
    var search by remember { mutableStateOf("") }
    var selectedProvider by remember { mutableStateOf<ProviderUi?>(null) }
    var productoACatalogar by remember { mutableStateOf<ProductDto?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // ESTADOS PARA DATOS DE MYSQL
    var categoriasBD by remember { mutableStateOf<List<CategoriaLocal>>(emptyList()) }
    var productosBD by remember { mutableStateOf<List<ProductoLocalSimplificado>>(emptyList()) }

    LaunchedEffect(Unit) {
        if (homeViewModel?.featured?.isEmpty() == true) {
            homeViewModel.search("pc components")
        }
        // Descargamos catálogos reales
        categoriasBD = obtenerCategoriasDeBD()
        productosBD = obtenerProductosSimplesDeBD()
    }

    val apiProducts = homeViewModel?.featured?.toList().orEmpty()
    val providers = buildProvidersFromApi(apiProducts)
    val filteredProviders = if (search.isBlank()) providers else providers.filter { it.name.contains(search, true) }

    Box(
        modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF030A1D), Color(0xFF0A1A43))))
    ) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(16.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onBack?.invoke() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }
                Text("Red de Proveedores", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                singleLine = true,
                placeholder = { Text("Buscar proveedor...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de proveedores
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filteredProviders) { provider ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { selectedProvider = provider },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF07111D)),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, provider.accent.copy(alpha = 0.3f))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(provider.accent.copy(alpha=0.2f)), contentAlignment = Alignment.Center) {
                                Text(provider.initials, color = provider.accent, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(provider.name, color = Color.White, fontWeight = FontWeight.Bold)
                                Text("${provider.productCount} productos disponibles", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // DIÁLOGO DE PRODUCTOS DEL PROVEEDOR
        selectedProvider?.let { provider ->
            Dialog(onDismissRequest = { selectedProvider = null }) {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF0B111E)), shape = RoundedCornerShape(20.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Catálogo de ${provider.name}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))

                        provider.products.take(6).forEach { product ->
                            Row(modifier = Modifier.fillMaxWidth().clickable {
                                productoACatalogar = product
                            }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Inventory2, contentDescription = null, tint = provider.accent)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(product.title ?: "Producto", color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }

                        Button(onClick = { selectedProvider = null }, modifier = Modifier.fillMaxWidth()) {
                            Text("Cerrar")
                        }
                    }
                }
            }
        }

        // SÚPER-FORMULARIO DE RESTOCK
        val prod = productoACatalogar
        if (prod != null) {
            DialogoCatalogarAvanzado(
                productoApi = prod,
                categoriasBD = categoriasBD,
                productosBD = productosBD,
                onDismiss = { productoACatalogar = null },
                onGuardarExistente = { idProd, stock ->
                    coroutineScope.launch {
                        val exito = sumarStockABaseDeDatos(idProd, stock)
                        Toast.makeText(context, if (exito) "✅ Stock actualizado" else "❌ Error", Toast.LENGTH_SHORT).show()
                        productoACatalogar = null
                        selectedProvider = null
                    }
                },
                onGuardarNuevo = { nombre, idCat, nuevaCat, pCompra, pVenta, stock ->
                    coroutineScope.launch {
                        val exito = enviarProductoNuevoABD(nombre, idCat, nuevaCat, pCompra, pVenta, stock, prod.thumbnail ?: "")
                        Toast.makeText(context, if (exito) "✅ Producto creado" else "❌ Error", Toast.LENGTH_SHORT).show()
                        productoACatalogar = null
                        selectedProvider = null
                        // Recargar BD local para futuras operaciones
                        productosBD = obtenerProductosSimplesDeBD()
                        categoriasBD = obtenerCategoriasDeBD()
                    }
                }
            )
        }
    }
}

@Composable
fun DialogoCatalogarAvanzado(
    productoApi: ProductDto,
    categoriasBD: List<CategoriaLocal>,
    productosBD: List<ProductoLocalSimplificado>,
    onDismiss: () -> Unit,
    onGuardarExistente: (Int, Int) -> Unit,
    onGuardarNuevo: (String, Int, String, Double, Double, Int) -> Unit
) {
    var esNuevoProducto by remember { mutableStateOf(false) }

    // Campos Existente
    var selectedProductoBd by remember { mutableStateOf(productosBD.firstOrNull()) }
    var stockSumarStr by remember { mutableStateOf("10") }
    var expandirDropdownProd by remember { mutableStateOf(false) }

    // Campos Nuevo
    var nombre by remember { mutableStateOf(productoApi.title ?: "") }
    var precioCompra by remember { mutableStateOf(productoApi.extractedPrice?.toString() ?: "0.0") }
    var precioVenta by remember { mutableStateOf("") }
    var stockInicial by remember { mutableStateOf("10") }

    // Categoría
    var crearCategoriaNueva by remember { mutableStateOf(false) }
    var categoriaSeleccionada by remember { mutableStateOf(categoriasBD.firstOrNull()) }
    var nombreNuevaCategoria by remember { mutableStateOf("") }
    var expandirDropdownCat by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.padding(8.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF0B111E))) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Catalogar Inventario", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(12.dp))

                // SWITCH EXISTENTE / NUEVO
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Sumar a existente", color = if (!esNuevoProducto) Color(0xFF4DA3FF) else Color.Gray)
                    Switch(checked = esNuevoProducto, onCheckedChange = { esNuevoProducto = it }, modifier = Modifier.padding(horizontal = 8.dp))
                    Text("Crear tupla nueva", color = if (esNuevoProducto) Color(0xFF38D996) else Color.Gray)
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!esNuevoProducto) {
                    // MODO: PRODUCTO EXISTENTE
                    Text("Selecciona el producto en BD:", color = Color.Gray, fontSize = 13.sp)
                    Box {
                        OutlinedTextField(
                            value = selectedProductoBd?.nombre ?: "Sin productos en BD",
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            modifier = Modifier.fillMaxWidth().clickable { expandirDropdownProd = true },
                            colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.White)
                        )
                        DropdownMenu(expanded = expandirDropdownProd, onDismissRequest = { expandirDropdownProd = false }) {
                            productosBD.forEach { p ->
                                DropdownMenuItem(text = { Text(p.nombre) }, onClick = { selectedProductoBd = p; expandirDropdownProd = false })
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = stockSumarStr, onValueChange = { stockSumarStr = it },
                        label = { Text("Cantidad a comprar (Stock)", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { selectedProductoBd?.let { onGuardarExistente(it.id, stockSumarStr.toIntOrNull() ?: 0) } },
                        modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4DA3FF))
                    ) { Text("Sumar Stock al Inventario") }

                } else {
                    // MODO: PRODUCTO NUEVO
                    OutlinedTextField(
                        value = nombre, onValueChange = { nombre = it },
                        label = { Text("Nombre del Producto", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = precioCompra, onValueChange = { precioCompra = it },
                            label = { Text("Costo", color = Color.Gray) },
                            modifier = Modifier.weight(1f), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                        OutlinedTextField(
                            value = precioVenta, onValueChange = { precioVenta = it },
                            label = { Text("P. Venta", color = Color(0xFF38D996)) },
                            modifier = Modifier.weight(1f), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                    }

                    OutlinedTextField(
                        value = stockInicial, onValueChange = { stockInicial = it },
                        label = { Text("Stock Inicial", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    // SELECTOR DE CATEGORÍA
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = crearCategoriaNueva, onCheckedChange = { crearCategoriaNueva = it })
                        Text("Crear categoría nueva", color = Color.White)
                    }

                    if (crearCategoriaNueva) {
                        OutlinedTextField(
                            value = nombreNuevaCategoria, onValueChange = { nombreNuevaCategoria = it },
                            label = { Text("Nombre de nueva categoría", color = Color(0xFFFFA64D)) },
                            modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                    } else {
                        Box {
                            OutlinedTextField(
                                value = categoriaSeleccionada?.nombre ?: "Seleccionar Categoría",
                                onValueChange = {}, readOnly = true, enabled = false,
                                modifier = Modifier.fillMaxWidth().clickable { expandirDropdownCat = true },
                                colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.White)
                            )
                            DropdownMenu(expanded = expandirDropdownCat, onDismissRequest = { expandirDropdownCat = false }) {
                                categoriasBD.forEach { c ->
                                    DropdownMenuItem(text = { Text(c.nombre) }, onClick = { categoriaSeleccionada = c; expandirDropdownCat = false })
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            val pC = precioCompra.toDoubleOrNull() ?: 0.0
                            val pV = precioVenta.toDoubleOrNull() ?: 0.0
                            val st = stockInicial.toIntOrNull() ?: 0
                            val idCat = if (crearCategoriaNueva) 0 else (categoriaSeleccionada?.id ?: 0)
                            onGuardarNuevo(nombre, idCat, if(crearCategoriaNueva) nombreNuevaCategoria else "", pC, pV, st)
                        },
                        modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38D996))
                    ) { Text("Crear y Catalogar Producto") }
                }
            }
        }
    }
}

// --- CONEXIONES AL BACKEND PHP ---

suspend fun obtenerCategoriasDeBD(): List<CategoriaLocal> = withContext(Dispatchers.IO) {
    val BASE_URL = "https://horologic-subreniform-angelika.ngrok-free.dev/techstock"
    val lista = mutableListOf<CategoriaLocal>()
    try {
        val connection = URL("$BASE_URL/obtener_categorias.php").openConnection() as HttpURLConnection
        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            val json = JSONObject(connection.inputStream.bufferedReader().readText())
            if (json.getBoolean("success")) {
                val array = json.getJSONArray("data")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    lista.add(CategoriaLocal(obj.getInt("id_categoria"), obj.getString("nombre_categoria")))
                }
            }
        }
    } catch (e: Exception) { e.printStackTrace() }
    lista
}

suspend fun obtenerProductosSimplesDeBD(): List<ProductoLocalSimplificado> = withContext(Dispatchers.IO) {
    val BASE_URL = "https://horologic-subreniform-angelika.ngrok-free.dev/techstock"
    val lista = mutableListOf<ProductoLocalSimplificado>()
    try {
        val connection = URL("$BASE_URL/productos.php").openConnection() as HttpURLConnection
        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            val json = JSONObject(connection.inputStream.bufferedReader().readText())
            if (json.getBoolean("success")) {
                val array = json.getJSONArray("data")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    lista.add(ProductoLocalSimplificado(obj.getInt("id_producto"), obj.getString("nombre_producto"), obj.optString("marca", "")))
                }
            }
        }
    } catch (e: Exception) { e.printStackTrace() }
    lista
}

suspend fun sumarStockABaseDeDatos(idProducto: Int, cantidad: Int): Boolean = withContext(Dispatchers.IO) {
    val BASE_URL = "https://horologic-subreniform-angelika.ngrok-free.dev/techstock"
    try {
        val connection = URL("$BASE_URL/sumar_stock.php").openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.doOutput = true
        val json = JSONObject().apply { put("id_producto", idProducto); put("cantidad", cantidad) }
        OutputStreamWriter(connection.outputStream).use { it.write(json.toString()) }
        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            return@withContext JSONObject(connection.inputStream.bufferedReader().readText()).optBoolean("success", false)
        }
    } catch (e: Exception) { e.printStackTrace() }
    false
}

suspend fun enviarProductoNuevoABD(nombre: String, idCat: Int, nuevaCat: String, pCompra: Double, pVenta: Double, stock: Int, imagenUrl: String): Boolean = withContext(Dispatchers.IO) {
    val BASE_URL = "https://horologic-subreniform-angelika.ngrok-free.dev/techstock"
    try {
        val connection = URL("$BASE_URL/restock.php").openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.doOutput = true
        val json = JSONObject().apply {
            put("nombre", nombre)
            put("id_categoria", idCat)
            put("nueva_categoria", nuevaCat)
            put("precio_compra", pCompra)
            put("precio_venta", pVenta)
            put("stock", stock)
            put("imagen_url", imagenUrl)
        }
        OutputStreamWriter(connection.outputStream).use { it.write(json.toString()) }
        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            return@withContext JSONObject(connection.inputStream.bufferedReader().readText()).optBoolean("success", false)
        }
    } catch (e: Exception) { e.printStackTrace() }
    false
}