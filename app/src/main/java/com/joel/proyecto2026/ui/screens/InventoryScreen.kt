package com.joel.proyecto2026.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.joel.proyecto2026.network.ProductDto
import com.joel.proyecto2026.ui.theme.PrimaryLight
import com.joel.proyecto2026.ui.theme.PrimaryLight2
import com.joel.proyecto2026.ui.viewmodel.HomeViewModel
import androidx.compose.runtime.LaunchedEffect

@Composable
fun InventoryScreen(
    onBackToHome: () -> Unit,
    homeViewModel: HomeViewModel,
    onProductoClick: (ProductDto) -> Unit = {}
) {
    var query by remember { mutableStateOf("") }

    val products = homeViewModel.featured

    val visibleProducts = remember(products, query) {
        products
            .filter { product ->
                val title = product.title.orEmpty()
                val source = product.source.orEmpty()

                title.contains(query, ignoreCase = true) ||
                        source.contains(query, ignoreCase = true)
            }
            .sortedBy { it.title.orEmpty() }
    }

    // Disparar búsqueda remota cuando cambia la consulta (comportamiento simple)
    LaunchedEffect(query) {
        // si la consulta está vacía se podría omitir la llamada; aquí la ejecutamos siempre
        homeViewModel.search(query.ifEmpty { "" })
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF030A1D),
                        Color(0xFF0A1A43),
                        Color(0xFF040C23)
                    )
                )
            )
    ) {
        GlowBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            HeaderInventory(onBackToHome = onBackToHome)

            Spacer(modifier = Modifier.height(18.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    SearchAndActions(
                        query = query,
                        onQueryChange = { query = it }
                    )
                }

                item {
                    StatsRow(products = products)
                }

                if (visibleProducts.isEmpty()) {
                    item {
                        EmptyInventoryMessage()
                    }
                } else {
                    items(
                        items = visibleProducts,
                        key = { product ->
                            "${product.title.orEmpty()}-${product.thumbnail.orEmpty()}"
                        }
                    ) { product ->
                        ProductListItem(
                            product = product,
                            onClick = { onProductoClick(product) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GlowBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(190.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            PrimaryLight.copy(alpha = 0.22f),
                            Color.Transparent
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(230.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            PrimaryLight2.copy(alpha = 0.18f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
private fun HeaderInventory(
    onBackToHome: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackToHome) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = Color.White
            )
        }

        Text(
            text = "Inventario",
            modifier = Modifier.weight(1f),
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.width(48.dp))
    }
}

@Composable
private fun SearchAndActions(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.75f)
                )
            },
            placeholder = {
                Text(
                    text = "Buscar productos",
                    color = Color.White.copy(alpha = 0.45f)
                )
            },
            modifier = Modifier
                .weight(1f)
                .height(58.dp),
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF07111D),
                unfocusedContainerColor = Color(0xFF07111D),
                focusedBorderColor = Color.White.copy(alpha = 0.18f),
                unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = PrimaryLight
            )
        )

        ActionMiniButton(
            icon = Icons.Filled.FilterList,
            text = "Filtrar"
        )

        ActionMiniButton(
            icon = Icons.Filled.Sort,
            text = "Ordenar"
        )
    }
}

@Composable
private fun ActionMiniButton(
    icon: ImageVector,
    text: String
) {
    Card(
        modifier = Modifier
            .width(74.dp)
            .height(58.dp)
            .clickable { },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF07111D)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = text,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 11.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun StatsRow(
    products: List<ProductDto>
) {
    val lowStockCount = products.count { product ->
        getFakeStock(product) < 10
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Filled.Inventory,
            title = "Total",
            value = products.size.toString(),
            subtitle = "items",
            iconColor = Color(0xFF4DA3FF)
        )

        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Filled.Warning,
            title = "Bajo",
            value = lowStockCount.toString(),
            subtitle = "stock",
            iconColor = Color(0xFFFF8A00)
        )

        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Filled.Category,
            title = "Categorías",
            value = "24",
            subtitle = "activas",
            iconColor = Color(0xFF4DA3FF)
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    value: String,
    subtitle: String,
    iconColor: Color
) {
    Card(
        modifier = modifier.heightIn(min = 96.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF07111D)
        ),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = title,
                color = Color.White.copy(alpha = 0.66f),
                fontSize = 11.sp,
                maxLines = 1
            )

            Text(
                text = value,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ProductListItem(
    product: ProductDto,
    onClick: () -> Unit
) {
    val stock = getFakeStock(product)
    val isLowStock = stock < 10

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 118.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF07111D)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 7.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = product.thumbnail,
                contentDescription = product.title,
                modifier = Modifier
                    .size(86.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.06f)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.title ?: "Producto sin nombre",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = "Provedor: ${product.source ?: "Categoría"} • SKU: ${generateSku(product)}",
                    color = Color.White.copy(alpha = 0.62f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Stock: $stock",
                    color = if (isLowStock) Color(0xFFFF8A00) else Color(0xFF3EDC81),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                StatusPill(isLow = isLowStock)

                Spacer(modifier = Modifier.height(10.dp))

                Icon(
                    imageVector = Icons.Filled.KeyboardArrowRight,
                    contentDescription = "Ver producto",
                    tint = Color.White.copy(alpha = 0.55f),
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
private fun StatusPill(
    isLow: Boolean
) {
    val color = if (isLow) Color(0xFFFF8A00) else Color(0xFF3EDC81)
    val text = if (isLow) "Low Stock" else "In Stock"

    Row(
        modifier = Modifier
            .widthIn(min = 92.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )

        Spacer(modifier = Modifier.width(7.dp))

        Text(
            text = text,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

@Composable
private fun EmptyInventoryMessage() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 140.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF07111D)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No se encontraron productos",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp
            )
        }
    }
}

private fun getFakeStock(product: ProductDto): Int {
    return ((product.reviews ?: 0) / 10).coerceAtLeast(1)
}

private fun generateSku(product: ProductDto): String {
    val cleanTitle = product.title
        .orEmpty()
        .replace(" ", "")
        .replace("-", "")
        .uppercase()
        .take(8)

    return cleanTitle.ifEmpty { "ITEM0001" }
}